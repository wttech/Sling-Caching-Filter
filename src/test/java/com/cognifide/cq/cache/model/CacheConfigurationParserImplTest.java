package com.cognifide.cq.cache.model;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Bartosz Rudnicki
 */
public class CacheConfigurationParserImplTest {

	private static final String RESOURCE_TYPE_1 = "resourceType1";

	private static final String RESOURCE_TYPE_2 = "resourceType2";

	private static final String RESOURCE_TYPE_3 = "resourceType2";

	private static final int TIME = 100;

	private static final int CACHE_LEVEL = 5;

	private static final int DEFAULT_TIME = Integer.MIN_VALUE;

	private static final int DEFAULT_CACHE_LEVEL = Integer.MIN_VALUE;

	private static final String INCORRECT_NUMERIC_INTEGER = "9999999999999999999999999999999999999999999999";

	private CacheConfigurationParserImpl parser;

	@Before
	public void setUp() {
		parser = new CacheConfigurationParserImpl();
	}

	@After
	public void tearDown() {
	}

	private String createEntry(Object... values) {
		StringBuilder builder = new StringBuilder();
		for (Object value : values) {
			if (builder.length() > 0) {
				builder.append(":");
			}
			builder.append(value);
		}
		return builder.toString();
	}

	@Test
	public void testParseEntryFullPattern() {
		CacheConfigurationEntry entry = parser.parseEntry(createEntry(RESOURCE_TYPE_1, TIME, CACHE_LEVEL));
		assertNotNull(entry);
		assertEquals(RESOURCE_TYPE_1, entry.getResourceType());
		assertEquals(TIME, entry.getTime());
		assertEquals(CACHE_LEVEL, entry.getCacheLevel());
	}

	@Test
	public void testParseEntryFullPatternWithIncorrectCacheLevel1() {
		CacheConfigurationEntry entry = parser
				.parseEntry(createEntry(RESOURCE_TYPE_1, TIME, RESOURCE_TYPE_1));
		assertNull(entry);
	}

	@Test
	public void testParseEntryFullPatternWithIncorrectCacheLevel2() {
		CacheConfigurationEntry entry = parser.parseEntry(createEntry(RESOURCE_TYPE_1, TIME,
				INCORRECT_NUMERIC_INTEGER));
		assertNotNull(entry);
		assertEquals(RESOURCE_TYPE_1, entry.getResourceType());
		assertEquals(TIME, entry.getTime());
		assertEquals(DEFAULT_CACHE_LEVEL, entry.getCacheLevel());
	}

	@Test
	public void testParseEntryFullPatternWithIncorrectTime1() {
		CacheConfigurationEntry entry = parser.parseEntry(createEntry(RESOURCE_TYPE_1, RESOURCE_TYPE_1,
				CACHE_LEVEL));
		assertNull(entry);
	}

	@Test
	public void testParseEntryFullPatternWithIncorrectTime2() {
		CacheConfigurationEntry entry = parser.parseEntry(createEntry(RESOURCE_TYPE_1,
				INCORRECT_NUMERIC_INTEGER, CACHE_LEVEL));
		assertNotNull(entry);
		assertEquals(RESOURCE_TYPE_1, entry.getResourceType());
		assertEquals(DEFAULT_TIME, entry.getTime());
		assertEquals(CACHE_LEVEL, entry.getCacheLevel());
	}

	@Test
	public void testParseEntryFullPatternWithIncorrectCacheLevelAndTime() {
		CacheConfigurationEntry entry = parser.parseEntry(createEntry(RESOURCE_TYPE_1,
				INCORRECT_NUMERIC_INTEGER, INCORRECT_NUMERIC_INTEGER));
		assertNotNull(entry);
		assertEquals(RESOURCE_TYPE_1, entry.getResourceType());
		assertEquals(DEFAULT_TIME, entry.getTime());
		assertEquals(DEFAULT_CACHE_LEVEL, entry.getCacheLevel());
	}

	@Test
	public void testParseEntryTimePattern() {
		CacheConfigurationEntry entry = parser.parseEntry(createEntry(RESOURCE_TYPE_1, TIME));
		assertNotNull(entry);
		assertEquals(RESOURCE_TYPE_1, entry.getResourceType());
		assertEquals(TIME, entry.getTime());
		assertEquals(DEFAULT_CACHE_LEVEL, entry.getCacheLevel());
	}

	@Test
	public void testParseEntryTimePatternWithIncorrectTime1() {
		CacheConfigurationEntry entry = parser.parseEntry(createEntry(RESOURCE_TYPE_1, RESOURCE_TYPE_1));
		assertNull(entry);
	}

	@Test
	public void testParseEntryTimePatternWithIncorrectTime2() {
		CacheConfigurationEntry entry = parser.parseEntry(createEntry(RESOURCE_TYPE_1,
				INCORRECT_NUMERIC_INTEGER));
		assertNotNull(entry);
		assertEquals(RESOURCE_TYPE_1, entry.getResourceType());
		assertEquals(DEFAULT_TIME, entry.getTime());
		assertEquals(DEFAULT_CACHE_LEVEL, entry.getCacheLevel());
	}

	@Test
	public void testShortPattern() {
		CacheConfigurationEntry entry = parser.parseEntry(createEntry(RESOURCE_TYPE_1));
		assertNotNull(entry);
		assertEquals(RESOURCE_TYPE_1, entry.getResourceType());
		assertEquals(DEFAULT_TIME, entry.getTime());
		assertEquals(DEFAULT_CACHE_LEVEL, entry.getCacheLevel());
	}

	@Test
	public void testParseEntryIncorrect() {
		CacheConfigurationEntry entry = parser.parseEntry(createEntry(RESOURCE_TYPE_1, RESOURCE_TYPE_1,
				RESOURCE_TYPE_1, RESOURCE_TYPE_1));
		assertNull(entry);
	}

	@Test
	public void testParseEntries() {
		String[] entries = new String[] { createEntry(RESOURCE_TYPE_1),
				createEntry(RESOURCE_TYPE_2, RESOURCE_TYPE_2),
				createEntry(RESOURCE_TYPE_3, TIME, CACHE_LEVEL) };

		Map<String, CacheConfigurationEntry> parsedEntries = parser.parseEntries(entries);
		assertNotNull(parsedEntries);
		assertEquals(2, parsedEntries.size());
		assertTrue(parsedEntries.containsKey(RESOURCE_TYPE_1));
		assertTrue(parsedEntries.containsKey(RESOURCE_TYPE_3));
		assertEquals(DEFAULT_TIME, parsedEntries.get(RESOURCE_TYPE_1).getTime());
		assertEquals(DEFAULT_CACHE_LEVEL, parsedEntries.get(RESOURCE_TYPE_1).getCacheLevel());
		assertEquals(TIME, parsedEntries.get(RESOURCE_TYPE_3).getTime());
		assertEquals(CACHE_LEVEL, parsedEntries.get(RESOURCE_TYPE_3).getCacheLevel());

	}
}
