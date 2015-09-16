package com.cognifide.cq.cache.expiry.guard;

import com.cognifide.cq.cache.cache.CacheHolder;
import com.cognifide.cq.cache.expiry.guard.action.DeleteAction;
import com.cognifide.cq.cache.expiry.guard.action.GuardAction;
import com.cognifide.cq.cache.model.ResourceTypeCacheConfiguration;
import java.util.Collections;
import java.util.regex.Pattern;
import org.apache.sling.api.SlingHttpServletRequest;

public class ExpiryGuard {

	private final String cacheName;

	private final String key;

	private final GuardAction guardAction;

	private Iterable<String> invalidationPathPrefixes;

	private Iterable<Pattern> invalidationPatterns;

	private boolean expired;

	ExpiryGuard(String cacheName, String key, GuardAction guardAction) {
		this.cacheName = cacheName;
		this.key = key;
		this.guardAction = guardAction;

		this.invalidationPathPrefixes = Collections.emptyList();
		this.invalidationPatterns = Collections.emptyList();
		this.expired = false;
	}

	public String getCacheName() {
		return cacheName;
	}

	public String getKey() {
		return key;
	}

	public void addInvlidatePathsFrom(ResourceTypeCacheConfiguration resourceTypeCacheConfiguration) {
		this.invalidationPathPrefixes = resourceTypeCacheConfiguration.getInvalidationPathPrefixes();
		this.invalidationPatterns = resourceTypeCacheConfiguration.getInvalidationPatterns();
	}

	public void onContentChange(String path) {
		invalidatePathPrefixes(path);
		invalidatePatterns(path);
		executeWhenExpired();
	}

	private void invalidatePathPrefixes(String path) {
		if (!expired) {
			for (String invalidationPathPrefix : invalidationPathPrefixes) {
				if (path.startsWith(invalidationPathPrefix)) {
					expired = true;
					break;
				}
			}
		}
	}

	private void invalidatePatterns(String path) {
		if (!expired) {
			for (Pattern invalidationPattern : invalidationPatterns) {
				if (invalidationPattern.matcher(path).find()) {
					expired = true;
					break;
				}
			}
		}
	}

	private void executeWhenExpired() {
		if (expired) {
			guardAction.execute();
		}
	}

	public static ExpiryGuard createDeletingExpiryGuard(SlingHttpServletRequest request, CacheHolder cacheHolder,
			ResourceTypeCacheConfiguration resourceTypeCacheConfiguration, String key) {
		final String cacheName = resourceTypeCacheConfiguration.getResourceType();
		ExpiryGuard expiryGuard = new ExpiryGuard(cacheName, key, new DeleteAction(cacheHolder, cacheName, key));

		resourceTypeCacheConfiguration.generateInvalidationPathsFor(request);
		expiryGuard.addInvlidatePathsFrom(resourceTypeCacheConfiguration);

		return expiryGuard;
	}
}
