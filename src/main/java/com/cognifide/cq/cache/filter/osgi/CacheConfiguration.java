package com.cognifide.cq.cache.filter.osgi;

public interface CacheConfiguration {

	/**
	 * Controls cache availability
	 *
	 * @return true if cache should be enabled, false otherwise
	 */
	boolean isEnabled();

	/**
	 * Time after element in cache will be invalid
	 *
	 * @return validity time in seconds
	 */
	int getValidityTimeInSeconds();
}
