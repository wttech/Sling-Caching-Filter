package com.cognifide.cq.cache.plugins.statistics.entry;

import com.cognifide.cq.cache.cache.CacheHolder;
import com.google.common.base.Optional;
import java.net.URI;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CacheObjectNameBuilder {

	private static final Logger logger = LoggerFactory.getLogger(CacheObjectNameBuilder.class);

	private static final String CACHE_OBJECT_NAME = "javax.cache:type=CacheStatistics,CacheManager=%s,Cache=%s";

	ObjectName buildObjectName(CacheHolder cacheHolder, String cacheName) throws MalformedObjectNameException {
		Optional<URI> uri = cacheHolder.getCacheManagerURI();
		if (!uri.isPresent()) {
			throw new IllegalStateException("Cache Manager did not respond with valid URI. Possible reason - cache manager is closed.");
		}

		String name = String.format(CACHE_OBJECT_NAME,
				sanitize(uri.get().toString()),
				sanitize(cacheName));
		if (logger.isDebugEnabled()) {
			logger.debug("Build object name with {} name", name);
		}
		return new ObjectName(name);
	}

	private String sanitize(String string) {
		return string == null ? "" : string.replaceAll(",|:|=|\n", ".");
	}
}
