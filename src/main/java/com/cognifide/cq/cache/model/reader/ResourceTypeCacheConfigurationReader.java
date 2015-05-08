package com.cognifide.cq.cache.model.reader;

import com.cognifide.cq.cache.model.ResourceTypeCacheConfiguration;
import org.apache.sling.api.SlingHttpServletRequest;

public interface ResourceTypeCacheConfigurationReader {

	/**
	 * Reads the configuration for the resource requested in given request. The configuration is read from the type of
	 * the requested resource.
	 *
	 * @param request
	 * @param defaultTime
	 * @return
	 */
	ResourceTypeCacheConfiguration readComponentConfiguration(SlingHttpServletRequest request, int defaultTime);

}
