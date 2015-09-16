package com.cognifide.cq.cache.cache.listener;

import com.cognifide.cq.cache.cache.CacheEntity;
import com.cognifide.cq.cache.expiry.collection.GuardCollectionWatcher;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryExpiredListener;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryRemovedListener;

public class CacheGaurdListener implements CacheEntryExpiredListener<String, CacheEntity>, CacheEntryRemovedListener<String, CacheEntity> {

	private final GuardCollectionWatcher guardCollectionWatcher;

	public CacheGaurdListener(GuardCollectionWatcher guardCollectionWatcher) {
		this.guardCollectionWatcher = guardCollectionWatcher;
	}

	@Override
	public void onExpired(Iterable<CacheEntryEvent<? extends String, ? extends CacheEntity>> events) throws CacheEntryListenerException {
		removeGuards(events);
	}

	private void removeGuards(Iterable<CacheEntryEvent<? extends String, ? extends CacheEntity>> events) {
		for (CacheEntryEvent<? extends String, ? extends CacheEntity> event : events) {
			this.guardCollectionWatcher.removeGuard(event.getSource().getName(), event.getKey());
		}
	}

	@Override
	public void onRemoved(Iterable<CacheEntryEvent<? extends String, ? extends CacheEntity>> events) throws CacheEntryListenerException {
		removeGuards(events);
	}

}
