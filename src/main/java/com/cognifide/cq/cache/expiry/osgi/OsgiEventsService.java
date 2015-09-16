package com.cognifide.cq.cache.expiry.osgi;

import com.cognifide.cq.cache.expiry.collection.GuardCollectionWalker;
import com.cognifide.cq.cache.expiry.guard.ExpiryGuard;
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
	@Property(
			name = EventConstants.EVENT_TOPIC,
			value = {
				SlingConstants.TOPIC_RESOURCE_ADDED,
				SlingConstants.TOPIC_RESOURCE_CHANGED,
				SlingConstants.TOPIC_RESOURCE_REMOVED}),
	@Property(name = EventConstants.EVENT_FILTER, value = "(|(path=/content/*)(path=/apps/*))")
})
public class OsgiEventsService implements EventHandler {

	private static final Logger logger = LoggerFactory.getLogger(OsgiEventsService.class);

	@Reference
	private GuardCollectionWalker collectionWalker;

	@Override
	public void handleEvent(Event event) {
		String path = readPathFrom(event);

		if (logger.isDebugEnabled()) {
			logger.debug("Processing {}", path);
		}

		for (ExpiryGuard expiryGuard : collectionWalker.getGuards()) {
			expiryGuard.onContentChange(path);
		}
	}

	private String readPathFrom(Event event) {
		return (String) event.getProperty(SlingConstants.PROPERTY_PATH);
	}
}
