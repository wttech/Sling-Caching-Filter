package com.cognifide.cq.cache.refresh.jcr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.ComponentContext;

/**
 * Based on the ServiceFinderImpl created by IntelliJ IDEA. User: stas Date: Feb 17, 2009 Time: 10:57:16 AM
 * 
 * @scr.component immediate="true" label="Jcr Events Service "
 * description="Jcr events service for the cache bundle."
 * @scr.property name="service.vendor" value="Cognifide"
 * 
 * @scr.service
 */
public class JcrEventsService implements EventListener {

	private static final Log log = LogFactory.getLog(JcrEventsService.class);

	private static final List<JcrEventListener> listeners = Collections
			.synchronizedList(new ArrayList<JcrEventListener>());

	public static void addEventListener(JcrEventListener listener) {
		listeners.add(listener);
	}

	public static void removeEventListener(JcrEventListener listener) {
		listeners.remove(listener);
	}

	public static void clearEventListeners() {
		listeners.clear();
	}

	private Session admin;

	/**
	 * Sling repo.
	 * 
	 * @scr.reference
	 */
	private SlingRepository repository;

	/**
	 * Handles OSGi activation.
	 * 
	 * @param context OSGi component context
	 */
	protected void activate(ComponentContext context) throws RepositoryException {
		log.info("activate");
		this.admin = repository.loginAdministrative(null);
		this.admin
				.getWorkspace()
				.getObservationManager()
				.addEventListener(this, org.apache.jackrabbit.spi.Event.ALL_TYPES, "/", true, null, null,
						false);
	}

	/**
	 * Handles OSGi deactivation.
	 * 
	 * @param context OSGi component context
	 */
	protected void deactivate(ComponentContext context) {
		if (admin != null) {
			try {
				admin.getWorkspace().getObservationManager().removeEventListener(this);
			} catch (RepositoryException repositoryException) {
				log.error("exception during removing listener", repositoryException);
			}
			admin.logout();
			admin = null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onEvent(EventIterator eventIterator) {
		String lastPath = "";
		while (eventIterator.hasNext()) {
			Event event = eventIterator.nextEvent();
			try {
				String path = event.getPath();

				// all of this to only get rid of 'duplicate' events
				int index = path.indexOf("/jcr:content");
				if (index > 0) {
					path = path.substring(0, index);
				}
				if (lastPath.equals(path)) {
					continue;
				} else {
					lastPath = path;
				}

				if (path.startsWith("/content") || path.startsWith("/apps")) {
					log.info("content changed: " + path);
					for (JcrEventListener eventListener : listeners) {
						if (eventListener != null) {
							eventListener.contentChanged(path);
						}
					}
				}
			} catch (RepositoryException e) {
				log.error("Error occured while processing event", e);
			}
		}
	}

}