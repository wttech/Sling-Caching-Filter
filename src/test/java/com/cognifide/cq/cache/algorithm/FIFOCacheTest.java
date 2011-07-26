package com.cognifide.cq.cache.algorithm;

import com.opensymphony.oscache.base.algorithm.AbstractConcurrentReadCache;

/**
 * @author Bartosz Rudnicki
 */
public class FIFOCacheTest extends CacheTestBase {

	@Override
	protected AbstractConcurrentReadCache getNewCacheInstance() {
		return new FIFOCache();
	}

}
