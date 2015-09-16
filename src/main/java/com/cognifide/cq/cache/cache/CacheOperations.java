package com.cognifide.cq.cache.cache;

import com.cognifide.cq.cache.definition.CacheConfigurationEntry;
import com.google.common.base.Optional;
import javax.cache.Cache;

/**
 * Handler for basic cache operations: create, retrieve, delete.
 */
public interface CacheOperations {

	/**
	 * Creates cache for given configuration.
	 *
	 * @param cacheConfigurationEntry cache configuration
	 * @return created cache
	 */
	Optional<Cache<String, CacheEntity>> create(CacheConfigurationEntry cacheConfigurationEntry);

	/**
	 * Searches for cache with given name
	 *
	 * @param cacheName
	 * @return cache or null if cache with given name does not exist
	 */
	Optional<Cache<String, CacheEntity>> findFor(String cacheName);

	/**
	 * Deletes cache with given name
	 *
	 * @param cacheName cache name
	 */
	void delete(String cacheName);

}
