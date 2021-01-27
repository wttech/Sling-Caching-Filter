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
package com.cognifide.cq.cache.cache.listener;

import com.cognifide.cq.cache.expiry.collection.GuardCollectionWatcher;
import java.io.ByteArrayOutputStream;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryExpiredListener;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryRemovedListener;

public class CacheGaurdListener implements CacheEntryExpiredListener<String, ByteArrayOutputStream>,
		CacheEntryRemovedListener<String, ByteArrayOutputStream> {

	private final GuardCollectionWatcher guardCollectionWatcher;

	public CacheGaurdListener(GuardCollectionWatcher guardCollectionWatcher) {
		this.guardCollectionWatcher = guardCollectionWatcher;
	}

	@Override
	public void onExpired(Iterable<CacheEntryEvent<? extends String, ? extends ByteArrayOutputStream>> events)
			throws CacheEntryListenerException {
		removeGuards(events);
	}

	private void removeGuards(Iterable<CacheEntryEvent<? extends String, ? extends ByteArrayOutputStream>> events) {
		for (CacheEntryEvent<? extends String, ? extends ByteArrayOutputStream> event : events) {
			this.guardCollectionWatcher.removeGuard(event.getSource().getName(), event.getKey());
		}
	}

	@Override
	public void onRemoved(Iterable<CacheEntryEvent<? extends String, ? extends ByteArrayOutputStream>> events)
			throws CacheEntryListenerException {
		removeGuards(events);
	}

}
