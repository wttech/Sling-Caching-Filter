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

import com.cognifide.cq.cache.expiry.guard.action.GuardAction;
import com.cognifide.cq.cache.expiry.guard.action.DeleteAction;
import com.cognifide.cq.cache.cache.CacheHolder;
import com.cognifide.cq.cache.model.ResourceTypeCacheConfiguration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ExpiryGuard {

	public static ExpiryGuard createDeletingExpiryGuard(CacheHolder cacheHolder, ResourceTypeCacheConfiguration resourceTypeCacheConfiguration, String key) {
		final String cacheName = resourceTypeCacheConfiguration.getResourceType();
		ExpiryGuard expiryGuard = new ExpiryGuard(cacheName, key, new DeleteAction(cacheHolder, cacheName, key));
		expiryGuard.addInvlidatePathsFrom(resourceTypeCacheConfiguration);
		return expiryGuard;
	}

	private final String cacheName;

	private final String key;

	private final GuardAction guardAction;

	private final List<Pattern> invalidatePaths;

	private boolean expired;

	ExpiryGuard(String cacheName, String key, GuardAction guardAction) {
		this.cacheName = cacheName;
		this.key = key;
		this.guardAction = guardAction;

		this.invalidatePaths = new ArrayList<Pattern>(8);
		this.expired = false;
	}

	public String getCacheName() {
		return cacheName;
	}

	public String getKey() {
		return key;
	}

	public void addInvlidatePathsFrom(ResourceTypeCacheConfiguration resourceTypeCacheConfiguration) {
		this.invalidatePaths.addAll(resourceTypeCacheConfiguration.getInvalidatePaths());
		this.invalidatePaths.add(Pattern.compile(resourceTypeCacheConfiguration.getResourceTypePath() + ".*"));
	}

	public void onContentChange(String path) {
		if (!expired) {
			invalidate(path);
		}
	}

	private void invalidate(String path) {
		for (Pattern invalidatePath : invalidatePaths) {
			if (invalidatePath.matcher(path).matches()) {
				expired = true;
				break;
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
