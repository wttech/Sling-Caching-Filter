package com.cognifide.cq.cache.cache;

import com.cognifide.cq.cache.cache.callback.MissingCacheEntryCallback;
import com.cognifide.cq.cache.definition.CacheConfigurationEntry;
import com.cognifide.cq.cache.definition.ResourceTypeCacheDefinition;
import com.cognifide.cq.cache.expiry.collection.GuardCollectionWatcher;
import com.cognifide.cq.cache.filter.osgi.CacheManagerProvider;
import com.cognifide.cq.cache.model.ResourceTypeCacheConfiguration;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.servlet.ServletException;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.Resource;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import static org.mockito.Matchers.isA;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class JCacheHolderTest {

	private static final String RESOURCE_TYPE = "resource/type";

	private static final String CACHE_KEY = "/apps/resource/type";

	private static final String KEY = "key";

	@Mock
	private MissingCacheEntryCallback callback;

	@Mock
	private CacheEntity cacheEntity;

	@Mock
	private CacheManagerProvider cacheManagerProvider;

	@Mock
	private CacheManager cacheManager;

	@Mock
	private Cache<String, CacheEntity> cache;

	@Mock
	private CacheOperations cacheOperations;

	@Mock
	private GuardCollectionWatcher guardCollectionWatcher;

	@InjectMocks
	private JCacheHolder testedObject = new JCacheHolder();

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	private ResourceTypeCacheDefinition createValidAndEnabledResourceTypeCacheDefinition() {
		ResourceTypeCacheDefinition resourceTypeCacheDefinition = mock(ResourceTypeCacheDefinition.class);

		when(resourceTypeCacheDefinition.isValid()).thenReturn(true);
		when(resourceTypeCacheDefinition.isEnabled()).thenReturn(true);
		when(resourceTypeCacheDefinition.getResourceType()).thenReturn(RESOURCE_TYPE);

		return resourceTypeCacheDefinition;
	}

	@Test
	public void shouldAnswerWithNoURIWhenCacheManagerIsClosed() {
		//given
		setUpClosedCacheManager();

		//when
		Optional<URI> actual = testedObject.getCacheManagerURI();

		//then
		assertThat(actual.isPresent()).isFalse();
	}

	private void setUpClosedCacheManager() {
		when(cacheManagerProvider.getCacheManager()).thenReturn(cacheManager);
		when(cacheManager.isClosed()).thenReturn(true);
	}

	@Test
	public void shouldAnswerWithURIWhenCacheManagerIsOpen() throws URISyntaxException {
		//given
		setUpOpenCacheManager();

		URI uri = new URI("uri");
		when(cacheManager.getURI()).thenReturn(uri);

		//when
		Optional<URI> actual = testedObject.getCacheManagerURI();

		//then
		assertThat(actual.isPresent()).isTrue();
		assertThat(actual.get()).isEqualTo(uri);
	}

	private void setUpOpenCacheManager() {
		when(cacheManagerProvider.getCacheManager()).thenReturn(cacheManager);
		when(cacheManager.isClosed()).thenReturn(false);
	}

	@Test
	public void shouldAnswerWithNoCacheNamesWhenCacheManagerIsClosed() {
		//given
		setUpClosedCacheManager();

		//when
		Iterable<String> actual = testedObject.getCacheNames();

		//then
		assertThat(actual).isEmpty();
	}

	@Test
	public void shouldAnswerWithCacheNamesWhenCacheManagerIsOpenAndCacheExists() {
		//given
		setUpOpenCacheManager();

		when(cacheManager.getCacheNames()).thenReturn(Arrays.asList(RESOURCE_TYPE));

		//when
		Iterable<String> actual = testedObject.getCacheNames();

		//then
		assertThat(actual).hasSize(1);
	}

	@Test
	public void shouldAnswerWithNoKeysWhenCacheWithGivenNameDoesNotExist() {
		//given
		when(cacheOperations.findFor(RESOURCE_TYPE)).thenReturn(Optional.<Cache<String, CacheEntity>>absent());

		//when
		Collection<String> actual = testedObject.getKeysFor(RESOURCE_TYPE);

		//then
		assertThat(actual).isEmpty();
	}

	@Test
	public void shouldAnswerWithNoKeysWhenCacheIsClosed() {
		//given
		setUpClosedCache();

		//when
		Collection<String> actual = testedObject.getKeysFor(RESOURCE_TYPE);

		//then
		assertThat(actual).isEmpty();
	}

	private void setUpClosedCache() {
		when(cacheOperations.findFor(RESOURCE_TYPE)).thenReturn(Optional.of(cache));
		when(cache.isClosed()).thenReturn(true);
	}

	@Test
	public void shouldAnswerWithKeysWhenCacheIsOpen() {
		//given
		setUpOpenCache();

		List<Cache.Entry<String, CacheEntity>> list = Lists.newArrayList();
		list.add(createEntry(KEY));
		when(cache.iterator()).thenReturn(list.iterator());

		//when
		Collection<String> actual = testedObject.getKeysFor(RESOURCE_TYPE);

		//then
		assertThat(actual).hasSize(1);
		assertThat(actual).contains(KEY);
	}

	private void setUpOpenCache() {
		when(cacheOperations.findFor(RESOURCE_TYPE)).thenReturn(Optional.of(cache));
		when(cache.isClosed()).thenReturn(false);
	}

	private Cache.Entry<String, CacheEntity> createEntry(String key) {
		@SuppressWarnings("unchecked")
		Cache.Entry<String, CacheEntity> entry = (Cache.Entry<String, CacheEntity>) mock(Cache.Entry.class);
		when(entry.getKey()).thenReturn(key);
		return entry;
	}

	@Test
	public void shouldAnswerWhenCacheDoesNotExistAndCannotBeCreated() throws IOException, ServletException {
		//given
		testedObject.activate();
		when(cacheOperations.findFor(RESOURCE_TYPE)).thenReturn(Optional.<Cache<String, CacheEntity>>absent());
		when(callback.generateCacheEntity()).thenReturn(cacheEntity);

		//when
		CacheEntity actual = testedObject.putOrGet(createReqeuest(), createResourceTypeCacheConfiguration(), callback);

		//then
		assertThat(actual).isEqualTo(cacheEntity);
		verifyNoMoreInteractions(cache);
	}

	@Test
	public void shouldAnswerWhenCacheDoesNotExistAndCanBeCreated() throws IOException, ServletException {
		//given
		testedObject.activate();
		testedObject.bindResourceTypeCacheDefinition(createValidAndEnabledResourceTypeCacheDefinition());

		ResourceTypeCacheConfiguration resourceTypeCacheConfiguration = createResourceTypeCacheConfiguration();
		@SuppressWarnings("unchecked")
		Cache<String, CacheEntity> newCache = mock(Cache.class);

		setUpClosedCache();
		when(cacheOperations.create(resourceTypeCacheConfiguration)).thenReturn(Optional.of(newCache));
		when(callback.generateCacheEntity()).thenReturn(cacheEntity);

		//when
		CacheEntity actual = testedObject.putOrGet(createReqeuest(), resourceTypeCacheConfiguration, callback);

		//then
		assertThat(actual).isEqualTo(cacheEntity);
		verify(cache).isClosed();
		verify(cacheOperations).findFor(RESOURCE_TYPE);
		verify(cacheOperations).delete(RESOURCE_TYPE);
		verify(cacheOperations).create(isA(CacheConfigurationEntry.class));
		verify(newCache).get(CACHE_KEY);
		verify(newCache).put(CACHE_KEY, cacheEntity);
		verifyNoMoreInteractions(cache, cacheOperations, newCache);
	}

	@Test
	public void shouldAnswerWhenCacheHasEntityWithGivenKey() throws IOException, ServletException {
		//given
		testedObject.activate();
		setUpOpenCache();
		when(cache.get(CACHE_KEY)).thenReturn(cacheEntity);
		when(callback.generateCacheEntity()).thenReturn(cacheEntity);

		//when
		CacheEntity actual = testedObject.putOrGet(createReqeuest(), createResourceTypeCacheConfiguration(), callback);

		//then
		assertThat(actual).isEqualTo(cacheEntity);
		verify(cache).isClosed();
		verify(cache).get(CACHE_KEY);
		verifyNoMoreInteractions(cache);
	}

	@Test
	public void shouldAnswerWhenCacheDoesNotHaveEntityWithGivenKey() throws IOException, ServletException {
		//given
		testedObject.activate();
		setUpOpenCache();
		when(callback.generateCacheEntity()).thenReturn(cacheEntity);

		//when
		CacheEntity actual = testedObject.putOrGet(createReqeuest(), createResourceTypeCacheConfiguration(), callback);

		//then
		assertThat(actual).isEqualTo(cacheEntity);
		verify(cache).isClosed();
		verify(cache).get(CACHE_KEY);
		verify(cache).put(CACHE_KEY, cacheEntity);
		verifyNoMoreInteractions(cache);
	}

	private SlingHttpServletRequest createReqeuest() {
		SlingHttpServletRequest request = mock(SlingHttpServletRequest.class);

		Resource resource = mock(Resource.class);
		when(resource.getResourceType()).thenReturn(RESOURCE_TYPE);
		when(request.getResource()).thenReturn(resource);

		RequestPathInfo requestPathInfo = mock(RequestPathInfo.class);
		when(requestPathInfo.getSelectorString()).thenReturn(StringUtils.EMPTY);
		when(request.getRequestPathInfo()).thenReturn(requestPathInfo);

		return request;
	}

	private ResourceTypeCacheConfiguration createResourceTypeCacheConfiguration() {
		ResourceTypeCacheConfiguration resourceTypeCacheConfiguration = mock(ResourceTypeCacheConfiguration.class);
		when(resourceTypeCacheConfiguration.getResourceType()).thenReturn(RESOURCE_TYPE);
		when(resourceTypeCacheConfiguration.getCacheLevel()).thenReturn(0);
		return resourceTypeCacheConfiguration;
	}

	@Test
	public void shouldNotRemoveElementWhenCacheDoesNotExist() {
		//given
		when(cacheOperations.findFor(RESOURCE_TYPE)).thenReturn(Optional.<Cache<String, CacheEntity>>absent());

		//when
		testedObject.remove(RESOURCE_TYPE, KEY);

		//then
		verify(cacheOperations).findFor(RESOURCE_TYPE);
		verifyNoMoreInteractions(cacheOperations, cache);
	}

	@Test
	public void shouldNotRemoveElementWhenCacheIsNotValid() {
		//given
		setUpClosedCache();

		//when
		testedObject.remove(RESOURCE_TYPE, KEY);

		//then
		verify(cacheOperations).findFor(RESOURCE_TYPE);
		verify(cache).isClosed();
		verifyNoMoreInteractions(cacheOperations, cache);
	}

	@Test
	public void shouldRemoveElementWhenCacheIsValid() {
		//given
		setUpOpenCache();

		//when
		testedObject.remove(RESOURCE_TYPE, KEY);

		//then
		verify(cacheOperations).findFor(RESOURCE_TYPE);
		verify(cache).isClosed();
		verify(cache).remove(KEY);
		verifyNoMoreInteractions(cacheOperations, cache);
	}

	@Test
	public void shouldNotClearCacheWhenItDoesNotExist() {
		//given
		when(cacheOperations.findFor(RESOURCE_TYPE)).thenReturn(Optional.<Cache<String, CacheEntity>>absent());

		//when
		testedObject.clear(RESOURCE_TYPE);

		//then
		verify(cacheOperations).findFor(RESOURCE_TYPE);
		verifyNoMoreInteractions(cacheOperations, cache);
	}

	@Test
	public void shouldNotClearCacheWhenItIsNotValid() {
		//given
		setUpClosedCache();

		//when
		testedObject.clear(RESOURCE_TYPE);

		//then
		verify(cacheOperations).findFor(RESOURCE_TYPE);
		verify(cache).isClosed();
		verifyNoMoreInteractions(cacheOperations, cache);
	}

	@Test
	public void shouldClearCacheWhenItIsValid() {
		//given
		setUpOpenCache();

		//when
		testedObject.clear(RESOURCE_TYPE);

		//then
		verify(cacheOperations).findFor(RESOURCE_TYPE);
		verify(cache).isClosed();
		verify(cache).clear();
		verifyNoMoreInteractions(cacheOperations, cache);
	}

}
