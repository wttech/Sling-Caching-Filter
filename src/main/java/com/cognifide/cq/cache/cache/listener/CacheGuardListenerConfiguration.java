package com.cognifide.cq.cache.cache.listener;

import com.cognifide.cq.cache.cache.CacheEntity;
import com.cognifide.cq.cache.expiry.collection.GuardCollectionWatcher;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Factory;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.event.CacheEntryEventFilter;
import javax.cache.event.CacheEntryListener;

public class CacheGuardListenerConfiguration implements CacheEntryListenerConfiguration<String, CacheEntity> {

	private static final long serialVersionUID = 1L;

	private final GuardCollectionWatcher guardCollectionWatcher;

	public CacheGuardListenerConfiguration(GuardCollectionWatcher guardCollectionWatcher) {
		this.guardCollectionWatcher = guardCollectionWatcher;
	}

	@Override
	public Factory<CacheEntryListener<? super String, ? super CacheEntity>> getCacheEntryListenerFactory() {
		return new FactoryBuilder.SingletonFactory<CacheEntryListener<? super String, ? super CacheEntity>>(new CacheGaurdListener(guardCollectionWatcher));
	}

	@Override
	public boolean isOldValueRequired() {
		return false;
	}

	@Override
	public Factory<CacheEntryEventFilter<? super String, ? super CacheEntity>> getCacheEntryEventFilterFactory() {
		return new FactoryBuilder.SingletonFactory<CacheEntryEventFilter<? super String, ? super CacheEntity>>(new CacheGuardFilter());
	}

	@Override
	public boolean isSynchronous() {
		return false;
	}

}
