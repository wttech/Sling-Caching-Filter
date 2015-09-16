package com.cognifide.cq.cache.cache.listener;

import com.cognifide.cq.cache.cache.CacheEntity;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryEventFilter;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.EventType;

public class CacheGuardFilter implements CacheEntryEventFilter<String, CacheEntity> {

	@Override
	public boolean evaluate(CacheEntryEvent<? extends String, ? extends CacheEntity> event) throws CacheEntryListenerException {
		EventType eventType = event.getEventType();
		return EventType.EXPIRED.equals(eventType) || EventType.REMOVED.equals(eventType);
	}

}
