/*
 * Copyright 2015 Wunderman Thompson Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cognifide.cq.cache.cache.listener;

import java.io.ByteArrayOutputStream;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryEventFilter;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.EventType;

public class CacheGuardFilter implements CacheEntryEventFilter<String, ByteArrayOutputStream> {

	@Override
	public boolean evaluate(CacheEntryEvent<? extends String, ? extends ByteArrayOutputStream> event)
			throws CacheEntryListenerException {
		EventType eventType = event.getEventType();
		return EventType.EXPIRED.equals(eventType) || EventType.REMOVED.equals(eventType);
	}

}
