package com.cognifide.cq.cache.model;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of {@link CacheConfigurationParser} using regular expressions.
 * 
 * Example entry: exampleSite/components/header:2000:0 It means that exampleSite header will be cached
 * site-wide for 2000 seconds.
 * 
 * @author Jakub Malecki
 */
public class CacheConfigurationParserImpl implements CacheConfigurationParser {

	private static final Log LOG = LogFactory.getLog(CacheConfigurationParserImpl.class);

	private static final Pattern FULL_PATTERN = Pattern.compile("^[\\w/]+:\\d+:[-]{0,1}\\d+$");

	private static final Pattern TIME_PATTERN = Pattern.compile("^[\\w/]+:\\d+$");

	private static final Pattern SHORT_PATTERN = Pattern.compile("^[\\w/]+$");

	private static final int DEFAULT_TIME = Integer.MIN_VALUE;

	private static final int DEFAULT_CACHE_LEVEL = Integer.MIN_VALUE;

	@Override
	public CacheConfigurationEntry parseEntry(String entry) {
		boolean matchesFull = FULL_PATTERN.matcher(entry).matches();
		boolean matchesTime = TIME_PATTERN.matcher(entry).matches();
		boolean matchesShort = SHORT_PATTERN.matcher(entry).matches();

		if (!(matchesFull || matchesTime || matchesShort)) {
			return null; // incorrect entry, leave it
		}

		int time = DEFAULT_TIME;
		int cacheLevel = DEFAULT_CACHE_LEVEL;
		String[] parts = entry.split(":");

		if (matchesFull) {
			try {
				cacheLevel = Integer.parseInt(parts[2]);
			} catch (NumberFormatException e) {
				// fall through, leave default value
				LOG.warn("invalid cache level for entry: " + entry);
			}
		}

		if (matchesFull || matchesTime) {
			try {
				time = Integer.parseInt(parts[1]);
			} catch (NumberFormatException e) {
				// fall through, leave default value
				LOG.warn("invalid cache timeout for entry: " + entry);
			}
		}

		return new CacheConfigurationEntry(parts[0], time, cacheLevel);
	}

	@Override
	public Map<String, CacheConfigurationEntry> parseEntries(String[] entries) {
		Map<String, CacheConfigurationEntry> result = new HashMap<String, CacheConfigurationEntry>();
		for (String entry : entries) {
			CacheConfigurationEntry configEntry = parseEntry(entry);
			if (configEntry != null) {
				result.put(configEntry.getResourceType(), configEntry);
			}
		}

		return result;
	}

}
