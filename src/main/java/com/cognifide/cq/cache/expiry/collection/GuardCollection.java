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
package com.cognifide.cq.cache.expiry.collection;

import com.cognifide.cq.cache.expiry.guard.ExpiryGuard;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;

@Service(value = {GuardCollectionWalker.class, GuardCollectionWatcher.class})
@Component(immediate = true)
public class GuardCollection implements GuardCollectionWalker, GuardCollectionWatcher {

	private Map<String, Map<String, ExpiryGuard>> guards;

	@Activate
	protected void activate() {
		guards = new ConcurrentHashMap<String, Map<String, ExpiryGuard>>();
	}

	@Override
	public void addGuard(ExpiryGuard expiryGuard) {
		Map<String, ExpiryGuard> entry = guards.get(expiryGuard.getCacheName());
		if (null == entry) {
			entry = new ConcurrentHashMap<String, ExpiryGuard>();
			guards.put(expiryGuard.getCacheName(), entry);
		}
		entry.put(expiryGuard.getKey(), expiryGuard);
	}

	@Override
	public Collection<ExpiryGuard> getGuards() {
		Set<ExpiryGuard> expiryGurads = new HashSet<ExpiryGuard>();
		for (Map.Entry<String, Map<String, ExpiryGuard>> entry : guards.entrySet()) {
			expiryGurads.addAll(entry.getValue().values());
		}
		return expiryGurads;
	}

	@Override
	public void removeGuards(String cacheName) {
		guards.remove(cacheName);
	}

	@Override
	public void removeGuard(String cacheName, String key) {
		Map<String, ExpiryGuard> entry = guards.get(cacheName);
		if (null != entry) {
			entry.remove(key);
		}
	}

	@Override
	public void clearGarnison() {
		guards.clear();
	}

	@Deactivate
	protected void deactivate() {
		clearGarnison();
	}
}
