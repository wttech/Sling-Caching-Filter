package com.cognifide.cq.cache.cache;

import com.google.common.base.Function;
import javax.cache.Cache;

public class EntryToKeyTransform implements Function<Cache.Entry<String, CacheEntity>, String> {

	@Override
	public String apply(Cache.Entry<String, CacheEntity> input) {
		return input.getKey();
	}
}
