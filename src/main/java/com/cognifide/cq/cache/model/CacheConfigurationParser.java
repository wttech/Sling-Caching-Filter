package com.cognifide.cq.cache.model;

import java.util.Map;

/**
 * Parses plain string configuration entries from Apache Felix to meaningful objects.
 * 
 * @author Jakub Malecki
 */
public interface CacheConfigurationParser {

	Map<String, CacheConfigurationEntry> parseEntries(String[] entries);

	CacheConfigurationEntry parseEntry(String entry);
}
