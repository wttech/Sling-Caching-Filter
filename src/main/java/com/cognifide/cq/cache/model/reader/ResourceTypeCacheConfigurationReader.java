package com.cognifide.cq.cache.model.reader;

import com.cognifide.cq.cache.model.ResourceTypeCacheConfiguration;
import com.google.common.base.Optional;
import org.apache.sling.api.SlingHttpServletRequest;

public interface ResourceTypeCacheConfigurationReader {

	/**
	 * Reads the configuration for the resource requested in given request. The configuration is read from the type of
	 * the requested resource.
	 *
	 * @param request
	 * @return
	 */
	Optional<ResourceTypeCacheConfiguration> readComponentConfiguration(SlingHttpServletRequest request);

}
