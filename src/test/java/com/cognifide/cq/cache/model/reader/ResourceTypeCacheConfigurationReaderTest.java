package com.cognifide.cq.cache.model.reader;

import com.cognifide.cq.cache.definition.ResourceTypeCacheDefinition;
import com.cognifide.cq.cache.model.CacheConstants;
import com.cognifide.cq.cache.model.PathAliasStoreImpl;
import com.cognifide.cq.cache.model.ResourceTypeCacheConfiguration;
import org.junit.Test;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Rule;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * @author Bartosz Rudnicki
 */
public class ResourceTypeCacheConfigurationReaderTest {

	private static final int DEFAULT_TIME = 1000;

	private static final String FIELD_1_NAME = "field1";

	private static final String FIELD_2_NAME = "field2";

	private static final String[] XML_CACHE_INVALIDATE_FIELDS = {FIELD_1_NAME, FIELD_2_NAME, null};

	private static final String INVALIDATE_PATH_1 = "/content/invalidate/path/1";

	private static final String INVALIDATE_PATH_2 = "/content/invalidate/path/2";

	private static final String[] XML_CACHE_INVALIDATE_PATHS = {INVALIDATE_PATH_1, INVALIDATE_PATH_2, null};

	private static final String RESOURCE_PATH = "/content/resource";

	private static final String RESOURCE_TYPE = "resource/type";

	@Mock
	private SlingHttpServletRequest request;

	@Mock
	private Resource resource;

	@Mock
	private Resource typeResource;

	@Mock
	private ResourceResolver resourceResolver;

	@Mock
	private Resource cacheResource;

	@Mock
	private ValueMap valueMap;

	@Mock
	private ResourceTypeCacheDefinition resourceTypeCacheDefinition;

	@Mock
	private PathAliasStoreImpl pathAliasStore;

	@InjectMocks
	private ResourceTypeCacheConfigurationReaderImpl testedObject = new ResourceTypeCacheConfigurationReaderImpl();

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@Test
	public void shouldCreateCustomResourceTypeCachConfigurationWhenResourceTypeIsNotConfiguredInOsgiOrJcr() {
		//given
		setUpRequest();
		Mockito.when(resourceResolver.getResource(RESOURCE_TYPE)).thenReturn(null);

		//when
		ResourceTypeCacheConfiguration actual = testedObject.readComponentConfiguration(request, DEFAULT_TIME);

		//then
		assertThat(actual.isEnabled(), is(false));
		assertThat(actual.getCacheLevel(), is(Integer.MIN_VALUE));
		assertThat(actual.getResourceType(), is(RESOURCE_TYPE));
		assertThat(actual.getResourceTypePath(), is(nullValue()));
		assertThat(actual.getTime(), is(DEFAULT_TIME));
		assertThat(actual.getInvalidatePaths().size(), is(1));
	}

	private void setUpRequest() {
		Mockito.when(request.getResource()).thenReturn(resource);
		Mockito.when(resource.getResourceType()).thenReturn(RESOURCE_TYPE);
		Mockito.when(resource.getPath()).thenReturn(RESOURCE_PATH);
		Mockito.when(request.getResourceResolver()).thenReturn(resourceResolver);
	}

	@Test
	public void shouldCreateResourceTypeCachConfigurationWhenResourceTypeIsConfiguredInOsgi() {
		//given
		setUpRequest();
		Mockito.when(resourceResolver.getResource(RESOURCE_TYPE)).thenReturn(null);

		setUpResourceTypeCacheDefinitionWithoutValidityTime();
		Mockito.when(resourceTypeCacheDefinition.getValidityTimeInSeconds()).thenReturn(30);

		testedObject.bindResourceTypeCacheDefinition(resourceTypeCacheDefinition);

		//when
		ResourceTypeCacheConfiguration actual = testedObject.readComponentConfiguration(request, DEFAULT_TIME);

		//then
		assertThat(actual.isEnabled(), is(true));
		assertThat(actual.getCacheLevel(), is(1));
		assertThat(actual.getResourceType(), is(RESOURCE_TYPE));
		assertThat(actual.getResourceTypePath(), is(nullValue()));
		assertThat(actual.getTime(), is(30));
		assertThat(actual.getInvalidatePaths().size(), is(3));
	}

	private void setUpResourceTypeCacheDefinitionWithoutValidityTime() {
		Mockito.when(resourceTypeCacheDefinition.isEnabled()).thenReturn(true);
		Mockito.when(resourceTypeCacheDefinition.getCacheLevel()).thenReturn(1);
		Mockito.when(resourceTypeCacheDefinition.getResourceType()).thenReturn(RESOURCE_TYPE);
		Mockito.when(resourceTypeCacheDefinition.isInvalidateOnSelf()).thenReturn(true);
		Mockito.when(resourceTypeCacheDefinition.getInvalidateOnPaths())
				.thenReturn(XML_CACHE_INVALIDATE_PATHS);
		Mockito.when(resourceTypeCacheDefinition.getInvalidateOnReferencedFields())
				.thenReturn(XML_CACHE_INVALIDATE_FIELDS);
	}

	@Test
	public void shouldCreateResourceTypeCachConfigurationWhenResourceTypeIsConfiguredInOsgiWithDefaultTimeIfNotProvided() {
		//given
		setUpRequest();
		Mockito.when(resourceResolver.getResource(RESOURCE_TYPE)).thenReturn(null);

		setUpResourceTypeCacheDefinitionWithoutValidityTime();
		Mockito.when(resourceTypeCacheDefinition.getValidityTimeInSeconds()).thenReturn(null);

		testedObject.bindResourceTypeCacheDefinition(resourceTypeCacheDefinition);

		//when
		ResourceTypeCacheConfiguration actual = testedObject.readComponentConfiguration(request, DEFAULT_TIME);

		//then
		assertThat(actual.isEnabled(), is(true));
		assertThat(actual.getCacheLevel(), is(1));
		assertThat(actual.getResourceType(), is(RESOURCE_TYPE));
		assertThat(actual.getResourceTypePath(), is(nullValue()));
		assertThat(actual.getTime(), is(DEFAULT_TIME));
		assertThat(actual.getInvalidatePaths().size(), is(3));
	}

	@Test
	public void shouldCreateResourceTypeCachConfigurationWhenResourceTypeIsConfiguredInJcr() {
		//given
		setUpRequest();
		Mockito.when(resourceResolver.getResource("/apps/" + RESOURCE_TYPE)).thenReturn(typeResource);
		Mockito.when(resourceResolver.getResource(typeResource, "cache")).thenReturn(cacheResource);
		setUpCacheResourceWithoutValidityTime();

		Mockito.when(valueMap.get(CacheConstants.CACHE_VALIDITY_TIME, DEFAULT_TIME)).thenReturn(30);

		//when
		ResourceTypeCacheConfiguration actual = testedObject.readComponentConfiguration(request, DEFAULT_TIME);

		//then
		assertThat(actual.isEnabled(), is(true));
		assertThat(actual.getCacheLevel(), is(1));
		assertThat(actual.getResourceType(), is(RESOURCE_TYPE));
		assertThat(actual.getResourceTypePath(), is(nullValue()));
		assertThat(actual.getTime(), is(30));
		assertThat(actual.getInvalidatePaths().size(), is(3));
	}

	@Test
	public void shouldCreateResourceTypeCachConfigurationWhenResourceTypeIsConfiguredInJcrWithDefaultTimeIfNotProvided() {
		//given
		setUpRequest();
		Mockito.when(resourceResolver.getResource("/apps/" + RESOURCE_TYPE)).thenReturn(typeResource);
		Mockito.when(resourceResolver.getResource(typeResource, "cache")).thenReturn(cacheResource);
		setUpCacheResourceWithoutValidityTime();

		Mockito.when(valueMap.get(CacheConstants.CACHE_VALIDITY_TIME, DEFAULT_TIME)).thenReturn(null);

		//when
		ResourceTypeCacheConfiguration actual = testedObject.readComponentConfiguration(request, DEFAULT_TIME);

		//then
		assertThat(actual.isEnabled(), is(true));
		assertThat(actual.getCacheLevel(), is(1));
		assertThat(actual.getResourceType(), is(RESOURCE_TYPE));
		assertThat(actual.getResourceTypePath(), is(nullValue()));
		assertThat(actual.getTime(), is(DEFAULT_TIME));
		assertThat(actual.getInvalidatePaths().size(), is(3));
	}

	private void setUpCacheResourceWithoutValidityTime() {
		Mockito.when(resourceResolver.getResource(typeResource, CacheConstants.CACHE_ENABLED)).thenReturn(cacheResource);
		Mockito.when(cacheResource.adaptTo(ValueMap.class)).thenReturn(valueMap);
		Mockito.when(valueMap.get(CacheConstants.CACHE_ENABLED, false)).thenReturn(true);
		Mockito.when(valueMap.get(CacheConstants.CACHE_LEVEL, Integer.MIN_VALUE)).thenReturn(1);
		Mockito.when(valueMap.get(CacheConstants.CACHE_INVALIDATE_ON_SELF, true)).thenReturn(true);
		Mockito.when(valueMap.get(CacheConstants.CACHE_INVALIDATE_FIELDS)).thenReturn(XML_CACHE_INVALIDATE_PATHS);
		Mockito.when(valueMap.get(CacheConstants.CACHE_INVALIDATE_PATHS)).thenReturn(XML_CACHE_INVALIDATE_FIELDS);
	}
}
