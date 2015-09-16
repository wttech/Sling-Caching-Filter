package com.cognifide.cq.cache.model.key;

import com.cognifide.cq.cache.definition.CacheConfigurationEntry;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.Resource;
import static org.assertj.core.api.Assertions.assertThat;
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

	private static final String RESOURCE_PATH_LONG = "/very/long/resource/path";

	private static final String RESOURCE_PATH_WITHOUT_SLASH_AT_START = "resource/without/slash";

	private static final String RESOURCE_TYPE_PATH = "/resource/type/path";

	private static final String RESOURCE_TYPE_PATH_WITHOUT_SLASH_AT_START = "resource/type/path";

	private static final String SELECTOR = "txt";

	private static final String SELECTOR_LONG = "text.txt";

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
	public void shouldGenerateKeyFromResourceWithNegativeCacheLevel() {
		//given
		setUpResource(RESOURCE_PATH, null);
		setUpSelectorString(SELECTOR_LONG);
		when(cacheConfigurationEntry.getCacheLevel()).thenReturn(-1);

		//then
		String actual = testedObject.generateKey(request, cacheConfigurationEntry);

		//then
		assertThat(actual).isEqualTo("/resource/path.text.txt");
	}

	private void setUpResource(String resourcePath, String resourceType) {
		when(resource.getPath()).thenReturn(resourcePath);
		when(resource.getResourceType()).thenReturn(resourceType);
	}

	private void setUpSelectorString(String selectorString) {
		when(requestPathInfo.getSelectorString()).thenReturn(selectorString);
	}

	@Test
	public void shouldGenerateKeyFromResourceWithZeroCacheLevel() {
		//given
		setUpResource(null, RESOURCE_TYPE_PATH);
		when(cacheConfigurationEntry.getCacheLevel()).thenReturn(0);

		//when
		String actual = testedObject.generateKey(request, cacheConfigurationEntry);

		//then
		assertThat(actual).isEqualTo(RESOURCE_TYPE_PATH);
	}

	@Test
	public void shouldGenerateKeyFromResourceWithPositiveCacheLevel2() {
		//given
		setUpResource(RESOURCE_PATH, RESOURCE_TYPE_PATH);
		setUpSelectorString(SELECTOR);
		when(cacheConfigurationEntry.getCacheLevel()).thenReturn(2);

		String actual = testedObject.generateKey(request, cacheConfigurationEntry);

		//then
		assertThat(actual).isEqualTo("/resource/type/path/resource/path.txt");
	}

	@Test
	public void shouldGenerateKeyFromResourceWithPositiveCacheLevel3() {
		//given
		setUpResource(RESOURCE_PATH_LONG, RESOURCE_TYPE_PATH_WITHOUT_SLASH_AT_START);
		when(cacheConfigurationEntry.getCacheLevel()).thenReturn(3);

		//when
		String actual = testedObject.generateKey(request, cacheConfigurationEntry);

		//then
		assertThat(actual).isEqualTo("/apps/resource/type/path/very/long/resource");
	}

	@Test
	public void shouldGenerateKeyForResourceWithPathWithoutSlashAtStart() {
		//given
		setUpResource(RESOURCE_PATH_WITHOUT_SLASH_AT_START, RESOURCE_TYPE_PATH);
		when(cacheConfigurationEntry.getCacheLevel()).thenReturn(2);

		//when
		String actual = testedObject.generateKey(request, cacheConfigurationEntry);

		//then
		assertThat(actual).isEqualTo("/resource/type/pathresource/without");
	}
}
