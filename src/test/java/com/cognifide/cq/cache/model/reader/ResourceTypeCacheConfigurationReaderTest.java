package com.cognifide.cq.cache.model.reader;

import com.cognifide.cq.cache.model.CacheConstants;
import com.cognifide.cq.cache.model.ResourceResolverStub;
import com.cognifide.cq.cache.model.ResourceTypeCacheConfiguration;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.cognifide.cq.cache.test.utils.ResourceStub;
import com.cognifide.cq.cache.test.utils.SlingHttpServletRequestStub;

/**
 * @author Bartosz Rudnicki
 */
public class ResourceTypeCacheConfigurationReaderTest {

	private static final int DEFAULT_TIME = 1000;

	private static final int CONSOLE_TIME = 100;

	private static final int CONSOLE_CACHE_LEVEL = 20;

	private static final int XML_TIME = 50;

	private static final int XML_CACHE_LEVEL = 10;

	private static final boolean XML_INVALIDATE_ON_SELF = false;

	private static final String FIELD_1_NAME = "field1";

	private static final String FIELD_2_NAME = "field2";

	private static final String FIELD_1_VALUE = "field1Value";

	private static final String FIELD_2_VALUE = null;

	private static final String[] XML_CACHE_INVALIDATE_FIELDS = {FIELD_1_NAME, FIELD_2_NAME, null};

	private static final String INVALIDATE_PATH_1 = "/content/invalidate/path/1";

	private static final String INVALIDATE_PATH_2 = "/content/invalidate/path/2";

	private static final String[] XML_CACHE_INVALIDATE_PATHS = {INVALIDATE_PATH_1, INVALIDATE_PATH_2, null};

	private static final String RESOURCE_PATH = "/content/resource";

	private static final String RESOURCE_TYPE = "resourceType";

	private static final String RESOURCE_TYPE_PATH = "/apps/resourceType";

	private static final String CACHE_RESOURCE_PATH = RESOURCE_TYPE + "/cache";

	private static final String INVALIDATION_PATH = "%s.*";

	private ResourceTypeCacheConfigurationReaderImpl reader;

	private SlingHttpServletRequestStub request;

	private ResourceResolverStub resourceResolver;

	private ResourceStub requestedResource;

	private ResourceStub typeResource;

	private ResourceStub cacheResource;

	@Before
	public void setUp() {
		reader = new ResourceTypeCacheConfigurationReaderImpl();

		requestedResource = new ResourceStub();
		requestedResource.setResourceType(RESOURCE_TYPE);
		requestedResource.setPath(RESOURCE_PATH);
		requestedResource.put(FIELD_1_NAME, FIELD_1_VALUE);
		requestedResource.put(FIELD_2_NAME, FIELD_2_VALUE);

		typeResource = new ResourceStub();
		typeResource.setPath(RESOURCE_TYPE);

		cacheResource = new ResourceStub();
		cacheResource.setPath(CACHE_RESOURCE_PATH);

		resourceResolver = new ResourceResolverStub();
		resourceResolver.getResources().put(RESOURCE_PATH, requestedResource);
		resourceResolver.getResources().put(RESOURCE_TYPE_PATH, typeResource);
		resourceResolver.getResources().put(CACHE_RESOURCE_PATH, cacheResource);

		request = new SlingHttpServletRequestStub();
		request.setResource(requestedResource);
		request.setResourceResolver(resourceResolver);
	}

	@After
	public void tearDown() {

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testComponentLoadedFromCacheWithAllFieldsSet() {
		cacheResource.put(CacheConstants.CACHE_ENABLED, true);
		cacheResource.put(CacheConstants.CACHE_VALIDITY_TIME, XML_TIME);
		cacheResource.put(CacheConstants.CACHE_LEVEL, XML_CACHE_LEVEL);
		cacheResource.put(CacheConstants.CACHE_INVALIDATE_ON_SELF, XML_INVALIDATE_ON_SELF);
		cacheResource.put(CacheConstants.CACHE_INVALIDATE_FIELDS, XML_CACHE_INVALIDATE_FIELDS);
		cacheResource.put(CacheConstants.CACHE_INVALIDATE_PATHS, XML_CACHE_INVALIDATE_PATHS);

		ResourceTypeCacheConfiguration config = reader.readComponentConfiguration(request, DEFAULT_TIME);

		assertNotNull(config);
		assertTrue(config.isEnabled());

		assertEquals(XML_CACHE_LEVEL, config.getCacheLevel());
		assertEquals(RESOURCE_TYPE, config.getResourceType());
		assertEquals(RESOURCE_TYPE, config.getResourceTypePath());
		assertEquals(XML_TIME, config.getTime());

		assertEquals(3, config.getInvalidatePaths().size());
		List<String> regexes = new ArrayList<String>();
		for (Pattern pattern : config.getInvalidatePaths()) {
			regexes.add(pattern.pattern());
		}
		assertTrue(regexes.contains(String.format(INVALIDATION_PATH, FIELD_1_VALUE)));
		assertTrue(regexes.contains(INVALIDATE_PATH_1));
		assertTrue(regexes.contains(INVALIDATE_PATH_2));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testComponentLoadedFromCacheWithNoFieldsSet() {
		cacheResource.put(CacheConstants.CACHE_ENABLED, false);

		ResourceTypeCacheConfiguration config = reader.readComponentConfiguration(request, DEFAULT_TIME);

		assertNotNull(config);
		assertFalse(config.isEnabled());

		assertEquals(Integer.MIN_VALUE, config.getCacheLevel());
		assertEquals(RESOURCE_TYPE, config.getResourceType());
		assertEquals(RESOURCE_TYPE, config.getResourceTypePath());
		assertEquals(DEFAULT_TIME, config.getTime());

		assertEquals(1, config.getInvalidatePaths().size());
		assertEquals(config.getInvalidatePaths().get(0).pattern(),
				String.format(INVALIDATION_PATH, RESOURCE_PATH));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testComponentLoadedFromCacheWithAllFieldsSetAndOnAbsolutePaths() {
		requestedResource.setResourceType(RESOURCE_TYPE_PATH);

		cacheResource.put(CacheConstants.CACHE_ENABLED, true);
		cacheResource.put(CacheConstants.CACHE_VALIDITY_TIME, XML_TIME);
		cacheResource.put(CacheConstants.CACHE_LEVEL, XML_CACHE_LEVEL);
		cacheResource.put(CacheConstants.CACHE_INVALIDATE_ON_SELF, XML_INVALIDATE_ON_SELF);
		cacheResource.put(CacheConstants.CACHE_INVALIDATE_FIELDS, new Object[]{FIELD_1_NAME, FIELD_2_NAME,
			null});
		cacheResource.put(CacheConstants.CACHE_INVALIDATE_PATHS, INVALIDATE_PATH_1);

		ResourceTypeCacheConfiguration config = reader.readComponentConfiguration(request, DEFAULT_TIME);

		assertNotNull(config);
		assertTrue(config.isEnabled());

		assertEquals(XML_CACHE_LEVEL, config.getCacheLevel());
		assertEquals(RESOURCE_TYPE_PATH, config.getResourceType());
		assertEquals(RESOURCE_TYPE, config.getResourceTypePath());
		assertEquals(XML_TIME, config.getTime());

		assertEquals(2, config.getInvalidatePaths().size());
		List<String> regexes = new ArrayList<String>();
		for (Pattern pattern : config.getInvalidatePaths()) {
			regexes.add(pattern.pattern());
		}
		assertTrue(regexes.contains(String.format(INVALIDATION_PATH, FIELD_1_VALUE)));
		assertTrue(regexes.contains(INVALIDATE_PATH_1));
	}

	@SuppressWarnings("unchecked")
	@Test(expected = IllegalArgumentException.class)
	public void testComponentLoadedFromCacheWithIncorrectInvalidateFieldsSet() {
		requestedResource.setResourceType(RESOURCE_TYPE_PATH);

		cacheResource.put(CacheConstants.CACHE_ENABLED, true);
		cacheResource.put(CacheConstants.CACHE_VALIDITY_TIME, XML_TIME);
		cacheResource.put(CacheConstants.CACHE_LEVEL, XML_CACHE_LEVEL);
		cacheResource.put(CacheConstants.CACHE_INVALIDATE_ON_SELF, XML_INVALIDATE_ON_SELF);
		cacheResource.put(CacheConstants.CACHE_INVALIDATE_FIELDS, this);
		cacheResource.put(CacheConstants.CACHE_INVALIDATE_PATHS, INVALIDATE_PATH_1);

		reader.readComponentConfiguration(request, DEFAULT_TIME);
	}
}
