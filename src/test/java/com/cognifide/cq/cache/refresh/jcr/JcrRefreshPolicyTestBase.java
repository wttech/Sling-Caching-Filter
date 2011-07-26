package com.cognifide.cq.cache.refresh.jcr;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.cognifide.cq.cache.test.utils.ReflectionHelper;
import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.base.CacheEntry;
import com.opensymphony.oscache.base.events.CacheEntryEvent;
import com.opensymphony.oscache.base.events.CachePatternEvent;
import com.opensymphony.oscache.base.events.CachewideEvent;

/**
 * @author Bartosz Rudnicki
 */
public abstract class JcrRefreshPolicyTestBase<T extends JcrRefreshPolicy> {

	protected static final String INSTANCE_KEY = "instance key";

	protected static final String OTHER_KEY = "other key";

	protected static final String INSTANCE_INVALIDATE_PATH = "/home/invalidate/me";

	protected static final String OTHER_INVALIDATE_PATH = "/home/dont/invalidate/me";

	protected static final String[] INVALIDATE_PATHS = { "/home/invalidate.*" };

	protected static final int DEFAULT_REFRESH_TIME = 10;

	protected abstract T getNewRefreshPolicyInstance(String key, String[] paths);

	protected T refreshPolicy;

	protected List<JcrEventListener> listeners;

	@Before
	public final void setUp() throws Exception {
		refreshPolicy = getNewRefreshPolicyInstance(INSTANCE_KEY, INVALIDATE_PATHS);
		JcrEventsService.addEventListener(refreshPolicy);

		listeners = ReflectionHelper.get(JcrEventsService.class, "listeners");
		listeners.clear();
		JcrEventsService.addEventListener(refreshPolicy);
	}

	@After
	public final void tearDown() {
		JcrEventsService.removeEventListener(refreshPolicy);
		refreshPolicy = null;
	}

	private interface JcrEventListenerTestMethodExecutor {
		void executeMethod(CacheEntryEvent cacheEntryEvent);
	};

	private void jcrEventListenerTest(JcrEventListenerTestMethodExecutor executor) {
		assertEquals(1, listeners.size());

		Cache cache = createMock(Cache.class);
		CacheEntry cacheEntry = createMock(CacheEntry.class);
		CacheEntryEvent cacheEntryEvent = new CacheEntryEvent(cache, cacheEntry);

		expect(cacheEntry.getKey()).andReturn(OTHER_KEY);
		expect(cacheEntry.getKey()).andReturn(INSTANCE_KEY);
		cache.removeCacheEventListener(refreshPolicy);

		replay(cacheEntry, cache);

		executor.executeMethod(cacheEntryEvent);
		assertEquals(1, listeners.size());

		executor.executeMethod(cacheEntryEvent);

		verify(cacheEntry, cache);
		assertTrue(listeners.isEmpty());
	}

	@Test
	public void testCacheEntryAdded() {
		jcrEventListenerTest(new JcrEventListenerTestMethodExecutor() {
			@Override
			public void executeMethod(CacheEntryEvent cacheEntryEvent) {
				refreshPolicy.cacheEntryAdded(cacheEntryEvent);
			}
		});
	}

	@Test
	public void testCacheEntryFlushed() {
		jcrEventListenerTest(new JcrEventListenerTestMethodExecutor() {
			@Override
			public void executeMethod(CacheEntryEvent cacheEntryEvent) {
				refreshPolicy.cacheEntryFlushed(cacheEntryEvent);
			}
		});
	}

	@Test
	public void testCacheEntryRemoved() {
		jcrEventListenerTest(new JcrEventListenerTestMethodExecutor() {
			@Override
			public void executeMethod(CacheEntryEvent cacheEntryEvent) {
				refreshPolicy.cacheEntryRemoved(cacheEntryEvent);
			}
		});
	}

	@Test
	public void testCacheEntryUpdated() {
		jcrEventListenerTest(new JcrEventListenerTestMethodExecutor() {
			@Override
			public void executeMethod(CacheEntryEvent cacheEntryEvent) {
				refreshPolicy.cacheEntryUpdated(cacheEntryEvent);
			}
		});
	}

	@Test
	public void testCacheFlushed() {
		assertEquals(1, listeners.size());

		Cache cache = createMock(Cache.class);
		CacheEntry cacheEntry = createMock(CacheEntry.class);
		CachewideEvent cachewideEvent = new CachewideEvent(cache, new Date(), this.getClass().getName());

		cache.removeCacheEventListener(refreshPolicy);

		replay(cacheEntry, cache);
		refreshPolicy.cacheFlushed(cachewideEvent);
		verify(cacheEntry, cache);

		assertTrue(listeners.isEmpty());
	}

	@Test
	public void testCacheGroupFlushed() {
		// TODO
	}

	@Test
	public void testCachePatternFlushed() {
		assertEquals(1, listeners.size());

		Cache cache = createMock(Cache.class);
		CachePatternEvent cachePatternEventCorrectKey = new CachePatternEvent(cache, INSTANCE_KEY);
		CachePatternEvent cachePatternEventWrongKey = new CachePatternEvent(cache, OTHER_KEY);

		cache.removeCacheEventListener(refreshPolicy);

		replay(cache);

		refreshPolicy.cachePatternFlushed(cachePatternEventWrongKey);
		assertEquals(1, listeners.size());

		refreshPolicy.cachePatternFlushed(cachePatternEventCorrectKey);

		verify(cache);
		assertTrue(listeners.isEmpty());
	}

	@Test
	public void testContentChanged() {
		assertFalse(refreshPolicy.contentChanged(OTHER_INVALIDATE_PATH));
		assertTrue(refreshPolicy.contentChanged(INSTANCE_INVALIDATE_PATH));
		assertTrue(refreshPolicy.contentChanged(OTHER_INVALIDATE_PATH));
		assertTrue(refreshPolicy.contentChanged(INSTANCE_INVALIDATE_PATH));
	}

	@Test
	public void testGetSetRefreshPeriod() {
		refreshPolicy.setRefreshPeriod(-100);
		assertEquals(-100, refreshPolicy.getRefreshPeriod());
		refreshPolicy.setRefreshPeriod(100);
		assertEquals(100, refreshPolicy.getRefreshPeriod());
		refreshPolicy.setRefreshPeriod(0);
		assertEquals(0, refreshPolicy.getRefreshPeriod());
	}

	@Test
	public void testNeedsRefreshFromTime() {
		refreshPolicy.setRefreshPeriod(100L);
		CacheEntry cacheEntry = new CacheEntry(INSTANCE_KEY);
		cacheEntry.setLastUpdate(System.currentTimeMillis());

		assertFalse(refreshPolicy.needsRefresh(cacheEntry));

		cacheEntry.setLastUpdate(1L);
		assertTrue(refreshPolicy.needsRefresh(cacheEntry));
	}

	@Test
	public void testNeedsRefreshFromContentChanged() {
		refreshPolicy.setRefreshPeriod(100L);
		CacheEntry cacheEntry = new CacheEntry(INSTANCE_KEY);
		cacheEntry.setLastUpdate(System.currentTimeMillis());

		assertFalse(refreshPolicy.needsRefresh(cacheEntry));
		assertFalse(refreshPolicy.contentChanged(OTHER_INVALIDATE_PATH));
		assertFalse(refreshPolicy.needsRefresh(cacheEntry));
		assertTrue(refreshPolicy.contentChanged(INSTANCE_INVALIDATE_PATH));
		assertTrue(refreshPolicy.needsRefresh(cacheEntry));
	}
}
