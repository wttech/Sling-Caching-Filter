package com.cognifide.cq.cache.model.reader;

import com.cognifide.cq.cache.model.ResourceTypeCacheConfiguration;
import org.apache.sling.api.SlingHttpServletRequest;

public interface ResourceTypeCacheConfigurationReader {

	/**
	 * Checks if requested resource has configuration
	 *
	 * @param request current request
	 * @return true if configuration can be found for requested resource
	 */
	boolean hasConfigurationFor(SlingHttpServletRequest request);

	/**
	 * Reads the configuration for the resource requested in given request. The configuration is read from the type of
	 * the requested resource.
	 *
	 * @param request
	 * @return
	 */
	ResourceTypeCacheConfiguration readComponentConfiguration(SlingHttpServletRequest request);

}
