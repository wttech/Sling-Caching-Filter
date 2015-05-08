package com.cognifide.cq.cache.refresh.jcr;

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
import org.junit.Test;
import org.osgi.service.component.ComponentContext;

import com.cognifide.cq.cache.test.utils.ReflectionHelper;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Rule;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * @author Bartosz Rudnicki
 */
public class JcrEventsServiceTest {

	private static final String PATH_1 = "/content/home/page/changed";

	private static final String PATH_2 = "/content/home/page/changed/jcr:content";

	private static final String PATH_3 = "/apps/other/page/changed";

	private static final String PATH_4 = "/libs/other/page/changed";

	@Mock
	private JcrEventListener listener;

	@Mock
	private SlingRepository repository;

	@InjectMocks
	private JcrEventsService testedObject = new JcrEventsService();

	private List<JcrEventListener> listeners;

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@Test
	public void shouldAddEventListenerWhenAskedToAdd() {
		//when
		testedObject.addEventListener(listener);

		//then
		assertThat(getListenersFromTestedObject().size(), is(1));
	}

	@SuppressWarnings("unchecked")
	private List<JcrEventListener> getListenersFromTestedObject() {
		List<JcrEventListener> result = null;
		try {
			Field listenersField = testedObject.getClass().getDeclaredField("listeners");
			listenersField.setAccessible(true);
			result = (List<JcrEventListener>) listenersField.get(testedObject);
		} catch (NoSuchFieldException ex) {
			Logger.getLogger(JcrEventsServiceTest.class.getName()).log(Level.SEVERE, null, ex);
		} catch (SecurityException ex) {
			Logger.getLogger(JcrEventsServiceTest.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IllegalArgumentException ex) {
			Logger.getLogger(JcrEventsServiceTest.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			Logger.getLogger(JcrEventsServiceTest.class.getName()).log(Level.SEVERE, null, ex);
		}
		return result;
	}

	@Test
	public void shouldRemoveEventListenerWhenAskedToRemove() {
		//given
		testedObject.addEventListener(listener);

		//when
		testedObject.removeEventListener(listener);

		//then
		assertThat(getListenersFromTestedObject().size(), is(0));
	}

	@Test
	public void shouldRemoveAllEventListenersWhenAskedToClear() {
		//given
		testedObject.addEventListener(listener);

		//when
		testedObject.clearEventListeners();

		//then
		assertThat(getListenersFromTestedObject().size(), is(0));
	}

	@Test
	public void testOnEvent() throws RepositoryException {
		EventIterator eventIterator = createMock(EventIterator.class);
		Event event1 = createMock(Event.class);
		Event event2 = createMock(Event.class);
		Event event3 = createMock(Event.class);
		Event event4 = createMock(Event.class);
		JcrEventListener listener = createMock(JcrEventListener.class);
//		JcrEventsService.addEventListener(listener);

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
		testedObject.onEvent(eventIterator);
		verify(eventIterator, event1, event2, event3, event4, listener);
	}

	@Mock
	private EventIterator eventIterator;

	@Mock
	private Event event1;

	@Mock
	private Event event2;

	@Mock
	private Event event3;

	@Mock
	private Event event4;

	@Test
	public void should() throws RepositoryException {
		when(eventIterator.hasNext()).thenReturn(true, true, true, true, false);
		when(eventIterator.nextEvent()).thenReturn(event1, event2, event3, event4);
		when(event1.getPath()).thenReturn(PATH_1);
		when(event2.getPath()).thenReturn(PATH_2);
		when(event3.getPath()).thenReturn(PATH_3);
		when(event4.getPath()).thenReturn(PATH_4);
		when(listener.contentChanged(PATH_1)).thenReturn(false);
		when(listener.contentChanged(PATH_3)).thenReturn(false);

		testedObject.onEvent(eventIterator);
	}

	@Test
	public void testActivate() throws IllegalAccessException, RepositoryException, InvocationTargetException,
			NoSuchFieldException, NoSuchMethodException {
		SlingRepository repository = createMock(SlingRepository.class);
		Session session = createMock(Session.class);
		Workspace workspace = createMock(Workspace.class);
		ObservationManager observationManager = createMock(ObservationManager.class);

		ReflectionHelper.set(JcrEventsService.class, "repository", testedObject, repository);

		expect(repository.loginAdministrative(null)).andReturn(session);
		expect(session.getWorkspace()).andReturn(workspace);
		expect(workspace.getObservationManager()).andReturn(observationManager);
		observationManager
				.addEventListener(testedObject, JcrEventsService.ALL_TYPES, "/", true, null, null, false);

		replay(repository, session, workspace, observationManager);
		ReflectionHelper.invoke(JcrEventsService.class, "activate",
				new Class<?>[]{ComponentContext.class}, testedObject, new Object[]{null});
		verify(repository, session, workspace, observationManager);
	}

	@Test
	public void testNullAdminDeactivate() throws Exception {
		ReflectionHelper.set(JcrEventsService.class, "admin", testedObject, null);
		ReflectionHelper.invoke(JcrEventsService.class, "deactivate",
				new Class<?>[]{ComponentContext.class}, testedObject, new Object[]{null});
	}

	@Test
	public void testNotNullAdminDeactivate() throws Exception {
		Session admin = createMock(Session.class);
		Workspace workspace = createMock(Workspace.class);
		ObservationManager observationManager = createMock(ObservationManager.class);
		ReflectionHelper.set(JcrEventsService.class, "admin", testedObject, admin);

		expect(admin.getWorkspace()).andReturn(workspace);
		expect(workspace.getObservationManager()).andReturn(observationManager);
		observationManager.removeEventListener(testedObject);
		admin.logout();

		replay(admin, workspace, observationManager);
		ReflectionHelper.invoke(JcrEventsService.class, "deactivate",
				new Class<?>[]{ComponentContext.class}, testedObject, new Object[]{null});
		verify(admin, workspace, observationManager);
	}

	@Test
	public void testNotNullAdminDeactivateWithException() throws Exception {
		Session admin = createMock(Session.class);
		Workspace workspace = createMock(Workspace.class);
		ReflectionHelper.set(JcrEventsService.class, "admin", testedObject, admin);

		expect(admin.getWorkspace()).andReturn(workspace);
		expect(workspace.getObservationManager()).andThrow(new RepositoryException());
		admin.logout();

		replay(admin, workspace);
		ReflectionHelper.invoke(JcrEventsService.class, "deactivate",
				new Class<?>[]{ComponentContext.class}, testedObject, new Object[]{null});
		verify(admin, workspace);
	}
}
