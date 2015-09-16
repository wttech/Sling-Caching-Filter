package com.cognifide.cq.cache.expiry.collection;

import com.cognifide.cq.cache.expiry.guard.ExpiryGuard;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;

@Service(value = {GuardCollectionWalker.class, GuardCollectionWatcher.class})
@Component(immediate = true)
public class GuardCollection implements GuardCollectionWalker, GuardCollectionWatcher {

	private ConcurrentMap<String, ConcurrentMap<String, ExpiryGuard>> guards;

	@Activate
	protected void activate() {
		guards = Maps.newConcurrentMap();
	}

	@Override
	public void addGuard(ExpiryGuard expiryGuard) {
		guards.putIfAbsent(expiryGuard.getCacheName(), Maps.<String, ExpiryGuard>newConcurrentMap());
		guards.get(expiryGuard.getCacheName()).put(expiryGuard.getKey(), expiryGuard);
	}

	@Override
	public Collection<ExpiryGuard> getGuards() {
		Set<ExpiryGuard> expiryGurads = Sets.newHashSet();
		for (Map.Entry<String, ConcurrentMap<String, ExpiryGuard>> entry : guards.entrySet()) {
			expiryGurads.addAll(entry.getValue().values());
		}
		return Collections.unmodifiableSet(expiryGurads);
	}

	@Override
	public void removeGuards(String cacheName) {
		guards.remove(cacheName);
	}

	@Override
	public void removeGuard(String cacheName, String key) {
		if (guards.containsKey(cacheName)) {
			guards.get(cacheName).remove(key);
		}
	}

	@Deactivate
	protected void deactivate() {
		guards.clear();
	}
}
