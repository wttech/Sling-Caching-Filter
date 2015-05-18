package com.cognifide.cq.cache.plugins.statistics;

import com.cognifide.cq.cache.filter.cache.action.CacheAction;

public interface Statistics {

	/**
	 * Action indicating that entry in cache was not acquired for given resource type and key.
	 *
	 * @param resourceType
	 * @param key
	 * @param cacheAction
	 */
	void cacheMiss(String resourceType, String key, CacheAction cacheAction);

	/**
	 * Action indicating that entry in cache was acquired for given resource type.
	 *
	 * @param resourceType
	 */
	void cacheHit(String resourceType);

	/**
	 * Clears all statistics.
	 */
	void clearStatistics();
}
