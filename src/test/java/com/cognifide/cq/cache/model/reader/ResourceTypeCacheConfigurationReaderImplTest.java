package com.cognifide.cq.cache.model.reader;

import com.cognifide.cq.cache.definition.ResourceTypeCacheDefinition;
import com.cognifide.cq.cache.model.ResourceTypeCacheConfiguration;
import com.cognifide.cq.cache.model.alias.PathAliasStore;
import com.google.common.base.Optional;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class ResourceTypeCacheConfigurationReaderImplTest {

	private static final String RESOURCE_TYPE = "resource/type";

	@Mock
	private SlingHttpServletRequest request;

	@Mock
	private Resource resource;

	@Mock
	private PathAliasStore pathAliasStore;

	@Mock
	private ResourceTypeCacheDefinition resourceTypeCacheDefinition;

	private ResourceTypeCacheConfigurationReaderImpl testedObject;

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@Before
	public void setUp() {
		testedObject = new ResourceTypeCacheConfigurationReaderImpl();
		Whitebox.setInternalState(testedObject, "pathAliasStore", pathAliasStore);
	}

	@Test
	public void shouldNotReturnConfigurationWhenThereIsNoDefinition() {
		//given
		setUpRequest();

		//when
		Optional<ResourceTypeCacheConfiguration> actual = testedObject.readComponentConfiguration(request);

		//then
		assertThat(actual.isPresent()).isFalse();
	}

	private void setUpRequest() {
		when(request.getResource()).thenReturn(resource);
		when(resource.getResourceType()).thenReturn(RESOURCE_TYPE);
	}

	private void setUpResourceTypeCacheDefinition() {
		when(resourceTypeCacheDefinition.getResourceType()).thenReturn(RESOURCE_TYPE);

		testedObject.bindResourceTypeCacheDefinition(resourceTypeCacheDefinition);
	}

	@Test
	public void shouldReturnConfigurationWhenThereIsDefinition() {
		//given
		setUpRequest();
		setUpResourceTypeCacheDefinition();

		//when
		Optional<ResourceTypeCacheConfiguration> actual = testedObject.readComponentConfiguration(request);

		//then
		assertThat(actual.isPresent()).isTrue();
		assertThat(actual.get().getResourceType()).isEqualTo(RESOURCE_TYPE);
	}

}
