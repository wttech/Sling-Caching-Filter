package com.cognifide.cq.cache.model;

import java.util.regex.Pattern;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Note: this tests both ResourceTypeCacheConfiguration and CacheConfigurationEntry.
 *
 * @author Bartosz Rudnicki
 */
@Ignore
public class CacheConfigurationClassesTest {

	private static final String RESOURCE_TYPE = "resourceType";

	private static final String RESOURCE_TYPE_PATH = "resourceTypePath";

	private static final String REGEX_1 = "regex1.*";

	private static final String REGEX_2 = "regex2.*";

	private static final int TIME = 100;

	private static final int CACHE_LEVEL = 5;

	private ResourceTypeCacheConfiguration config;

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {

	}

	@Test
	public void testConstructors() {
		config = new ResourceTypeCacheConfiguration(RESOURCE_TYPE, TIME);
		assertEquals(RESOURCE_TYPE, config.getResourceType());
//		assertEquals(TIME, config.getValidityTimeInSeconds());
//		assertFalse(config.isEnabled());
//		assertEquals(Integer.MIN_VALUE, config.getCacheLevel());
//
//		config = new ResourceTypeCacheConfiguration(RESOURCE_TYPE, TIME, CACHE_LEVEL);
//		assertEquals(RESOURCE_TYPE, config.getResourceType());
//		assertEquals(TIME, config.getTime());
//		assertFalse(config.isEnabled());
//		assertEquals(CACHE_LEVEL, config.getCacheLevel());
	}

	@Test
	public void testGettersAndSetters() {
		config = new ResourceTypeCacheConfiguration(RESOURCE_TYPE, TIME);

//		assertEquals(Integer.MIN_VALUE, config.getCacheLevel());
//		config.setCacheLevel(CACHE_LEVEL);
//		assertEquals(CACHE_LEVEL, config.getCacheLevel());

		assertFalse(config.isEnabled());
		config.setEnabled(true);
		assertTrue(config.isEnabled());
		config.setEnabled(false);
		assertFalse(config.isEnabled());

		assertEquals(RESOURCE_TYPE, config.getResourceType());
		config.setResourceType(RESOURCE_TYPE_PATH);
		assertEquals(RESOURCE_TYPE_PATH, config.getResourceType());

		assertNull(config.getResourceTypePath());
		config.setResourceTypePath(RESOURCE_TYPE_PATH);
		assertEquals(RESOURCE_TYPE_PATH, config.getResourceTypePath());

//		assertEquals(TIME, config.getTime());
//		config.setValidityTimeInSeconds(TIME + TIME);
//		assertEquals(TIME + TIME, config.getTime());

		assertTrue(config.getInvalidatePaths().isEmpty());
		config.addInvalidatePath(Pattern.compile(REGEX_1));
		assertEquals(1, config.getInvalidatePaths().size());
		assertEquals(REGEX_1, config.getInvalidatePaths().get(0).pattern());
		config.addInvalidatePath(REGEX_2);
		assertEquals(2, config.getInvalidatePaths().size());
		assertEquals(REGEX_2, config.getInvalidatePaths().get(1).pattern());
	}
}
