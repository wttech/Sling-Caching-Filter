package com.cognifide.cq.cache.plugins.statistics;

import com.cognifide.cq.cache.filter.cache.action.CacheAction;

// might extends CacheEntryEventListener when when statistics from cache are to be used
public interface Statistics {

	static final String KEY_PARAMETER = "key";

	static final String ACTION_PARAMETER = "action";

	static final String DELETE_ACTION_PARAMETER_VALUE = "delete";

	static final String SHOW_KEYS_ACTION_PARAMETER_VALUE = "showKeys";

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
