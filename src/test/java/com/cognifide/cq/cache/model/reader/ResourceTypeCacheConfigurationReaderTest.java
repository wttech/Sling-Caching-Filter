package com.cognifide.cq.cache.model.reader;

import com.cognifide.cq.cache.definition.ResourceTypeCacheDefinition;
import com.cognifide.cq.cache.filter.osgi.CacheConfiguration;
import com.cognifide.cq.cache.model.ResourceTypeCacheConfiguration;
import com.cognifide.cq.cache.model.alias.PathAliasStore;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
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
	private CacheConfiguration cacheConfiguration;

	@Mock
	private ResourceTypeCacheDefinition resourceTypeCacheDefinition;

	@Mock
	private PathAliasStore pathAliasStore;

	@InjectMocks
	private ResourceTypeCacheConfigurationReaderImpl testedObject = new ResourceTypeCacheConfigurationReaderImpl();

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@Test
	public void shouldCreateCustomResourceTypeCachConfigurationWhenResourceTypeIsNotConfiguredInOsgiOrJcr() {
		//given
		setUpRequest();
		when(resourceResolver.getResource(RESOURCE_TYPE)).thenReturn(null);

		//when
		ResourceTypeCacheConfiguration actual = testedObject.readComponentConfiguration(request);

		//then
		assertThat(actual, is(nullValue()));
	}

	private void setUpRequest() {
		when(request.getResource()).thenReturn(resource);
		when(resource.getResourceType()).thenReturn(RESOURCE_TYPE);
		when(resource.getPath()).thenReturn(RESOURCE_PATH);
		when(request.getResourceResolver()).thenReturn(resourceResolver);
	}

	@Test
	public void shouldCreateResourceTypeCachConfigurationWhenResourceTypeIsConfiguredInOsgi() {
		//given
		setUpRequest();
		when(resourceResolver.getResource(RESOURCE_TYPE)).thenReturn(null);

		setUpResourceTypeCacheDefinitionWithoutValidityTime();
		when(resourceTypeCacheDefinition.getValidityTimeInSeconds()).thenReturn(30);

		testedObject.bindResourceTypeCacheDefinition(resourceTypeCacheDefinition);

		//when
		ResourceTypeCacheConfiguration actual = testedObject.readComponentConfiguration(request);

		//then
		assertThat(actual.isEnabled(), is(true));
		assertThat(actual.getCacheLevel(), is(1));
		assertThat(actual.getResourceType(), is(RESOURCE_TYPE));
		assertThat(actual.getResourceTypePath(), is(nullValue()));
		assertThat(actual.getValidityTimeInSeconds(), is(30));
		assertThat(actual.getInvalidatePaths().size(), is(3));
	}

	private void setUpResourceTypeCacheDefinitionWithoutValidityTime() {
		when(resourceTypeCacheDefinition.isEnabled()).thenReturn(true);
		when(resourceTypeCacheDefinition.getCacheLevel()).thenReturn(1);
		when(resourceTypeCacheDefinition.getResourceType()).thenReturn(RESOURCE_TYPE);
		when(resourceTypeCacheDefinition.isInvalidateOnSelf()).thenReturn(true);
		when(resourceTypeCacheDefinition.getInvalidateOnPaths()).thenReturn(XML_CACHE_INVALIDATE_PATHS);
		when(resourceTypeCacheDefinition.getInvalidateOnReferencedFields()).thenReturn(XML_CACHE_INVALIDATE_FIELDS);
	}

	@Test
	public void shouldCreateResourceTypeCachConfigurationWhenResourceTypeIsConfiguredInOsgiWithDefaultTimeIfNotProvided() {
		//given
		setUpRequest();
		when(resourceResolver.getResource(RESOURCE_TYPE)).thenReturn(null);
		setUpCacheConfiguration();
		setUpResourceTypeCacheDefinitionWithoutValidityTime();
		when(resourceTypeCacheDefinition.getValidityTimeInSeconds()).thenReturn(null);

		testedObject.bindResourceTypeCacheDefinition(resourceTypeCacheDefinition);

		//when
		ResourceTypeCacheConfiguration actual = testedObject.readComponentConfiguration(request);

		//then
		assertThat(actual.isEnabled(), is(true));
		assertThat(actual.getCacheLevel(), is(1));
		assertThat(actual.getResourceType(), is(RESOURCE_TYPE));
		assertThat(actual.getResourceTypePath(), is(nullValue()));
		assertThat(actual.getValidityTimeInSeconds(), is(DEFAULT_TIME));
		assertThat(actual.getInvalidatePaths().size(), is(3));
	}

	private void setUpResourceResolver() {
		when(resourceResolver.getResource("/apps/" + RESOURCE_TYPE)).thenReturn(typeResource);
		when(resourceResolver.getResource(typeResource, "cache")).thenReturn(cacheResource);
	}

	private void setUpCacheConfiguration() {
		when(cacheConfiguration.getDuration()).thenReturn(DEFAULT_TIME);
	}
}
