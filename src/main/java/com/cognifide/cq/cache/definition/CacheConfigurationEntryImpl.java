package com.cognifide.cq.cache.definition;

/**
 * @author Jakub Malecki
 */
public class CacheConfigurationEntryImpl implements CacheConfigurationEntry {

	private String resourceType;

	private int validityTimeInSeconds;

	private int cacheLevel;

	public CacheConfigurationEntryImpl(String resourceType, int time, int cacheLevel) {
		this.resourceType = resourceType;
		this.validityTimeInSeconds = time;
		this.cacheLevel = cacheLevel;
	}

	@Override
	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	@Override
	public Integer getValidityTimeInSeconds() {
		return validityTimeInSeconds;
	}

	public void setValidityTimeInSeconds(int validityTimeInSeconds) {
		this.validityTimeInSeconds = validityTimeInSeconds;
	}

	@Override
	public Integer getCacheLevel() {
		return cacheLevel;
	}

	public void setCacheLevel(int cacheLevel) {
		this.cacheLevel = cacheLevel;
	}

}
