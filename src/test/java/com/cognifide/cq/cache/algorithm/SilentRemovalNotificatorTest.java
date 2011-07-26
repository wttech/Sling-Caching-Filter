package com.cognifide.cq.cache.algorithm;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.cognifide.cq.cache.test.utils.ReflectionHelper;
import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.base.events.CacheEntryEvent;
import com.opensymphony.oscache.base.events.CacheEntryEventListener;

/**
 * @author Bartosz Rudnicki
 */
public class SilentRemovalNotificatorTest {

	private static final String KEY_1 = "key1";

	private static final String KEY_2 = "key2";

	private static final String KEY_3 = "key3";

	private Map<Thread, String> silentlyRemovedEntries;

	@Before
	public void setUp() throws Exception {
		silentlyRemovedEntries = ReflectionHelper.get(SilentRemovalNotificator.class,
				"silentlyRemovedEntries");
		silentlyRemovedEntries.clear();
	}

	@After
	public void tearDown() {
		silentlyRemovedEntries.clear();
	}

	@Test
	public void testRegisterSilentlyRemovedEntry() throws InterruptedException {
		Thread thread1 = new Thread(new Runnable() {
			@Override
			public void run() {
				SilentRemovalNotificator.registerSilentlyRemovedEntry(KEY_1);
			}
		});

		Thread thread2 = new Thread(new Runnable() {
			@Override
			public void run() {
				SilentRemovalNotificator.registerSilentlyRemovedEntry(KEY_2);
			}
		});

		Thread thread3 = new Thread(new Runnable() {
			@Override
			public void run() {
				SilentRemovalNotificator.registerSilentlyRemovedEntry(KEY_3);
			}
		});

		assertTrue(silentlyRemovedEntries.isEmpty());

		thread1.start();
		thread1.join();

		assertEquals(1, silentlyRemovedEntries.size());
		assertEquals(KEY_1, silentlyRemovedEntries.get(thread1));

		thread2.start();
		thread3.start();

		thread2.join();
		thread3.join();

		assertEquals(3, silentlyRemovedEntries.size());
		assertEquals(KEY_1, silentlyRemovedEntries.get(thread1));
		assertEquals(KEY_2, silentlyRemovedEntries.get(thread2));
		assertEquals(KEY_3, silentlyRemovedEntries.get(thread3));
		assertNull(silentlyRemovedEntries.get(Thread.currentThread()));
	}

	@Test
	public void testNotifyListeners() {
		// set up mocks
		CacheEntryEventListener listener = createMock(CacheEntryEventListener.class);
		listener.cacheEntryRemoved((CacheEntryEvent) anyObject());
		replay(listener);

		// set up cache and notificator
		SilentRemovalNotificator.registerSilentlyRemovedEntry(KEY_1);
		Cache cache = new Cache(true, false, false);
		cache.addCacheEventListener(listener);

		// execute test
		SilentRemovalNotificator.notifyListeners(cache);

		// validate results
		verify(listener);
		assertTrue(silentlyRemovedEntries.isEmpty());
	}
}
