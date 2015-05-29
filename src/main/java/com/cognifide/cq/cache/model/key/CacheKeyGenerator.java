package com.cognifide.cq.cache.model.key;

import com.cognifide.cq.cache.definition.CacheConfigurationEntry;
import org.apache.sling.api.SlingHttpServletRequest;

/**
 * Generates cache keys basing on given strategy.
 *
 * @author Jakub Malecki
 */
public interface CacheKeyGenerator {

	/**
	 * Generates key based on given request and cache configuration entry
	 *
	 * @param request
	 * @param cacheConfigurationEntry
	 * @return generated key
	 */
	String generateKey(SlingHttpServletRequest request, CacheConfigurationEntry cacheConfigurationEntry);
}
