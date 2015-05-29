package com.cognifide.cq.cache.expiry.collection;

import com.cognifide.cq.cache.expiry.guard.ExpiryGuard;

/**
 * Hold collection of guards.
 * Instances are thread safe.
 */
public interface GuardCollectionWatcher {

	/**
	 * Adds expiry guard to underling collection
	 * @param expiryGuard
	 */
	void addGuard(ExpiryGuard expiryGuard);

	/**
	 * Removes all guards that have given cache name
	 * @param cacheName
	 */
	void removeGuards(String cacheName);

	/**
	 * Removes guard that has given cache name and key
	 * @param cacheName
	 * @param key
	 */
	void removeGuard(String cacheName, String key);
}
