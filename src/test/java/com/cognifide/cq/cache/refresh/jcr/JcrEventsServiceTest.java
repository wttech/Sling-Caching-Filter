package com.cognifide.cq.cache.refresh.jcr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.ObservationManager;

import org.apache.sling.jcr.api.SlingRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.component.ComponentContext;

import com.cognifide.cq.cache.test.utils.ReflectionHelper;

/**
 * @author Bartosz Rudnicki
 */
public class JcrEventsServiceTest {

	private static final String PATH_1 = "/content/home/page/changed";

	private static final String PATH_2 = "/content/home/page/changed/jcr:content";

	private static final String PATH_3 = "/apps/other/page/changed";

	private static final String PATH_4 = "/libs/other/page/changed";

	private JcrEventsService service;

	private List<JcrEventListener> listeners;

	@Before
	public void setUp() throws Exception {
		service = new JcrEventsService();
		JcrEventsService.clearEventListeners();
		listeners = ReflectionHelper.get(JcrEventsService.class, "listeners");
	}

	@After
	public void tearDown() {
		JcrEventsService.clearEventListeners();
	}

	@Test
	public void testAddRemoveEventListener() {
		assertTrue(listeners.isEmpty());

		JcrEventListener listener1 = createMock(JcrEventListener.class);
		JcrEventListener listener2 = createMock(JcrEventListener.class);

		JcrEventsService.addEventListener(listener1);
		assertEquals(1, listeners.size());
		assertEquals(listener1, listeners.get(0));

		JcrEventsService.addEventListener(listener2);
		assertEquals(2, listeners.size());
		assertEquals(listener2, listeners.get(1));

		JcrEventsService.removeEventListener(listener1);
		assertEquals(1, listeners.size());
		assertEquals(listener2, listeners.get(0));

		JcrEventsService.removeEventListener(listener1);
		assertEquals(1, listeners.size());
		assertEquals(listener2, listeners.get(0));

		JcrEventsService.removeEventListener(listener2);
		assertTrue(listeners.isEmpty());
	}

	@Test
	public void testClearEventListeners() {
		assertTrue(listeners.isEmpty());

		JcrEventListener listener1 = createMock(JcrEventListener.class);
		JcrEventListener listener2 = createMock(JcrEventListener.class);

		JcrEventsService.addEventListener(listener1);
		JcrEventsService.addEventListener(listener2);

		assertEquals(2, listeners.size());

		JcrEventsService.clearEventListeners();

		assertTrue(listeners.isEmpty());
	}

	@Test
	public void testOnEvent() throws RepositoryException {
		EventIterator eventIterator = createMock(EventIterator.class);
		Event event1 = createMock(Event.class);
		Event event2 = createMock(Event.class);
		Event event3 = createMock(Event.class);
		Event event4 = createMock(Event.class);
		JcrEventListener listener = createMock(JcrEventListener.class);
		JcrEventsService.addEventListener(listener);

		expect(eventIterator.hasNext()).andReturn(true);
		expect(eventIterator.nextEvent()).andReturn(event1);
		expect(event1.getPath()).andReturn(PATH_1);
		expect(listener.contentChanged(PATH_1)).andReturn(false);

		expect(eventIterator.hasNext()).andReturn(true);
		expect(eventIterator.nextEvent()).andReturn(event2);
		expect(event2.getPath()).andReturn(PATH_2);

		expect(eventIterator.hasNext()).andReturn(true);
		expect(eventIterator.nextEvent()).andReturn(event3);
		expect(event3.getPath()).andReturn(PATH_3);
		expect(listener.contentChanged(PATH_3)).andReturn(false);

		expect(eventIterator.hasNext()).andReturn(true);
		expect(eventIterator.nextEvent()).andReturn(event4);
		expect(event4.getPath()).andReturn(PATH_4);

		expect(eventIterator.hasNext()).andReturn(false);

		replay(eventIterator, event1, event2, event3, event4, listener);
		service.onEvent(eventIterator);
		verify(eventIterator, event1, event2, event3, event4, listener);
	}

	@Test
	public void testActivate() throws IllegalAccessException, RepositoryException, InvocationTargetException,
			NoSuchFieldException, NoSuchMethodException {
		SlingRepository repository = createMock(SlingRepository.class);
		Session session = createMock(Session.class);
		Workspace workspace = createMock(Workspace.class);
		ObservationManager observationManager = createMock(ObservationManager.class);

		ReflectionHelper.set(JcrEventsService.class, "repository", service, repository);

		expect(repository.loginAdministrative(null)).andReturn(session);
		expect(session.getWorkspace()).andReturn(workspace);
		expect(workspace.getObservationManager()).andReturn(observationManager);
		observationManager
				.addEventListener(service, JcrEventsService.ALL_TYPES, "/", true, null, null, false);

		replay(repository, session, workspace, observationManager);
		ReflectionHelper.invoke(JcrEventsService.class, "activate",
				new Class<?>[] { ComponentContext.class }, service, new Object[] { null });
		verify(repository, session, workspace, observationManager);
	}

	@Test
	public void testNullAdminDeactivate() throws Exception {
		ReflectionHelper.set(JcrEventsService.class, "admin", service, null);
		ReflectionHelper.invoke(JcrEventsService.class, "deactivate",
				new Class<?>[] { ComponentContext.class }, service, new Object[] { null });
	}

	@Test
	public void testNotNullAdminDeactivate() throws Exception {
		Session admin = createMock(Session.class);
		Workspace workspace = createMock(Workspace.class);
		ObservationManager observationManager = createMock(ObservationManager.class);
		ReflectionHelper.set(JcrEventsService.class, "admin", service, admin);

		expect(admin.getWorkspace()).andReturn(workspace);
		expect(workspace.getObservationManager()).andReturn(observationManager);
		observationManager.removeEventListener(service);
		admin.logout();

		replay(admin, workspace, observationManager);
		ReflectionHelper.invoke(JcrEventsService.class, "deactivate",
				new Class<?>[] { ComponentContext.class }, service, new Object[] { null });
		verify(admin, workspace, observationManager);
	}

	@Test
	public void testNotNullAdminDeactivateWithException() throws Exception {
		Session admin = createMock(Session.class);
		Workspace workspace = createMock(Workspace.class);
		ReflectionHelper.set(JcrEventsService.class, "admin", service, admin);

		expect(admin.getWorkspace()).andReturn(workspace);
		expect(workspace.getObservationManager()).andThrow(new RepositoryException());
		admin.logout();

		replay(admin, workspace);
		ReflectionHelper.invoke(JcrEventsService.class, "deactivate",
				new Class<?>[] { ComponentContext.class }, service, new Object[] { null });
		verify(admin, workspace);
	}
}
