package com.cognifide.cq.cache.expiry.collection;

import com.cognifide.cq.cache.expiry.guard.ExpiryGuard;

public interface GuardCollectionWatcher {

	void addGuard(ExpiryGuard expiryGuard);

	void removeGuards(String cacheName);

	void removeGuard(String cacheName, String key);
}
