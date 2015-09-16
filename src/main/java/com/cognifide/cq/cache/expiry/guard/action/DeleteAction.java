package com.cognifide.cq.cache.expiry.guard.action;

import com.cognifide.cq.cache.cache.CacheHolder;
import com.google.common.base.Preconditions;

public class DeleteAction implements GuardAction {

	private final CacheHolder cacheHolder;

	private final String cacheName;

	private final String key;

	public DeleteAction(CacheHolder cacheHolder, String cacheName, String key) {
		this.cacheHolder = Preconditions.checkNotNull(cacheHolder);
		this.cacheName = Preconditions.checkNotNull(cacheName);
		this.key = Preconditions.checkNotNull(key);
	}

	@Override
	public void execute() {
		cacheHolder.remove(cacheName, key);
	}
}
