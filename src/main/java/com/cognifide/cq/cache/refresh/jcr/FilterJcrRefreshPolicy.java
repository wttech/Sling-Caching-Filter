package com.cognifide.cq.cache.refresh.jcr;

import java.util.regex.Pattern;

import com.cognifide.cq.cache.model.ResourceTypeCacheConfiguration;

/**
 * @author Bartosz Rudnicki
 */
public class FilterJcrRefreshPolicy extends JcrRefreshPolicy {

	private static final long serialVersionUID = -8888494251459418367L;

	public FilterJcrRefreshPolicy(JcrEventsService jcrEventsService, String cacheEntryKey, ResourceTypeCacheConfiguration configuration) {
		super(jcrEventsService, cacheEntryKey, configuration.getTime());
		this.invalidatePaths.addAll(configuration.getInvalidatePaths());
		this.invalidatePaths.add(Pattern.compile(configuration.getResourceTypePath() + ".*"));
	}

}
