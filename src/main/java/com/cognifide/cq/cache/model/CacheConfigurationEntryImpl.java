package com.cognifide.cq.cache.model;

/**
 * @author Jakub Malecki
 */
public class CacheConfigurationEntry {

	private String resourceType;

	private int time; // in seconds

	private int cacheLevel;

	public CacheConfigurationEntry(String resourceType, int time, int cacheLevel) {
		this.resourceType = resourceType;
		this.time = time;
		this.cacheLevel = cacheLevel;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public int getCacheLevel() {
		return cacheLevel;
	}

	public void setCacheLevel(int cacheLevel) {
		this.cacheLevel = cacheLevel;
	}

}
