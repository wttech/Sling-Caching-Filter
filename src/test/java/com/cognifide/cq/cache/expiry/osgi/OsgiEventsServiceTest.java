package com.cognifide.cq.cache.expiry.osgi;

import com.cognifide.cq.cache.expiry.collection.GuardCollectionWalker;
import com.cognifide.cq.cache.expiry.guard.ExpiryGuard;
import java.util.Arrays;
import java.util.Map;
import org.apache.sling.api.SlingConstants;
import org.assertj.core.util.Maps;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.osgi.service.event.Event;

public class OsgiEventsServiceTest {

	private static final String PATH = "path";

	@Mock
	private ExpiryGuard expiryGuard;

	@Mock
	private GuardCollectionWalker collectionWalker;

	@InjectMocks
	private OsgiEventsService testedObject = new OsgiEventsService();

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@Test
	public void shouldExecuteOnContentChangeMethodOnExpiryGuards() {
		//given
		Event event = createEvent();
		when(collectionWalker.getGuards()).thenReturn(Arrays.asList(expiryGuard));

		//when
		testedObject.handleEvent(event);

		//then
		verify(expiryGuard).onContentChange(PATH);
	}

	private Event createEvent() {
		Map<String, String> properties = Maps.newHashMap();
		properties.put(SlingConstants.PROPERTY_PATH, PATH);

		return new Event("topic", properties);
	}

}
