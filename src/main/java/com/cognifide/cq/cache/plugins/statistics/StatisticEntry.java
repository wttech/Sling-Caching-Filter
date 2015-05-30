package com.cognifide.cq.cache.plugins.statistics;

import com.cognifide.cq.cache.cache.CacheHolder;
import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.Collections;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatisticEntry {

	private static final Logger logger = LoggerFactory.getLogger(StatisticEntry.class);

	private static final String CACHE_OBJECT_NAME = "javax.cache:type=CacheStatistics,CacheManager=%s,Cache=%s";

	private static final String CACHE_MISS_PERCENTAGE_FIELD_NAME = "CacheMissPercentage";

	private static final String CACHE_MISSES_FIELD_NAME = "CacheMisses";

	private static final String CACHE_HITS_FIELD_NAME = "CacheHits";

	private final String cacheName;

	private final long cacheHits;

	private final long cacheMisses;

	private final float cacheMissPercentage;

	private final Collection<String> keys;

	public StatisticEntry(CacheHolder cacheHolder, String cacheName)
			throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException,
			MalformedObjectNameException {
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

		this.cacheName = cacheName;

		ObjectName objectName = buildObjectName(cacheHolder, cacheName);
		this.cacheHits = (Long) mBeanServer.getAttribute(objectName, CACHE_HITS_FIELD_NAME);
		this.cacheMisses = (Long) mBeanServer.getAttribute(objectName, CACHE_MISSES_FIELD_NAME);
		this.cacheMissPercentage = (Float) mBeanServer.getAttribute(objectName, CACHE_MISS_PERCENTAGE_FIELD_NAME);

		this.keys = cacheHolder.getKeysFor(cacheName);
	}

	private ObjectName buildObjectName(CacheHolder cacheHolder, String cacheName) throws MalformedObjectNameException {
		String name = String.format(CACHE_OBJECT_NAME,
				sanitize(cacheHolder.getCacheManagerURI().toString()),
				sanitize(cacheName));
		if (logger.isDebugEnabled()) {
			logger.debug("Build object name with {} name", name);
		}
		return new ObjectName(name);
	}

	private String sanitize(String string) {
		return string == null ? "" : string.replaceAll(",|:|=|\n", ".");
	}

	public String getCacheName() {
		return cacheName;
	}

	public long getCacheHits() {
		return cacheHits;
	}

	public long getCacheMisses() {
		return cacheMisses;
	}

	public float getCacheHitPercentage() {
		long hits = cacheHits + cacheMisses;
		return 0L == hits ? 0.0f : (cacheHits / (float) (hits)) * 100.0f;
	}

	public float getCacheMissPercentage() {
		return cacheMissPercentage;
	}

	public Collection<String> getKeys() {
		return Collections.unmodifiableCollection(keys);
	}

}
