package com.cognifide.cq.cache.filter.osgi;

import com.cognifide.cq.cache.cache.CacheEntity;
import javax.cache.Cache;
import javax.cache.CacheManager;

public interface CacheManagerProvider {

	/**
	 * Returns full configured cache manager
	 *
	 * @return cache manager
	 */
	CacheManager getCacheManager();

	/**
	 * Updated cache configuration with specific vendor configuration
	 *
	 * @param cache - cache configuration that will be updated
	 */
	void updateCacheConfiguration(Cache<String, CacheEntity> cache);
}
