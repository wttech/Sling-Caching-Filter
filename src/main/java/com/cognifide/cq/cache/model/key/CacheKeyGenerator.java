package com.cognifide.cq.cache.model.key;

import com.cognifide.cq.cache.model.CacheConfigurationEntry;
import org.apache.sling.api.SlingHttpServletRequest;

/**
 * Generates cache keys basing on given strategy.
 *
 * @author Jakub Malecki
 */
public interface CacheKeyGenerator {

	String generateKey(SlingHttpServletRequest request, CacheConfigurationEntry cacheConfigurationEntry);
}
