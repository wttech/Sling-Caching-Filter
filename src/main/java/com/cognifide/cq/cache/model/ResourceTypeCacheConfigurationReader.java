package com.cognifide.cq.cache.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

/**
 * @author Bartosz Rudnicki
 */
public class ResourceTypeCacheConfigurationReader {

	private static final String INVALIDATION_PATH = "%s.*";

	private static final String RESOURCE_TYPE_PATH = "/apps/%s";

	private PathAliasStore pathAliasStore;

	public void setPathAliasStore(PathAliasStore pathAliasStore) {
		this.pathAliasStore = pathAliasStore;
	}

	/**
	 * Reads the configuration for the resource requested in given request. The configuration is read from the type of
	 * the requested resource.
	 */
	public ResourceTypeCacheConfiguration readComponentConfiguration(SlingHttpServletRequest request,
			Map<String, CacheConfigurationEntry> cacheConfigurationEntries, int defaultTime) {
		Resource requestedResource = request.getResource();

		Resource typeResource = getTypeResource(request);
		Resource cacheResource = request.getResourceResolver().getResource(typeResource,
				CacheConstants.CACHE_PATH);

		ResourceTypeCacheConfiguration config;
		if (cacheResource == null) {
			config = new ResourceTypeCacheConfiguration(requestedResource.getResourceType(), defaultTime);
			config.addInvalidatePath(String.format(INVALIDATION_PATH,
					getPagePath(requestedResource.getPath())));
		} else {
			config = readComponentConfiguration(requestedResource, cacheResource.adaptTo(ValueMap.class),
					defaultTime);
		}

		overrideComponentSettings(cacheConfigurationEntries, requestedResource, config);

		if (typeResource != null) {
			config.setResourceTypePath(typeResource.getPath());
		}

		return config;
	}

	/**
	 * Overrides values from the component .content.xml file with values set up in the OSGi management console.
	 */
	private void overrideComponentSettings(Map<String, CacheConfigurationEntry> cacheConfigurationEntries,
			Resource requestedResource, ResourceTypeCacheConfiguration config) {
		CacheConfigurationEntry globalEntry = cacheConfigurationEntries.get(requestedResource
				.getResourceType());
		if (globalEntry != null) {
			config.setEnabled(true);
			if (globalEntry.getTime() != Integer.MIN_VALUE) {
				config.setTime(globalEntry.getTime());
			}
			if (globalEntry.getCacheLevel() != Integer.MIN_VALUE) {
				config.setCacheLevel(globalEntry.getCacheLevel());
			}
		}
	}

	/**
	 * Reads the component cache configuration.
	 */
	private ResourceTypeCacheConfiguration readComponentConfiguration(Resource requestedResource,
			ValueMap cacheMap, int defaultTime) {
		ResourceTypeCacheConfiguration config = readBasicComponentConfiguration(
				requestedResource.getResourceType(), cacheMap, defaultTime);
		readComponentPathsConfiguration(requestedResource, cacheMap, config);
		return config;
	}

	/**
	 * Prepares a list of all paths that should be listened for changes in order to invalidate the cache of given
	 * component.
	 */
	private void readComponentPathsConfiguration(Resource requestedResource, ValueMap cacheMap,
			ResourceTypeCacheConfiguration config) {
		// self change invalidation
		if (cacheMap.get(CacheConstants.CACHE_INVALIDATE_ON_SELF, true)) {
			String selfChangeInvalidationPath = getSelfChangeInvalidationPath(requestedResource);
			config.addInvalidatePath(selfChangeInvalidationPath);
		}

		// reference fields invalidation
		List<String> referenceFieldInvalidation = getReferenceFieldInvalidation(requestedResource, cacheMap);
		config.addInvalidatePaths(referenceFieldInvalidation);

		// custom paths invalidation
		List<String> customPathInvalidation = getCustomPathInvalidation(cacheMap);
		config.addInvalidatePaths(customPathInvalidation);
	}

	private List<String> getCustomPathInvalidation(ValueMap cacheMap) {
		String[] invalidatePaths = readArray(cacheMap.get(CacheConstants.CACHE_INVALIDATE_PATHS));
		return InvalidationPathUtil.getInvalidationPaths(pathAliasStore, invalidatePaths);
	}

	private List<String> getReferenceFieldInvalidation(Resource requestedResource, ValueMap cacheMap) {
		List<String> result = new ArrayList<String>();
		String[] invalidateFields = readArray(cacheMap.get(CacheConstants.CACHE_INVALIDATE_FIELDS));
		for (String fieldName : invalidateFields) {
			if (StringUtils.isNotBlank(fieldName)) {
				ValueMap resourceMap = requestedResource.adaptTo(ValueMap.class);
				if (resourceMap != null) {
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
	 * Reads component basic cache configuration.
	 */
	private ResourceTypeCacheConfiguration readBasicComponentConfiguration(String resourceType,
			ValueMap cacheMap, int defaultTime) {
		int cacheLevel = cacheMap.get(CacheConstants.CACHE_LEVEL, Integer.MIN_VALUE);
		int time = cacheMap.get(CacheConstants.CACHE_VALIDITY_TIME, defaultTime);

		ResourceTypeCacheConfiguration config = new ResourceTypeCacheConfiguration(resourceType, time,
				cacheLevel);
		config.setEnabled(cacheMap.get(CacheConstants.CACHE_ENABLED, false));
		return config;
	}

	/**
	 * Reads an array from given object.
	 */
	private String[] readArray(Object value) {
		if (value == null) {
			return new String[]{};
		} else if (value instanceof String) {
			return new String[]{(String) value};
		} else if (value instanceof String[]) {
			return (String[]) value;
		} else if (value instanceof Object[]) {
			Object[] array = ((Object[]) value);
			String[] result = new String[array.length];
			for (int i = 0; i < array.length; i++) {
				if (array[i] == null) {
					result[i] = null;
				} else {
					result[i] = array[i].toString();
				}
			}
			return result;
		} else {
			throw new IllegalArgumentException("Expected argument type is String or String[] but was "
					+ value.getClass().getName());
		}
	}

	/**
	 * Returns the Resource of the type of the requested component.
	 */
	private Resource getTypeResource(SlingHttpServletRequest request) {
		return request.getResourceResolver().getResource(
				getAbsoluteTypePath(request.getResource().getResourceType()));
	}

	private String getAbsoluteTypePath(String path) {
		if (path.startsWith("/")) {
			return path;
		} else {
			return String.format(RESOURCE_TYPE_PATH, path);
		}
	}

	private String getPagePath(String componentPath) {
		int jcrContentIndex = componentPath.indexOf("/jcr:content");
		String pagePath;
		if (jcrContentIndex > 0) {
			pagePath = componentPath.substring(0, jcrContentIndex);
		} else {
			pagePath = componentPath;
		}
		return pagePath;
	}
}
