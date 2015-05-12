package com.cognifide.cq.cache.refresh.jcr;

import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.observation.ObservationManager;

import org.apache.sling.jcr.api.SlingRepository;
import org.junit.Test;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Rule;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
	private EventIterator eventIterator;

	@Mock
	private Event event1;

	@Mock
	private Event event2;

	@Mock
	private Event event3;

	@Mock
	private Event event4;

	@Mock
	private JcrEventListener listener;

	@Mock
	private SlingRepository repository;

	private Session session;

	@Mock
	private Workspace workspace;

	@Mock
	private ObservationManager observationManager;

	@InjectMocks
	private JcrEventsService testedObject = new JcrEventsService();

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@Before
	public void setUp() {
		// ommits injecting session to tested object
		session = mock(Session.class);
	}

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
	public void shouldHandleEventsWithPathsStartingWithContentAndApps() throws RepositoryException {
		//given
		when(eventIterator.hasNext()).thenReturn(true, true, true, true, false);
		when(eventIterator.nextEvent()).thenReturn(event1, event2, event3, event4);
		when(event1.getPath()).thenReturn(PATH_1);
		when(event2.getPath()).thenReturn(PATH_2);
		when(event3.getPath()).thenReturn(PATH_3);
		when(event4.getPath()).thenReturn(PATH_4);
		testedObject.addEventListener(listener);

		//when
		testedObject.onEvent(eventIterator);

		//then
		Mockito.verify(eventIterator, times(5)).hasNext();
		Mockito.verify(eventIterator, times(4)).nextEvent();
		Mockito.verify(listener).contentChanged(PATH_1);
		Mockito.verify(listener, never()).contentChanged(PATH_2);
		Mockito.verify(listener).contentChanged(PATH_3);
		Mockito.verify(listener, never()).contentChanged(PATH_4);
	}

	@Test
	public void observerationManagerShouldRegisterJcrEventsService() throws RepositoryException {
		//given
		setUpRepository();

		//when
		testedObject.activate();

		//then
		Mockito.verify(observationManager)
				.addEventListener(testedObject, JcrEventsService.ALL_TYPES, "/", true, null, null, false);
	}

	private void setUpRepository() throws RepositoryException {
		when(repository.loginAdministrative(null)).thenReturn(session);
		when(session.getWorkspace()).thenReturn(workspace);
		when(workspace.getObservationManager()).thenReturn(observationManager);
	}

	@Test
	public void shouldDeactivateWithoutExceptionWhenNoSession() throws RepositoryException {
		//when
		testedObject.deactivate();

		//then
		Mockito.verify(observationManager, never()).removeEventListener(testedObject);
	}

	@Test
	public void shouldDeactivateAndUnregisterFromObservationManagerWhenSession() throws RepositoryException {
		//given
		setUpRepository();
		testedObject.activate();

		//when
		testedObject.deactivate();

		//then
		Mockito.verify(observationManager).removeEventListener(testedObject);
		Mockito.verify(session).logout();
	}
}
