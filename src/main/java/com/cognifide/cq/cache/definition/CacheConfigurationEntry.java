package com.cognifide.cq.cache.definition;

public interface CacheConfigurationEntry {

	/**
	 * Resource type of resource, for which cache definition was created.
	 *
	 * @return resource type
	 */
	String getResourceType();

	/**
	 * Specifies cache entry validity time in seconds. If not set duration property read from the OSGi console will be
	 * used.
	 *
	 * @return validity time in seconds
	 */
	Integer getValidityTimeInSeconds();

	/**
	 * Specifies the level of component caching. By default set to -1.
	 *
	 * @return level of cache
	 */
	Integer getCacheLevel();

}
