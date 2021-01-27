/*
 * Copyright 2015 Wunderman Thompson Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cognifide.cq.cache.model;

import com.cognifide.cq.cache.definition.ResourceTypeCacheDefinition;
import com.cognifide.cq.cache.model.alias.PathAliasStore;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import java.util.regex.Pattern;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class DefinitionPathTranslatorTest {

	private static final String RESOURCE_TYPE_PATH = "resource/type/path";

	private static final String ABSOLUTE_RESOURCE_TYPE_PATH = "/apps/" + RESOURCE_TYPE_PATH;

	private static final String RESOURCE_CONTAINING_PAGE = "/resource/containing/page";

	private static final String[] INVALIDATE_ON_PATHS_ARRAY = new String[]{"/invalidate/path/1", "invalidate/path/2"};

	private static final String FIELD_NAME = "field";

	private static final String FIELD_VALUE = "/field/value";

	private static final String[] INVALIDATE_ON_FIELDS = new String[]{FIELD_NAME};

	@Mock
	private PathAliasStore pathAliasStore;

	@Mock
	private ResourceTypeCacheDefinition definition;

	@Mock
	private ResourceTypeCacheConfiguration configuration;

	@Mock
	private Resource resource;

	@Mock
	private ValueMap valueMap;

	@Mock
	private Resource resourceTypeResource;

	@Mock
	private ResourceResolver resourceResolver;

	@Mock
	private PageManager pageManager;

	@Mock
	private Page page;

	private DefinitionPathTranslator testedObject;

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@Before
	public void setUp() {
		testedObject = new DefinitionPathTranslator(pathAliasStore, definition, configuration, resource);
	}

	@Test
	public void configuratioShouldNotHaveInvalidationPathsWhenDefintionIsOff() {
		//given
		when(definition.isEnabled()).thenReturn(false);

		//when
		testedObject.translatePaths();

		//then
		verifyNoMoreInteractions(configuration);
	}

	@Test
	public void configuratioShouldHaveOneInvalidationPathWhenDefinitionHasAllOptionsOff() {
		//given
		when(definition.isEnabled()).thenReturn(true);
		when(definition.isInvalidateOnSelf()).thenReturn(false);
		when(definition.isInvalidateOnContainingPage()).thenReturn(false);
		when(definition.getInvalidateOnPaths()).thenReturn(new String[0]);
		when(definition.getInvalidateOnReferencedFields()).thenReturn(new String[0]);

		setUpRepository();

		//when
		testedObject.translatePaths();

		//then
		verify(configuration).addInvalidationPathPrefix(ABSOLUTE_RESOURCE_TYPE_PATH);
		verifyNoMoreInteractions(configuration);
	}

	private void setUpRepository() {
		when(resource.getPath()).thenReturn(RESOURCE_TYPE_PATH);
		when(resource.getResourceResolver()).thenReturn(resourceResolver);
		when(resource.adaptTo(ValueMap.class)).thenReturn(valueMap);
		when(valueMap.get(FIELD_NAME)).thenReturn(FIELD_VALUE);
		when(resourceResolver.getResource(ABSOLUTE_RESOURCE_TYPE_PATH)).thenReturn(resourceTypeResource);
		when(resourceTypeResource.getPath()).thenReturn(ABSOLUTE_RESOURCE_TYPE_PATH);
		when(resourceResolver.adaptTo(PageManager.class)).thenReturn(pageManager);
		when(pageManager.getContainingPage(resource)).thenReturn(page);
		when(page.getPath()).thenReturn(RESOURCE_CONTAINING_PAGE);
	}

	@Test
	public void configuratioShouldHaveTwoInvalidationPathsWhenDefinitionHasInvalidSelfOn() {
		//given
		when(definition.isEnabled()).thenReturn(true);
		when(definition.isInvalidateOnSelf()).thenReturn(true);
		when(definition.isInvalidateOnContainingPage()).thenReturn(false);
		when(definition.getInvalidateOnPaths()).thenReturn(new String[0]);
		when(definition.getInvalidateOnReferencedFields()).thenReturn(new String[0]);

		setUpRepository();

		//when
		testedObject.translatePaths();

		//then
		verify(configuration).addInvalidationPathPrefix(ABSOLUTE_RESOURCE_TYPE_PATH);
		verify(configuration).addInvalidationPathPrefix(RESOURCE_TYPE_PATH);
		verifyNoMoreInteractions(configuration);
	}

	@Test
	public void configuratioShouldHaveThreeInvalidationPathsWhenDefinitionHasInvalidOnContainingPage() {
		//given
		when(definition.isEnabled()).thenReturn(true);
		when(definition.isInvalidateOnSelf()).thenReturn(true);
		when(definition.isInvalidateOnContainingPage()).thenReturn(true);
		when(definition.getInvalidateOnPaths()).thenReturn(new String[0]);
		when(definition.getInvalidateOnReferencedFields()).thenReturn(new String[0]);

		setUpRepository();

		//when
		testedObject.translatePaths();

		//then
		verify(configuration).addInvalidationPathPrefix(ABSOLUTE_RESOURCE_TYPE_PATH);
		verify(configuration).addInvalidationPathPrefix(RESOURCE_TYPE_PATH);
		verify(configuration).addInvalidationPathPrefix(RESOURCE_CONTAINING_PAGE);
		verifyNoMoreInteractions(configuration);
	}

	@Test
	public void configuratioShouldHaveMultipleInvalidationPathsWhenDefinitionHasInvalidOnPaths() {
		//given
		when(definition.isEnabled()).thenReturn(true);
		when(definition.isInvalidateOnSelf()).thenReturn(true);
		when(definition.isInvalidateOnContainingPage()).thenReturn(true);
		when(definition.getInvalidateOnPaths()).thenReturn(INVALIDATE_ON_PATHS_ARRAY);
		when(definition.getInvalidateOnReferencedFields()).thenReturn(new String[0]);

		when(pathAliasStore.isAlias(anyString())).thenReturn(false);
		setUpRepository();

		//when
		testedObject.translatePaths();

		//then
		verify(configuration).addInvalidationPathPrefix(ABSOLUTE_RESOURCE_TYPE_PATH);
		verify(configuration).addInvalidationPathPrefix(RESOURCE_TYPE_PATH);
		verify(configuration).addInvalidationPathPrefix(RESOURCE_CONTAINING_PAGE);
		verify(configuration, times(2)).addInvalidationPattern(any(Pattern.class));
		verifyNoMoreInteractions(configuration);
	}

	@Test
	public void configuratioShouldHaveMultipleInvalidationPathsWhenDefinitionHasInvalidOnFields() {
		//given
		when(definition.isEnabled()).thenReturn(true);
		when(definition.isInvalidateOnSelf()).thenReturn(true);
		when(definition.isInvalidateOnContainingPage()).thenReturn(true);
		when(definition.getInvalidateOnPaths()).thenReturn(INVALIDATE_ON_PATHS_ARRAY);
		when(definition.getInvalidateOnReferencedFields()).thenReturn(INVALIDATE_ON_FIELDS);

		when(pathAliasStore.isAlias(anyString())).thenReturn(false);
		setUpRepository();

		//when
		testedObject.translatePaths();

		//then
		verify(configuration).addInvalidationPathPrefix(ABSOLUTE_RESOURCE_TYPE_PATH);
		verify(configuration).addInvalidationPathPrefix(RESOURCE_TYPE_PATH);
		verify(configuration).addInvalidationPathPrefix(RESOURCE_CONTAINING_PAGE);
		verify(configuration).addInvalidationPathPrefix(FIELD_VALUE);
		verify(configuration, times(2)).addInvalidationPattern(any(Pattern.class));
		verifyNoMoreInteractions(configuration);
	}

}
