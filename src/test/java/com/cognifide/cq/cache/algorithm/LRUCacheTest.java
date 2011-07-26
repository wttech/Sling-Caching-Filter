package com.cognifide.cq.cache.algorithm;

import com.opensymphony.oscache.base.algorithm.AbstractConcurrentReadCache;

/**
 * @author Bartosz Rudnicki
 */
public class LRUCacheTest extends CacheTestBase {

	@Override
	protected AbstractConcurrentReadCache getNewCacheInstance() {
		return new LRUCache();
	}

}
