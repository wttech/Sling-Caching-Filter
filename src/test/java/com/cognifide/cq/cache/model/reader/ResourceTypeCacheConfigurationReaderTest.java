package com.cognifide.cq.cache.model.reader;

import com.cognifide.cq.cache.definition.osgi.OsgiResourceTypeCacheDefinition;
import com.cognifide.cq.cache.model.ResourceResolverStub;
import com.cognifide.cq.cache.model.ResourceTypeCacheConfiguration;
import com.cognifide.cq.cache.test.utils.ComponentContextStub;
import com.cognifide.cq.cache.test.utils.ResourceStub;
import com.cognifide.cq.cache.test.utils.SlingHttpServletRequestStub;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

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

	private static final String INVALIDATION_PATH = "%s.*";

	private ResourceTypeCacheConfigurationReaderImpl reader;

	private SlingHttpServletRequestStub request;

	private ResourceResolverStub resourceResolver;

	private ResourceStub requestedResource;

	private ResourceStub typeResource;

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

		resourceResolver = new ResourceResolverStub();
		resourceResolver.getResources().put(RESOURCE_PATH, requestedResource);
		resourceResolver.getResources().put(RESOURCE_TYPE_PATH, typeResource);

		request = new SlingHttpServletRequestStub();
		request.setResource(requestedResource);
		request.setResourceResolver(resourceResolver);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testComponentLoadedFromCacheWithAllFieldsSet() {
		setUpResourceTypeDefinition();

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

	private void setUpResourceTypeDefinition() {
		OsgiResourceTypeCacheDefinition osgiResourceTypeCacheDefinition = new OsgiResourceTypeCacheDefinition();
		ComponentContextStub componentContext = new ComponentContextStub();
		componentContext.put("cache.config.active", true);
		componentContext.put("cache.config.resource.type", RESOURCE_TYPE);
		componentContext.put("cache.config.validity.time", XML_TIME);
		componentContext.put("cache.config.cache.level", XML_CACHE_LEVEL);
		componentContext.put("cache.config.invalidate.on.self", XML_INVALIDATE_ON_SELF);
		componentContext.put("cache.config.invalidate.on.referenced.fields", XML_CACHE_INVALIDATE_FIELDS);
		componentContext.put("cache.config.invalidate.on.paths", XML_CACHE_INVALIDATE_PATHS);
		osgiResourceTypeCacheDefinition.activate(componentContext);
		reader.bindResourceTypeCacheDefinition(osgiResourceTypeCacheDefinition);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testComponentLoadedFromCacheWithNoFieldsSet() {
		setUpDisabledResourceTypeDefinition();

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

	private void setUpDisabledResourceTypeDefinition() {
		OsgiResourceTypeCacheDefinition osgiResourceTypeCacheDefinition = new OsgiResourceTypeCacheDefinition();
		ComponentContextStub componentContext = new ComponentContextStub();
		componentContext.put("cache.config.active", false);
		componentContext.put("cache.config.resource.type", RESOURCE_PATH);
		osgiResourceTypeCacheDefinition.activate(componentContext);
		reader.bindResourceTypeCacheDefinition(osgiResourceTypeCacheDefinition);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testComponentLoadedFromCacheWithAllFieldsSetAndOnAbsolutePaths() {
		requestedResource.setResourceType(RESOURCE_TYPE_PATH);

		setUpResourceTypeDefinitionWithAdditionalInvalidateOptions();

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

	private void setUpResourceTypeDefinitionWithAdditionalInvalidateOptions() {
		OsgiResourceTypeCacheDefinition osgiResourceTypeCacheDefinition = new OsgiResourceTypeCacheDefinition();
		ComponentContextStub componentContext = new ComponentContextStub();
		componentContext.put("cache.config.active", true);
		componentContext.put("cache.config.resource.type", RESOURCE_TYPE_PATH);
		componentContext.put("cache.config.validity.time", XML_TIME);
		componentContext.put("cache.config.cache.level", XML_CACHE_LEVEL);
		componentContext.put("cache.config.invalidate.on.self", XML_INVALIDATE_ON_SELF);
		componentContext.put("cache.config.invalidate.on.referenced.fields", new String[]{FIELD_1_NAME, FIELD_2_NAME, null});
		componentContext.put("cache.config.invalidate.on.paths", new String[]{INVALIDATE_PATH_1});
		osgiResourceTypeCacheDefinition.activate(componentContext);
		reader.bindResourceTypeCacheDefinition(osgiResourceTypeCacheDefinition);
	}
}
