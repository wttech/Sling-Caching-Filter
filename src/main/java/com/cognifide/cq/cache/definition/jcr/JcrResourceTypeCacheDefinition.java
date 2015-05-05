package com.cognifide.cq.cache.definition.jcr;

import com.cognifide.cq.cache.definition.ResourceTypeCacheDefinition;
import com.cognifide.cq.cache.model.CacheConstants;
import java.util.Arrays;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

public class JcrResourceTypeCacheDefinition implements ResourceTypeCacheDefinition {

	private final Boolean cacheEnabled;

	private final String resourceType;

	private final Integer validityTime;

	private final Integer cacheLevel;

	private final Boolean invalidateOnSelf;

	private final String[] invalidateOnReferencedFields;

	private final String[] invalidateOnPaths;

	public JcrResourceTypeCacheDefinition(Resource cacheResource, Resource requestedResource, int defaultTime) {
		ValueMap valueMap = cacheResource.adaptTo(ValueMap.class);
		this.cacheEnabled = valueMap.get(CacheConstants.CACHE_ENABLED, false);
		this.validityTime = valueMap.get(CacheConstants.CACHE_VALIDITY_TIME, defaultTime);
		this.cacheLevel = valueMap.get(CacheConstants.CACHE_LEVEL, Integer.MIN_VALUE);
		this.invalidateOnSelf = valueMap.get(CacheConstants.CACHE_INVALIDATE_ON_SELF, true);
		this.invalidateOnReferencedFields = readArray(valueMap.get(CacheConstants.CACHE_INVALIDATE_FIELDS));
		this.invalidateOnPaths = readArray(valueMap.get(CacheConstants.CACHE_INVALIDATE_PATHS));
		this.resourceType = requestedResource.getResourceType();
	}

	private String[] readArray(Object value) {
		String[] result = new String[]{};
		if (value == null) {
			result = new String[]{};
		} else if (value instanceof String) {
			result = new String[]{(String) value};
		} else if (value instanceof String[]) {
			result = (String[]) value;
		} else if (value instanceof Object[]) {
			Object[] array = ((Object[]) value);
			result = Arrays.copyOfRange(array, 0, array.length, String[].class);
		} else {
			throw new IllegalArgumentException("Expected argument type is String or String[] but was "
					+ value.getClass().getName());
		}
		return result;
	}

	@Override
	public Boolean isEnabled() {
		return cacheEnabled;
	}

	@Override
	public String getResourceType() {
		return resourceType;
	}

	@Override
	public Integer getValidityTimeInSeconds() {
		return validityTime;
	}

	@Override
	public Integer getCacheLevel() {
		return cacheLevel;
	}

	@Override
	public Boolean isInvalidateOnSelf() {
		return invalidateOnSelf;
	}

	@Override
	public String[] getInvalidateOnReferencedFields() {
		return Arrays.copyOf(invalidateOnReferencedFields, invalidateOnReferencedFields.length);
	}

	@Override
	public String[] getInvalidateOnPaths() {
		return Arrays.copyOf(invalidateOnPaths, invalidateOnPaths.length);
	}
}
