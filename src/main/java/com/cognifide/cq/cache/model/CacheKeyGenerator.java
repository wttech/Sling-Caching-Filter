package com.cognifide.cq.cache.model;

import org.apache.sling.api.resource.Resource;

/**
 * Generates cache keys basing on given strategy.
 * 
 * @author Jakub Malecki
 */
public interface CacheKeyGenerator {

	String generateKey(int cacheLevel, Resource resource, String selectorString);

	String generateKey(int cacheLevel, String prefix, String pagePath, String selectorString);
}
