package com.cognifide.cq.cache.algorithm;

import java.util.HashMap;
import java.util.Map;

import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.base.CacheEntry;
import com.opensymphony.oscache.base.events.CacheEntryEvent;
import com.opensymphony.oscache.base.events.CacheEntryEventListener;

/**
 * @author Bartosz Rudnicki
 */
public class SilentRemovalNotificator {

	private static final Map<Thread, String> silentlyRemovedEntries = new HashMap<Thread, String>();

	public static void registerSilentlyRemovedEntry(String key) {
		silentlyRemovedEntries.put(Thread.currentThread(), key);
	}

	public static void notifyListeners(Cache cache) {
		String key = silentlyRemovedEntries.remove(Thread.currentThread());
		if (key != null) {
			CacheEntry cacheEntry = new CacheEntry(key);
			CacheEntryEvent event = new CacheEntryEvent(cache, cacheEntry,
					SilentRemovalNotificator.class.getName());

			// Guaranteed to return a non-null array
			Object[] listeners = cache.getCacheEventListenerList().getListenerList();

			// Process the listeners last to first, notifying those that are interested in this event
			for (int i = listeners.length - 2; i >= 0; i -= 2) {
				if (listeners[i + 1] instanceof CacheEntryEventListener) {
					CacheEntryEventListener listener = (CacheEntryEventListener) listeners[i + 1];
					listener.cacheEntryRemoved(event);
				}
			}
		}
	}
}
