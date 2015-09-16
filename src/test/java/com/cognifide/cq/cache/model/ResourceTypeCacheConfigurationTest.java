package com.cognifide.cq.cache.model;

import com.cognifide.cq.cache.definition.ResourceTypeCacheDefinition;
import com.cognifide.cq.cache.model.alias.PathAliasStore;
import org.apache.commons.lang.StringUtils;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class ResourceTypeCacheConfigurationTest {

	@Mock
	private ResourceTypeCacheDefinition resourceTypeCacheDefinition;

	@Mock
	private PathAliasStore pathAliasStore;

	private ResourceTypeCacheConfiguration testedObject;

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@Before
	public void setUp() {
		when(resourceTypeCacheDefinition.getResourceType()).thenReturn("/resource/type");

		testedObject = new ResourceTypeCacheConfiguration(resourceTypeCacheDefinition, pathAliasStore);
	}

	@Test
	public void shouldNotAddEmptyInvalidationPathPrefix() {
		//when
		testedObject.addInvalidationPathPrefix(null);
		testedObject.addInvalidationPathPrefix(StringUtils.EMPTY);
		testedObject.addInvalidationPathPrefix("prefix");

		//then
		assertThat(testedObject.getInvalidationPathPrefixes()).hasSize(1);
	}

}
