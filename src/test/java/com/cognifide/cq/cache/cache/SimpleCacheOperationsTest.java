package com.cognifide.cq.cache.cache;

import com.cognifide.cq.cache.definition.CacheConfigurationEntry;
import com.cognifide.cq.cache.expiry.collection.GuardCollectionWatcher;
import com.cognifide.cq.cache.filter.osgi.CacheManagerProvider;
import com.google.common.base.Optional;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.MutableConfiguration;
import org.apache.commons.lang.StringUtils;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class SimpleCacheOperationsTest {

	private static final String RESOURCE_TYPE = "resource/type";

	@Mock
	private CacheManagerProvider cacheManagerProvider;

	@Mock
	private CacheManager cacheManager;

	@Mock
	private Cache<String, CacheEntity> cache;

	@Mock
	private GuardCollectionWatcher guardCollectionWatcher;

	@InjectMocks
	private SimpleCacheOperations testedObject = new SimpleCacheOperations();

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@Test
	public void shouldNotCreateCacheWhenNameIsNotValid() {
		//given
		CacheConfigurationEntry cacheConfigurationEntry = mock(CacheConfigurationEntry.class);

		//when
		Optional<Cache<String, CacheEntity>> actual = testedObject.create(cacheConfigurationEntry);

		//then
		assertThat(actual.isPresent()).isFalse();
	}

	@Test
	public void shouldNotCreateCacheWhenCacheManagerIsClosed() {
		//given
		setUpClosedCacheManager();

		//when
		Optional<Cache<String, CacheEntity>> actual = testedObject.create(createCacheConfigurationEntry());

		//then
		assertThat(actual.isPresent()).isFalse();
	}

	private CacheConfigurationEntry createCacheConfigurationEntry() {
		CacheConfigurationEntry cacheConfigurationEntry = mock(CacheConfigurationEntry.class);
		when(cacheConfigurationEntry.getResourceType()).thenReturn(RESOURCE_TYPE);
		return cacheConfigurationEntry;
	}

	private void setUpClosedCacheManager() {
		when(cacheManagerProvider.getCacheManager()).thenReturn(cacheManager);
		when(cacheManager.isClosed()).thenReturn(true);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void shouldCreateCacheWhenCacheManagerIsOpen() {
		//given
		setUpOpenCacheManager();
		doReturn(cache).when(cacheManager).createCache(eq(RESOURCE_TYPE), isA(MutableConfiguration.class));

		//when
		Optional<Cache<String, CacheEntity>> actual = testedObject.create(createCacheConfigurationEntry());

		//then
		assertThat(actual.isPresent()).isTrue();
	}

	private void setUpOpenCacheManager() {
		when(cacheManagerProvider.getCacheManager()).thenReturn(cacheManager);
		when(cacheManager.isClosed()).thenReturn(false);
	}

	@Test
	public void shouldNotFindCacheWhenCacheNameIsNull() {
		//when
		Optional<Cache<String, CacheEntity>> actual = testedObject.findFor(null);

		//then
		assertThat(actual.isPresent()).isFalse();
	}

	@Test
	public void shouldNotFindCacheWhenCacheNameIsEmptyString() {
		//when
		Optional<Cache<String, CacheEntity>> actual = testedObject.findFor(StringUtils.EMPTY);

		//then
		assertThat(actual.isPresent()).isFalse();
	}

	@Test
	public void shouldNotFindCacheWhenCacheManagerIsClosed() {
		//given
		setUpClosedCacheManager();

		//when
		Optional<Cache<String, CacheEntity>> actual = testedObject.findFor(RESOURCE_TYPE);

		//then
		assertThat(actual.isPresent()).isFalse();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void shouldFindCacheWhenCacheManagerIsOpen() {
		//given
		setUpOpenCacheManager();
		when(cacheManager.getCache(RESOURCE_TYPE, String.class, CacheEntity.class)).thenReturn(cache);

		//when
		Optional<Cache<String, CacheEntity>> actual = testedObject.findFor(RESOURCE_TYPE);

		//then
		assertThat(actual.isPresent()).isTrue();
	}

	@Test
	public void shouldNotDeleteCacheWhenCacheDoesNotExist() {
		//given
		setUpOpenCacheManager();

		//when
		testedObject.delete(RESOURCE_TYPE);

		//then
		verify(cacheManager).isClosed();
		verify(cacheManager).getCache(RESOURCE_TYPE, String.class, CacheEntity.class);
		verifyNoMoreInteractions(cacheManager);
	}

	@Test
	public void shouldDeleteCacheWhenCacheExists() {
		//given
		setUpOpenCacheManager();
		when(cacheManager.getCache(RESOURCE_TYPE, String.class, CacheEntity.class)).thenReturn(cache);

		//when
		testedObject.delete(RESOURCE_TYPE);

		//then
		verify(cacheManager).isClosed();
		verify(cacheManager).getCache(RESOURCE_TYPE, String.class, CacheEntity.class);
		verify(cacheManager).destroyCache(RESOURCE_TYPE);
		verifyNoMoreInteractions(cacheManager);
	}
}
