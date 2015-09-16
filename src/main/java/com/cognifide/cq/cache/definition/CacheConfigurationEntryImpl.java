package com.cognifide.cq.cache.definition;

import com.google.common.base.Preconditions;

/**
 * @author Jakub Malecki
 */
public class CacheConfigurationEntryImpl implements CacheConfigurationEntry {

	private final String resourceType;

	private final int validityTimeInSeconds;

	private final int cacheLevel;

	public CacheConfigurationEntryImpl(CacheConfigurationEntry cacheConfigurationEntry) {
		Preconditions.checkNotNull(cacheConfigurationEntry);
		this.resourceType = Preconditions.checkNotNull(cacheConfigurationEntry.getResourceType());
		this.validityTimeInSeconds = cacheConfigurationEntry.getValidityTimeInSeconds();
		this.cacheLevel = cacheConfigurationEntry.getCacheLevel();
	}

	@Override
	public String getResourceType() {
		return resourceType;
	}

	@Override
	public int getValidityTimeInSeconds() {
		return validityTimeInSeconds;
	}

	@Override
	public int getCacheLevel() {
		return cacheLevel;
	}
}
