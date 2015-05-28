package com.cognifide.cq.cache.model;

import com.cognifide.cq.cache.model.key.CacheKeyGeneratorImpl;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.Resource;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * @author Bartosz Rudnicki
 */
public class CacheKeyGeneratorImplTest {

	private static final String PATH = "/some/path";

	@Mock
	private SlingHttpServletRequest request;

	@Mock
	private RequestPathInfo requestPathInfo;

	@Mock
	private ResourceTypeCacheConfiguration resourceTypeCacheConfiguration;

	@Mock
	private Resource resource;

	private CacheKeyGeneratorImpl testedObject;

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@Before
	public void setUp() {
		testedObject = new CacheKeyGeneratorImpl();

		when(request.getResource()).thenReturn(resource);
		when(request.getRequestPathInfo()).thenReturn(requestPathInfo);
	}

	@Test
	public void testGenerateKeyFromResourceWithNegativeCacheLevel() {
		//given
		setUpResource("/some/path", null);
		setUpSelectorString("text.txt");
		when(resourceTypeCacheConfiguration.getCacheLevel()).thenReturn(-1);

		//then
		String actual = testedObject.generateKey(request, resourceTypeCacheConfiguration);

		//then
		assertThat(actual, is("/some/path.text.txt"));
	}

	private void setUpResource(String resourcePath, String resourceType) {
		when(resource.getPath()).thenReturn(resourcePath);
		when(resource.getResourceType()).thenReturn(resourceType);
	}

	private void setUpSelectorString(String selectorString) {
		when(requestPathInfo.getSelectorString()).thenReturn(selectorString);
	}

	@Test
	public void testGenerateKeyFromResourceWithZeroCacheLevel() {
		//given
		setUpResource(null, "/some/resource/path");
		when(resourceTypeCacheConfiguration.getCacheLevel()).thenReturn(0);

		//when
		String actual = testedObject.generateKey(request, resourceTypeCacheConfiguration);

		//then
		assertThat(actual, is("/some/resource/path"));
	}

	@Test
	public void testGenerateKeyFromResourceWithPositiveCacheLevel2() {
		//given
		setUpResource("/some/resource/path", "/some/resource/type/path");
		setUpSelectorString("txt");
		when(resourceTypeCacheConfiguration.getCacheLevel()).thenReturn(2);

		String actual = testedObject.generateKey(request, resourceTypeCacheConfiguration);

		//then
		assertThat(actual, is("/some/resource/type/path/some/resource.txt"));
	}

	@Test
	public void testGenerateKeyFromResourceWithPositiveCacheLevel3() {
		//given
		setUpResource("/some/resource/path", "some/resource/type/path");
		when(resourceTypeCacheConfiguration.getCacheLevel()).thenReturn(3);

		//when
		String actual = testedObject.generateKey(request, resourceTypeCacheConfiguration);

		//then
		assertThat(actual, is("/apps/some/resource/type/path/some/resource/path"));
	}
}
