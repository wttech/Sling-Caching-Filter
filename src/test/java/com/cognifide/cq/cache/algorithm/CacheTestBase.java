package com.cognifide.cq.cache.algorithm;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.cognifide.cq.cache.test.utils.ReflectionHelper;
import com.opensymphony.oscache.base.algorithm.AbstractConcurrentReadCache;

/**
 * @author Bartosz Rudnicki
 */
public abstract class CacheTestBase {

	private static final String KEY_1 = "key1";

	private static final String KEY_2 = "key2";

	private static final Object VALUE_1 = new Object();

	private static final Object VALUE_2 = new Object();

	private AbstractConcurrentReadCache cache;

	private Map<Thread, String> silentlyRemovedEntries;

	protected abstract AbstractConcurrentReadCache getNewCacheInstance();

	@Before
	public void setUp() throws Exception {
		cache = getNewCacheInstance();

		silentlyRemovedEntries = ReflectionHelper.get(SilentRemovalNotificator.class,
				"silentlyRemovedEntries");
		silentlyRemovedEntries.clear();
	}

	@After
	public void tearDown() {
		if (silentlyRemovedEntries != null) {
			silentlyRemovedEntries.clear();
		}
	}

	@Test
	public void testRemoveItem() {
		cache.setMaxEntries(1);

		cache.put(KEY_1, VALUE_1);
		assertTrue(silentlyRemovedEntries.isEmpty());

		cache.put(KEY_2, VALUE_2);
		assertEquals(1, silentlyRemovedEntries.size());
		assertEquals(KEY_1, silentlyRemovedEntries.get(Thread.currentThread()));
	}

	@Test
	public void testItemRemoved() {
		cache.setMaxEntries(Integer.MAX_VALUE);
		cache.put(KEY_1, VALUE_1);
		cache.put(KEY_2, VALUE_2);

		assertTrue(silentlyRemovedEntries.isEmpty());
		cache.remove(KEY_1);
		assertEquals(1, silentlyRemovedEntries.size());
		assertEquals(KEY_1, silentlyRemovedEntries.get(Thread.currentThread()));
		silentlyRemovedEntries.clear();

		cache.remove(VALUE_1);
		assertTrue(silentlyRemovedEntries.isEmpty());

		cache.remove(KEY_1);
		assertTrue(silentlyRemovedEntries.isEmpty());

		cache.remove(KEY_2);
		assertEquals(1, silentlyRemovedEntries.size());
		assertEquals(KEY_2, silentlyRemovedEntries.get(Thread.currentThread()));
		silentlyRemovedEntries.clear();

		cache.remove(VALUE_2);
		assertTrue(silentlyRemovedEntries.isEmpty());
	}
}
