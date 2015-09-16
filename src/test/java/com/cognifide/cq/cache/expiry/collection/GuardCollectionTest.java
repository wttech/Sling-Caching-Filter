package com.cognifide.cq.cache.expiry.collection;

import com.cognifide.cq.cache.cache.CacheHolder;
import com.cognifide.cq.cache.expiry.guard.ExpiryGuard;
import com.cognifide.cq.cache.model.ResourceTypeCacheConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class GuardCollectionTest {

	private static final String CACHE_NAME_1 = "cache name 1";

	private static final String CACHE_NAME_2 = "cache name 2";

	private static final String KEY = "key";

	@Mock
	private CacheHolder cacheHolder;

	@Mock
	private ResourceTypeCacheConfiguration resourceTypeCacheConfiguration;

	private GuardCollection testedObject;

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@Before
	public void setUp() {
		testedObject = new GuardCollection();
		testedObject.activate();
	}

	@Test
	public void collectionShouldAddExpiryGuard() {
		ExpiryGuard expiryGuard = createExpiryGuard("cache name", KEY);

		//when
		testedObject.addGuard(expiryGuard);

		//then
		assertThat(testedObject.getGuards()).hasSize(1);
	}

	private ExpiryGuard createExpiryGuard(String cacheName, String key) {
		when(resourceTypeCacheConfiguration.getResourceType()).thenReturn(cacheName);
		return ExpiryGuard.createDeletingExpiryGuard(null, cacheHolder, resourceTypeCacheConfiguration, key);
	}

	@Test
	public void collectionShouldRemoveBucketIfAskedFor() {
		//given
		testedObject.addGuard(createExpiryGuard(CACHE_NAME_1, KEY));
		testedObject.addGuard(createExpiryGuard(CACHE_NAME_2, KEY));

		//when
		testedObject.removeGuards(CACHE_NAME_1);

		//then
		assertThat(testedObject.getGuards()).hasSize(1);
	}

	@Test
	public void collectionShouldRemoveGuardIfAskedForAndCacheNameAndKeyIsValid() {
		//given
		testedObject.addGuard(createExpiryGuard(CACHE_NAME_1, KEY));
		testedObject.addGuard(createExpiryGuard(CACHE_NAME_2, KEY));

		//when
		testedObject.removeGuard(CACHE_NAME_1, KEY);

		//then
		assertThat(testedObject.getGuards()).hasSize(1);
	}

	@Test
	public void collectionShouldNotRemoveGuardIfAskedForAndCacheNameIsInvalid() {
		//given
		testedObject.addGuard(createExpiryGuard(CACHE_NAME_1, KEY));
		testedObject.addGuard(createExpiryGuard(CACHE_NAME_2, KEY));

		//when
		testedObject.removeGuard("cache name 3", KEY);

		//then
		assertThat(testedObject.getGuards()).hasSize(2);
	}

	@Test
	public void collectionShouldEmptyGuardsWhenDeactivated() {
		//given
		testedObject.addGuard(createExpiryGuard("cache name", KEY));

		//when
		testedObject.deactivate();

		//then
		assertThat(testedObject.getGuards()).isEmpty();
	}

}
