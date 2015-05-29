package com.cognifide.cq.cache.model.key;

import com.cognifide.cq.cache.definition.CacheConfigurationEntry;
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

	private static final String RESOURCE_PATH = "/resource/path";

	private static final String LONG_RESOURCE_PATH = "/very/long/resource/path";

	private static final String RESOURCE_TYPE_PATH = "/resource/type/path";

	@Mock
	private SlingHttpServletRequest request;

	@Mock
	private RequestPathInfo requestPathInfo;

	@Mock
	private Resource resource;

	@Mock
	private CacheConfigurationEntry cacheConfigurationEntry;

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
		setUpResource(RESOURCE_PATH, null);
		setUpSelectorString("text.txt");
		when(cacheConfigurationEntry.getCacheLevel()).thenReturn(-1);

		//then
		String actual = testedObject.generateKey(request, cacheConfigurationEntry);

		//then
		assertThat(actual, is("/resource/path.text.txt"));
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
		setUpResource(null, RESOURCE_TYPE_PATH);
		when(cacheConfigurationEntry.getCacheLevel()).thenReturn(0);

		//when
		String actual = testedObject.generateKey(request, cacheConfigurationEntry);

		//then
		assertThat(actual, is(RESOURCE_TYPE_PATH));
	}

	@Test
	public void testGenerateKeyFromResourceWithPositiveCacheLevel2() {
		//given
		setUpResource(RESOURCE_PATH, RESOURCE_TYPE_PATH);
		setUpSelectorString("txt");
		when(cacheConfigurationEntry.getCacheLevel()).thenReturn(2);

		String actual = testedObject.generateKey(request, cacheConfigurationEntry);

		//then
		assertThat(actual, is("/resource/type/path/resource/path.txt"));
	}

	@Test
	public void testGenerateKeyFromResourceWithPositiveCacheLevel3() {
		//given
		setUpResource(LONG_RESOURCE_PATH, "resource/type/path");
		when(cacheConfigurationEntry.getCacheLevel()).thenReturn(3);

		//when
		String actual = testedObject.generateKey(request, cacheConfigurationEntry);

		//then
		assertThat(actual, is("/apps/resource/type/path/very/long/resource"));
	}
}
