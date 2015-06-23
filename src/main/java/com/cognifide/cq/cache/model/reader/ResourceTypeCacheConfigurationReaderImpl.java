package com.cognifide.cq.cache.model.reader;

import com.cognifide.cq.cache.definition.ResourceTypeCacheDefinition;
import com.cognifide.cq.cache.filter.osgi.CacheConfiguration;
import com.cognifide.cq.cache.model.ResourceTypeCacheConfiguration;
import com.cognifide.cq.cache.model.alias.PathAliasStore;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.ReferenceStrategy;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
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

	@Override
	public boolean hasConfigurationFor(SlingHttpServletRequest request) {
		return resourceTypeCacheDefinitions.containsKey(request.getResource().getResourceType());
	}

	@Override
	public ResourceTypeCacheConfiguration readComponentConfiguration(SlingHttpServletRequest request) {
		Resource resource = request.getResource();

		ResourceTypeCacheDefinition resourceTypeCacheDefinition = findResourceTypeCacheDefinition(resource);
		ResourceTypeCacheConfiguration configuration = null;

		if (null != resourceTypeCacheDefinition) {
			configuration = new ResourceTypeCacheConfiguration(resourceTypeCacheDefinition, cacheConfiguration,
					pathAliasStore);
		}

		return configuration;
	}

	private ResourceTypeCacheDefinition findResourceTypeCacheDefinition(Resource resource) {
		ResourceTypeCacheDefinition resourceTypeCacheDefinition = null;
		if (resourceTypeCacheDefinitions.containsKey(resource.getResourceType())) {
			resourceTypeCacheDefinition = resourceTypeCacheDefinitions.get(resource.getResourceType());
		}
		return resourceTypeCacheDefinition;
	}
}
