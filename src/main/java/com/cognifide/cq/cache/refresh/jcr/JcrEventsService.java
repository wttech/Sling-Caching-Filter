package com.cognifide.cq.cache.refresh.jcr;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import org.apache.commons.lang.StringUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.jcr.api.SlingRepository;

@Component(immediate = true)
@Service(JcrEventsService.class)
public class JcrEventsService implements EventListener {

	private static final Log LOG = LogFactory.getLog(JcrEventsService.class);

	public static final int ALL_TYPES = Event.NODE_ADDED | Event.NODE_REMOVED | Event.PROPERTY_ADDED
			| Event.PROPERTY_CHANGED | Event.PROPERTY_REMOVED;

	private final List<JcrEventListener> listeners = new CopyOnWriteArrayList<JcrEventListener>();

	private Session admin;

	/**
	 * Sling repo.
	 *
	 * @scr.reference
	 */
	@Reference
	private SlingRepository repository;

	public void addEventListener(JcrEventListener listener) {
		listeners.add(listener);
	}

	public void removeEventListener(JcrEventListener listener) {
		listeners.remove(listener);
	}

	public void clearEventListeners() {
		listeners.clear();
	}

	/**
	 * Handles OSGi activation.
	 *
	 * @throws javax.jcr.RepositoryException
	 */
	@Activate
	public void activate() throws RepositoryException {
		LOG.info("Activating Sling Caching Filter jcr events service");
		this.admin = repository.loginAdministrative(null);
		this.admin.getWorkspace().getObservationManager()
				.addEventListener(this, ALL_TYPES, "/", true, null, null, false);
	}

	/**
	 * Handles OSGi deactivation.
	 *
	 */
	@Deactivate
	public void deactivate() {
		if (admin != null) {
			try {
				admin.getWorkspace().getObservationManager().removeEventListener(this);
			} catch (RepositoryException repositoryException) {
				LOG.error("exception during removing listener", repositoryException);
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

				path = getRidOdDuplicateEvents(path);
				if (!lastPath.equals(path)) {
					lastPath = path;
					if (path.startsWith("/content") || path.startsWith("/apps")) {
						LOG.info("content changed: " + path);
						for (JcrEventListener eventListener : listeners) {
							if (eventListener != null) {
								eventListener.contentChanged(path);
							}
						}
					}
				}

			} catch (RepositoryException e) {
				LOG.error("Error occured while processing event", e);
			}
		}
	}

	private String getRidOdDuplicateEvents(String path) {
		return StringUtils.substringBefore(path, "/jcr:content");
	}

}
