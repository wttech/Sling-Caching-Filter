package com.cognifide.cq.cache.tag;

import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.base.EntryRefreshPolicy;
import com.opensymphony.oscache.base.events.CacheEventListener;

/**
 * @author Bartosz Rudnicki
 */
public class CacheStub extends Cache {

	private static final long serialVersionUID = 6404033247592911128L;

	private CacheEventListener cacheEventListener;

	private String key;

	private Object content;

	private EntryRefreshPolicy refreshPolicy;

	private boolean addCacheEventListenerExecuted = false;

	private boolean putInCacheExecuted = false;

	public CacheStub() {
		super(true, false, false);
	}

	@Override
	public void addCacheEventListener(CacheEventListener listener) {
		cacheEventListener = listener;
		addCacheEventListenerExecuted = true;
	}

	@Override
	public void putInCache(String key, Object content, EntryRefreshPolicy policy) {
		putInCacheExecuted = true;
		this.key = key;
		this.content = content;
		this.refreshPolicy = policy;
	}

	public CacheEventListener getCacheEventListener() {
		return cacheEventListener;
	}

	public String getKey() {
		return key;
	}

	public Object getContent() {
		return content;
	}

	public EntryRefreshPolicy getRefreshPolicy() {
		return refreshPolicy;
	}

	public boolean isAddCacheEventListenerExecuted() {
		return addCacheEventListenerExecuted;
	}

	public boolean isPutInCacheExecuted() {
		return putInCacheExecuted;
	}

}
