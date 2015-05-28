package com.cognifide.cq.cache.model.reader;

import com.cognifide.cq.cache.definition.ResourceTypeCacheDefinition;
import com.cognifide.cq.cache.definition.jcr.JcrResourceTypeCacheDefinition;
import com.cognifide.cq.cache.filter.osgi.CacheConfiguration;
import com.cognifide.cq.cache.model.CacheConstants;
import com.cognifide.cq.cache.model.InvalidationPathUtil;
import com.cognifide.cq.cache.model.ResourceTypeCacheConfiguration;
import com.cognifide.cq.cache.model.alias.PathAliasStore;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.ReferenceStrategy;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bartosz Rudnicki
 */
@Service
@Component(immediate = true)
public class ResourceTypeCacheConfigurationReaderImpl implements ResourceTypeCacheConfigurationReader {

	private static final Logger logger = LoggerFactory.getLogger(ResourceTypeCacheConfigurationReaderImpl.class);

	private static final String INVALIDATION_PATH = "%s.*";

	private static final String RESOURCE_TYPE_PATH = "/apps/%s";

	@Reference(
			referenceInterface = ResourceTypeCacheDefinition.class,
			policy = ReferencePolicy.DYNAMIC,
			cardinality = ReferenceCardinality.MANDATORY_MULTIPLE,
			strategy = ReferenceStrategy.EVENT)
	private final ConcurrentMap<String, ResourceTypeCacheDefinition> resourceTypeCacheDefinitions
			= new ConcurrentHashMap<String, ResourceTypeCacheDefinition>(8);

	@Reference
	private PathAliasStore pathAliasStore;

	@Reference
	private CacheConfiguration cacheConfiguration;

	@Override
	public boolean hasConfigurationFor(SlingHttpServletRequest request) {
		return hasConfigurationInOsgi(request) || hasConfigurationInJcr(request);
	}

	private boolean hasConfigurationInOsgi(SlingHttpServletRequest request) {
		return resourceTypeCacheDefinitions.containsKey(request.getResource().getResourceType());
	}

	private boolean hasConfigurationInJcr(SlingHttpServletRequest request) {
		Resource typeResource = getTypeResource(request);
		Resource cacheResource = getCacheResource(request, typeResource);
		return null != cacheResource;
	}

	private Resource getCacheResource(SlingHttpServletRequest request, Resource typeResource) {
		return request.getResourceResolver().getResource(typeResource, CacheConstants.CACHE_PATH);
	}

	@Override
	public ResourceTypeCacheConfiguration readComponentConfiguration(SlingHttpServletRequest request) {
		Resource requestedResource = request.getResource();

		Resource typeResource = getTypeResource(request);
		Resource cacheResource = getCacheResource(request, typeResource);

		ResourceTypeCacheDefinition resourceTypeCacheDefinition
				= findResourceTypeCacheDefinition(requestedResource, cacheResource);
		ResourceTypeCacheConfiguration configuration = null;

		if (null != resourceTypeCacheDefinition) {
			configuration = readComponentConfiguration(requestedResource, resourceTypeCacheDefinition);
			if (typeResource != null) {
				configuration.setResourceTypePath(typeResource.getPath());
			}
		}

		return configuration;
	}

	public void bindResourceTypeCacheDefinition(ResourceTypeCacheDefinition resourceTypeCacheDefinition) {
		String resourceType = resourceTypeCacheDefinition.getResourceType();
		if (resourceTypeCacheDefinitions.containsKey(resourceType)) {
			logger.warn("Resource type cache definition was already defined for {}", resourceType);
		}
		resourceTypeCacheDefinitions.putIfAbsent(resourceType, resourceTypeCacheDefinition);
	}

	public void unbindResourceTypeCacheDefinition(ResourceTypeCacheDefinition resourceTypeCacheDefinition) {
		resourceTypeCacheDefinitions.remove(resourceTypeCacheDefinition.getResourceType());
	}

	private ResourceTypeCacheDefinition findResourceTypeCacheDefinition(
			Resource requestedResource, Resource cacheResource) {
		ResourceTypeCacheDefinition resourceTypeCacheDefinition = null;
		if (resourceTypeCacheDefinitions.containsKey(requestedResource.getResourceType())) {
			resourceTypeCacheDefinition = resourceTypeCacheDefinitions.get(requestedResource.getResourceType());
		} else if (null != cacheResource) {
			resourceTypeCacheDefinition
					= new JcrResourceTypeCacheDefinition(cacheResource, requestedResource, cacheConfiguration.getDuration());
		}
		return resourceTypeCacheDefinition;
	}

	/**
	 * Reads the component cache configuration.
	 */
	private ResourceTypeCacheConfiguration readComponentConfiguration(Resource requestedResource,
			ResourceTypeCacheDefinition resourceTypeCacheDefinition) {
		ResourceTypeCacheConfiguration config
				= new ResourceTypeCacheConfiguration(resourceTypeCacheDefinition, cacheConfiguration.getDuration());
		return readComponentPathsConfiguration(requestedResource, config, resourceTypeCacheDefinition);
	}

	/**
	 * Prepares a list of all paths that should be listened for changes in order to invalidate the cache of given
	 * component.
	 */
	private ResourceTypeCacheConfiguration readComponentPathsConfiguration(Resource requestedResource,
			ResourceTypeCacheConfiguration config, ResourceTypeCacheDefinition resourceTypeCacheDefinition) {
		// self change invalidation
		if (resourceTypeCacheDefinition.isInvalidateOnSelf()) {
			config.addInvalidatePath(getSelfChangeInvalidationPath(requestedResource));
		}

		// reference fields invalidation
		config.addInvalidatePaths(getReferenceFieldInvalidation(requestedResource, resourceTypeCacheDefinition));

		// custom paths invalidation
		config.addInvalidatePaths(getCustomPathInvalidation(resourceTypeCacheDefinition));

		return config;
	}

	private List<String> getCustomPathInvalidation(ResourceTypeCacheDefinition resourceTypeCacheDefinition) {
		return InvalidationPathUtil.getInvalidationPaths(pathAliasStore, resourceTypeCacheDefinition.getInvalidateOnPaths());
	}

	private List<String> getReferenceFieldInvalidation(
			Resource requestedResource, ResourceTypeCacheDefinition resourceTypeCacheDefinition) {
		List<String> result = new ArrayList<String>();
		ValueMap resourceMap = requestedResource.adaptTo(ValueMap.class);
		if (resourceMap != null) {
			for (String fieldName : resourceTypeCacheDefinition.getInvalidateOnReferencedFields()) {
				if (StringUtils.isNotBlank(fieldName)) {
					String fieldValue = resourceMap.get(fieldName, String.class);
					if (StringUtils.isNotBlank(fieldValue)) {
						result.add(String.format(INVALIDATION_PATH, fieldValue));
					}
				}
			}
		}
		return result;
	}

	private String getSelfChangeInvalidationPath(Resource requestedResource) {
		return String.format(INVALIDATION_PATH, getPagePath(requestedResource.getPath()));
	}

	/**
	 * Returns the Resource of the type of the requested component.
	 */
	private Resource getTypeResource(SlingHttpServletRequest request) {
		return request.getResourceResolver().getResource(
				getAbsoluteTypePath(request.getResource().getResourceType()));
	}

	private String getAbsoluteTypePath(String path) {
		return path.startsWith("/") ? path : String.format(RESOURCE_TYPE_PATH, path);
	}

	private String getPagePath(String componentPath) {
		int jcrContentIndex = componentPath.indexOf("/jcr:content");
		return jcrContentIndex > 0 ? componentPath.substring(0, jcrContentIndex) : componentPath;
	}
}
