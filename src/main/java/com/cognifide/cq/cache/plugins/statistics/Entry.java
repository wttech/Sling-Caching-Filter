package com.cognifide.cq.cache.plugins.statistics;

import com.cognifide.cq.cache.filter.cache.action.CacheAction;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicLong;

public class Entry {

	private final AtomicLong misses;

	private final AtomicLong hits;

	private final CopyOnWriteArraySet<String> keys;

	private List<CacheAction> cacheActions;

	public Entry() {
		this.misses = new AtomicLong();
		this.hits = new AtomicLong();
		this.keys = new CopyOnWriteArraySet<String>();
		this.cacheActions = new CopyOnWriteArrayList<CacheAction>();
	}

	public long getMisses() {
		return misses.longValue();
	}

	public long getHits() {
		return hits.longValue();
	}

	public void addKey(String key) {
		this.keys.add(key);
	}

	public void addCacheAction(CacheAction cacheAction) {
		this.cacheActions.add(cacheAction);
	}

	public void cacheMiss() {
		misses.addAndGet(1);
	}

	public void cacheHit() {
		hits.addAndGet(1);
	}

	public void executeCacheActions() {
		for (CacheAction cacheAction : cacheActions) {
			cacheAction.execute();
		}
	}

	public Set<String> getKeys() {
		return Collections.unmodifiableSet(keys);
	}
}
