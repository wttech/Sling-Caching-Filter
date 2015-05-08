package com.cognifide.cq.cache.refresh.jcr;

import com.cognifide.cq.cache.model.ResourceTypeCacheConfiguration;
import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.base.CacheEntry;
import com.opensymphony.oscache.base.events.CacheEntryEvent;
import com.opensymphony.oscache.base.events.CachePatternEvent;
import com.opensymphony.oscache.base.events.CachewideEvent;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Pattern;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * @author Bartosz Rudnicki
 */
public class FilterJcrRefreshPolicyTest {

	private static final String CACHE_ENTRY_KEY = "instance key";

	private static final String DIFFERENT_CACHE_ENTRY_KEY = "different instance key";

	private static final Pattern INVALIDATE_PATH_PATTERN = Pattern.compile("/home/invalidate.*");

	private static final String RESOURCE_TYPE = "/resource/type";

	private static final String INSTANCE_INVALIDATE_PATH = "/home/invalidate/me";

	private static final String OTHER_INVALIDATE_PATH = "/home/dont/invalidate/me";

	@Mock
	private CacheEntry cacheEntry;

	@Mock
	private Cache cache;

	@Mock
	private JcrEventsService jcrEventsService;

	@Mock
	private ResourceTypeCacheConfiguration resourceTypeCacheConfiguration;

	private FilterJcrRefreshPolicy testedObject;

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@Before
	public void setUp() throws Exception {
		when(resourceTypeCacheConfiguration.getTime()).thenReturn(10);
		when(resourceTypeCacheConfiguration.getInvalidatePaths()).thenReturn(Arrays.asList(INVALIDATE_PATH_PATTERN));
		when(resourceTypeCacheConfiguration.getResourceTypePath()).thenReturn(RESOURCE_TYPE);
		testedObject = new FilterJcrRefreshPolicy(jcrEventsService, CACHE_ENTRY_KEY, resourceTypeCacheConfiguration);
	}

	@Test
	public void shouldRemoveRefreshPolicyWhenCacheEntryAdded() {
		//given
		CacheEntryEvent cacheEntryEvent = setUpCacheEntryEventWithSameKeyAsRefreshPolicy();

		//when
		testedObject.cacheEntryAdded(cacheEntryEvent);

		//then
		verifyRefreshPolicyWasRemovedFromServiceAndCacheEntryEvent();
	}

	private CacheEntryEvent setUpCacheEntryEventWithSameKeyAsRefreshPolicy() {
		when(cacheEntry.getKey()).thenReturn(CACHE_ENTRY_KEY);
		return new CacheEntryEvent(cache, cacheEntry);
	}

	private void verifyRefreshPolicyWasRemovedFromServiceAndCacheEntryEvent() {
		Mockito.verify(jcrEventsService).removeEventListener(testedObject);
		Mockito.verify(cache).removeCacheEventListener(testedObject);
	}

	@Test
	public void shoulNotRemoveRefreshPolicyWhenCacheEntryEventHasDifferentKeyOnCacheEntryAdded() {
		//given
		CacheEntryEvent cacheEntryEvent = setUpCacheEntryEventWithDifferentKeyAsRefreshPolicy();

		//when
		testedObject.cacheEntryAdded(cacheEntryEvent);

		//then
		verifyRefreshPolicyWasNotRemovedFromServiceAndCacheEntryEvent();
	}

	private CacheEntryEvent setUpCacheEntryEventWithDifferentKeyAsRefreshPolicy() {
		when(cacheEntry.getKey()).thenReturn(DIFFERENT_CACHE_ENTRY_KEY);
		return new CacheEntryEvent(cache, cacheEntry);
	}

	private void verifyRefreshPolicyWasNotRemovedFromServiceAndCacheEntryEvent() {
		Mockito.verify(jcrEventsService, never()).removeEventListener(testedObject);
		Mockito.verify(cache, never()).removeCacheEventListener(testedObject);
	}

	@Test
	public void shouldRemoveRefreshPolicyWhenCacheEntryFlushed() {
		//given
		CacheEntryEvent cacheEntryEvent = setUpCacheEntryEventWithSameKeyAsRefreshPolicy();

		//when
		testedObject.cacheEntryFlushed(cacheEntryEvent);

		//then
		verifyRefreshPolicyWasRemovedFromServiceAndCacheEntryEvent();
	}

	@Test
	public void shoulNotRemoveRefreshPolicyWhenCacheEntryEventHasDifferentKeyOnCacheEntryFlushed() {
		//given
		CacheEntryEvent cacheEntryEvent = setUpCacheEntryEventWithDifferentKeyAsRefreshPolicy();

		//when
		testedObject.cacheEntryFlushed(cacheEntryEvent);

		//then
		verifyRefreshPolicyWasNotRemovedFromServiceAndCacheEntryEvent();
	}

	@Test
	public void shouldRemoveRefreshPolicyWhenCacheEntryRemoved() {
		//given
		CacheEntryEvent cacheEntryEvent = setUpCacheEntryEventWithSameKeyAsRefreshPolicy();

		//when
		testedObject.cacheEntryRemoved(cacheEntryEvent);

		//then
		verifyRefreshPolicyWasRemovedFromServiceAndCacheEntryEvent();
	}

	@Test
	public void shoulNotRemoveRefreshPolicyWhenCacheEntryEventHasDifferentKeyOnCacheEntryRemoved() {
		//given
		CacheEntryEvent cacheEntryEvent = setUpCacheEntryEventWithDifferentKeyAsRefreshPolicy();

		//when
		testedObject.cacheEntryRemoved(cacheEntryEvent);

		//then
		verifyRefreshPolicyWasNotRemovedFromServiceAndCacheEntryEvent();
	}

	@Test
	public void shouldRemoveRefreshPolicyWhenCacheEntryUpdated() {
		//given
		CacheEntryEvent cacheEntryEvent = setUpCacheEntryEventWithSameKeyAsRefreshPolicy();

		//when
		testedObject.cacheEntryUpdated(cacheEntryEvent);

		//then
		verifyRefreshPolicyWasRemovedFromServiceAndCacheEntryEvent();
	}

	@Test
	public void shoulNotRemoveRefreshPolicyWhenCacheEntryEventHasDifferentKeyOnCacheEntryUpdated() {
		//given
		CacheEntryEvent cacheEntryEvent = setUpCacheEntryEventWithDifferentKeyAsRefreshPolicy();

		//when
		testedObject.cacheEntryUpdated(cacheEntryEvent);

		//then
		verifyRefreshPolicyWasNotRemovedFromServiceAndCacheEntryEvent();
	}

	@Test
	public void shouldRemoveRefreshPolicyWhenCacheFlushed() {
		//given
		CachewideEvent cachewideEvent = setUpCacheWideEvent();

		//when
		testedObject.cacheFlushed(cachewideEvent);

		//then
		verifyRefreshPolicyWasRemovedFromServiceAndCacheEntryEvent();
	}

	private CachewideEvent setUpCacheWideEvent() {
		return new CachewideEvent(cache, new Date(), StringUtils.EMPTY);
	}

	@Test
	public void shouldRemoveRefreshPolicyWhenCachePatternFlushedAndKeyMatches() {
		//given
		CachePatternEvent cachePatternEvent = new CachePatternEvent(cache, CACHE_ENTRY_KEY);

		//when
		testedObject.cachePatternFlushed(cachePatternEvent);

		//then
		verifyRefreshPolicyWasRemovedFromServiceAndCacheEntryEvent();
	}

	@Test
	public void shouldNotRemoveRefreshPolicyWhenCachePatternFlushedAndKeyNotMatches() {
		//given
		CachePatternEvent cachePatternEvent = new CachePatternEvent(cache, DIFFERENT_CACHE_ENTRY_KEY);

		//when
		testedObject.cachePatternFlushed(cachePatternEvent);

		//then
		verifyRefreshPolicyWasNotRemovedFromServiceAndCacheEntryEvent();
	}

	@Test
	public void shouldNotNeedRefreshWhenRefreshPeriodDidNotPass() {
		//given
		when(cacheEntry.getKey()).thenReturn(CACHE_ENTRY_KEY);
		when(cacheEntry.getLastUpdate()).thenReturn(System.currentTimeMillis());
		testedObject.setRefreshPeriod(100L);

		//when
		boolean actual = testedObject.needsRefresh(cacheEntry);

		//then
		assertFalse("should not refresh because refreshed peiriod did not pass", actual);
	}

	@Test
	public void shouldNeedRefreshWhenRefreshPeriodPass() {
		//given
		when(cacheEntry.getKey()).thenReturn(CACHE_ENTRY_KEY);
		when(cacheEntry.getLastUpdate()).thenReturn(1L);
		testedObject.setRefreshPeriod(100L);

		//when
		boolean actual = testedObject.needsRefresh(cacheEntry);

		//then
		assertTrue("should refresh because refreshed peiriod passed", actual);
	}

	@Test
	public void shouldNotNeedToRefreshWhenContentDidNotChangeAndDoesNotNeedRefreshFromPassedTime() {
		//given
		when(cacheEntry.getKey()).thenReturn(CACHE_ENTRY_KEY);
		when(cacheEntry.getLastUpdate()).thenReturn(System.currentTimeMillis());
		testedObject.setRefreshPeriod(100L);

		//when
		testedObject.contentChanged(OTHER_INVALIDATE_PATH);
		boolean actual = testedObject.needsRefresh(cacheEntry);

		//then
		assertFalse("no need for refresh when content did not change", actual);
	}

	@Test
	public void shouldNeedToRefreshWhenContentChangedAndDoesNotNeedRefreshFromPassedTime() {
		//given
		when(cacheEntry.getKey()).thenReturn(CACHE_ENTRY_KEY);
		when(cacheEntry.getLastUpdate()).thenReturn(System.currentTimeMillis());
		testedObject.setRefreshPeriod(100L);

		//when
		testedObject.contentChanged(INSTANCE_INVALIDATE_PATH);
		boolean actual = testedObject.needsRefresh(cacheEntry);

		//then
		assertTrue("content changed and there is need for refresh", actual);
	}
}
