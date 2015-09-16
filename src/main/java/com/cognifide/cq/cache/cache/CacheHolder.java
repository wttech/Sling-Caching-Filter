package com.cognifide.cq.cache.cache;

import com.cognifide.cq.cache.cache.callback.MissingCacheEntryCallback;
import com.cognifide.cq.cache.model.ResourceTypeCacheConfiguration;
import com.google.common.base.Optional;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import javax.servlet.ServletException;
import org.apache.sling.api.SlingHttpServletRequest;

public interface CacheHolder {

	/**
	 *
	 * @return cache manager uri
	 */
	Optional<URI> getCacheManagerURI();

	/**
	 * Collects names of all caches
	 *
	 * @return iterable of cache names
	 */
	Iterable<String> getCacheNames();

	/**
	 * Collects all keys for given cache
	 *
	 * @param cacheName
	 * @return collections of keys
	 */
	Collection<String> getKeysFor(String cacheName);

	/**
	 * Gets element from cache. Cache and key are generated based on current resource and resource type. If element does
	 * not exist in cache callback method is used to generate content.
	 *
	 * @param request
	 * @param resourceTypeCacheConfiguration
	 * @param callback
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	CacheEntity putOrGet(SlingHttpServletRequest request, ResourceTypeCacheConfiguration resourceTypeCacheConfiguration, MissingCacheEntryCallback callback)
			throws IOException, ServletException;

	/**
	 * Removes element from cache using given cache name and key
	 *
	 * @param cacheName
	 * @param key
	 */
	void remove(String cacheName, String key);

	/**
	 * Clears all elements in given cache
	 *
	 * @param cacheName - cache name
	 */
	void clear(String cacheName);
}
