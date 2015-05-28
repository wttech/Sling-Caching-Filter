/*
 * Copyright 2015 Cognifide Polska Sp. z o. o..
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
package com.cognifide.cq.cache.expiry.osgi;

import com.cognifide.cq.cache.expiry.collection.GuardCollectionWalker;
import com.cognifide.cq.cache.expiry.guard.ExpiryGuard;
import java.util.HashSet;
import java.util.Set;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingConstants;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service(value = {EventHandler.class})
@Component(immediate = true)
@Properties({
	@Property(name = EventConstants.EVENT_TOPIC, value = {SlingConstants.TOPIC_RESOURCE_ADDED, SlingConstants.TOPIC_RESOURCE_CHANGED, SlingConstants.TOPIC_RESOURCE_REMOVED}),
	@Property(name = EventConstants.EVENT_FILTER, value = "(|(path=/content/*)(path=/apps/*))")
})
public class OsgiEventsService implements EventHandler {

	private static final Logger logger = LoggerFactory.getLogger(OsgiEventsService.class);

	@Reference
	private GuardCollectionWalker garnisonWalker;

	@Override
	public void handleEvent(Event event) {
		String path = readPathFrom(event);

		if (logger.isInfoEnabled()) {
			logger.info("Processing {}", path);
		}

		Set<ExpiryGuard> expiryGuardsToRemove = new HashSet<ExpiryGuard>();
		for (ExpiryGuard expiryGuard : garnisonWalker.getGuards()) {
			expiryGuard.onContentChange(path);
			if (expiryGuard.isExpired()) {
				expiryGuardsToRemove.add(expiryGuard);
			}
		}

		removeExpiredCacheElementsAndGaurds(expiryGuardsToRemove);
	}

	private String readPathFrom(Event event) {
		return (String) event.getProperty(SlingConstants.PROPERTY_PATH);
	}

	private void removeExpiredCacheElementsAndGaurds(Set<ExpiryGuard> expiryGuardsToRemove) {
		for (ExpiryGuard expiryGuard : expiryGuardsToRemove) {
			expiryGuard.executeWhenExpired();
		}
	}
}
