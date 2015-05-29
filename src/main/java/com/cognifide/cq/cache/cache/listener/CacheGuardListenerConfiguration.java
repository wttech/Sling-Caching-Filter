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
package com.cognifide.cq.cache.cache.listener;

import com.cognifide.cq.cache.expiry.collection.GuardCollectionWatcher;
import java.io.ByteArrayOutputStream;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Factory;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.event.CacheEntryEventFilter;
import javax.cache.event.CacheEntryListener;

public class CacheGuardListenerConfiguration implements CacheEntryListenerConfiguration<String, ByteArrayOutputStream> {

	private static final long serialVersionUID = 1L;

	private final GuardCollectionWatcher guardCollectionWatcher;

	public CacheGuardListenerConfiguration(GuardCollectionWatcher guardCollectionWatcher) {
		this.guardCollectionWatcher = guardCollectionWatcher;
	}

	@Override
	public Factory<CacheEntryListener<? super String, ? super ByteArrayOutputStream>> getCacheEntryListenerFactory() {
		return new FactoryBuilder.SingletonFactory<CacheEntryListener<? super String, ? super ByteArrayOutputStream>>(
				new CacheGaurdListener(guardCollectionWatcher));
	}

	@Override
	public boolean isOldValueRequired() {
		return false;
	}

	@Override
	public Factory<CacheEntryEventFilter<? super String, ? super ByteArrayOutputStream>> getCacheEntryEventFilterFactory() {
		return new FactoryBuilder.SingletonFactory<CacheEntryEventFilter<? super String, ? super ByteArrayOutputStream>>(
				new CacheGuardFilter());
	}

	@Override
	public boolean isSynchronous() {
		return false;
	}

}
