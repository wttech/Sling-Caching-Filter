/*
 * Copyright 2015 Cognifide Polska Sp. z o. o..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cognifide.cq.cache.expiry.guard;

import com.cognifide.cq.cache.cache.CacheHolder;
import com.cognifide.cq.cache.expiry.guard.action.DeleteAction;
import com.cognifide.cq.cache.expiry.guard.action.GuardAction;
import com.cognifide.cq.cache.model.ResourceTypeCacheConfiguration;
import java.util.ArrayList;
import java.util.regex.Pattern;
import org.apache.sling.api.SlingHttpServletRequest;

public class ExpiryGuard {

	public static ExpiryGuard createDeletingExpiryGuard(SlingHttpServletRequest request, CacheHolder cacheHolder,
			ResourceTypeCacheConfiguration resourceTypeCacheConfiguration, String key) {
		final String cacheName = resourceTypeCacheConfiguration.getResourceType();
		ExpiryGuard expiryGuard = new ExpiryGuard(cacheName, key, new DeleteAction(cacheHolder, cacheName, key));

		resourceTypeCacheConfiguration.generateInvalidationPathsFor(request);
		expiryGuard.addInvlidatePathsFrom(resourceTypeCacheConfiguration);

		return expiryGuard;
	}

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

		this.invalidationPathPrefixes = new ArrayList<String>(8);
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

	public boolean isExpired() {
		return expired;
	}

	public void executeWhenExpired() {
		if (isExpired()) {
			guardAction.execute();
		}
	}
}
