/*
 * Copyright 2015 Wunderman Thompson Technology
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
package com.cognifide.cq.cache.expiry.guard.action;

import com.cognifide.cq.cache.cache.CacheHolder;

public class DeleteAction implements GuardAction {

	private final CacheHolder cacheHolder;

	private final String cacheName;

	private final String key;

	public DeleteAction(CacheHolder cacheHolder, String cacheName, String key) {
		this.cacheHolder = cacheHolder;
		this.cacheName = cacheName;
		this.key = key;
	}

	@Override
	public void execute() {
		cacheHolder.remove(cacheName, key);
	}
}
