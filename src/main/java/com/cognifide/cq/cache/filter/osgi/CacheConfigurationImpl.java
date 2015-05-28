/*
 * Copyright 2015 Cognifide Polska Sp. z o. o..
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
package com.cognifide.cq.cache.filter.osgi;

import com.cognifide.cq.cache.model.alias.PathAlias;
import com.cognifide.cq.cache.model.alias.PathAliasReader;
import com.cognifide.cq.cache.model.alias.PathAliasStore;
import java.util.Properties;
import java.util.Set;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.osgi.service.component.ComponentContext;

@Component(label = "Sling Caching Filter", description = "Sling Caching Filter", metatype = true, immediate = true)
@Service
public class CacheConfigurationImpl implements CacheConfiguration {

	private static final String CACHE_PATH_KEY = "cache.path";

	private static final String CACHE_KEY_KEY = "cache.key";

	private static final String CACHE_USE_HOST_DOMAIN_KEY = "cache.use.host.domain.in.key";

	private static final boolean DEFAULT_FILTER_ENABLED = false;

	@Property(label = "Enabled", boolValue = DEFAULT_FILTER_ENABLED)
	private static final String PROPERTY_FILTER_ENABLED = "cache.config.enabled";

	private static final String[] DEFAULT_PATH_ALIASES = new String[]{"", ""};

	@Property(label = "Path aliases", value = {"", ""}, cardinality = Integer.MAX_VALUE)
	private static final String PROPERTY_PATH_ALIASES = "cache.config.pathaliases";

	private static final boolean DEFAULT_MEMORY = true;

	@Property(label = "Memory", boolValue = DEFAULT_MEMORY)
	private static final String PROPERTY_MEMORY = "cache.config.memory";

	private static final int DEFAULT_CAPACITY = 1000;

	@Property(label = "Capacity", intValue = DEFAULT_CAPACITY)
	private static final String PROPERTY_CAPACITY = "cache.config.capacity";

	private static final String DEFAULT_ALGORITHM = "com.cognifide.cq.cache.algorithm.LRUCache";

	@Property(label = "Algorithm", value = DEFAULT_ALGORITHM)
	private static final String PROPERTY_ALGORITHM = "cache.config.algorithm";

	private static final boolean DEFAULT_BLOCKING = false;

	@Property(label = "Blocking", boolValue = DEFAULT_BLOCKING)
	private static final String PROPERTY_BLOCKING = "cache.config.blocking";

	private static final boolean DEFAULT_UNLIMITED_DISK = false;

	@Property(label = "Unlimited disk", boolValue = DEFAULT_UNLIMITED_DISK)
	private static final String PROPERTY_UNLIMITED_DISK = "cache.config.unlimiteddisk";

	private static final String DEFAULT_PERSISTENCE_CLASS = "com.opensymphony.oscache.plugins.diskpersistence.HashDiskPersistenceListener";

	@Property(label = "Persistance class", value = DEFAULT_PERSISTENCE_CLASS)
	private static final String PROPERTY_PERSISTENCE_CLASS = "cache.config.persistenceclass";

	private static final String DEFAULT_PATH = "";

	@Property(label = "Path", value = DEFAULT_PATH)
	private static final String PROPERTY_PATH = "cache.config.path";

	private static final boolean DEFAULT_PERSISTENCE_OVERFLOW_ONLY = true;

	@Property(label = "Persistance overflow only", boolValue = DEFAULT_PERSISTENCE_OVERFLOW_ONLY)
	private static final String PROPERTY_PERSISTENCE_OVERFLOW_ONLY = "cache.config.persistenceoverflowonly";

	private static final String DEFAULT_EVENT_LISTENERS = "";

	@Property(label = "Event listeners", value = DEFAULT_EVENT_LISTENERS)
	private static final String PROPERTY_EVENT_LISTENERS = "cache.config.eventlisteners";

	private static final String DEFAULT_KEY = "__oscache_cache";

	@Property(label = "Key", value = DEFAULT_KEY)
	private static final String PROPERTY_KEY = "cache.config.key";

	private static final boolean DEFAULT_USE_HOST_DOMAIN_IN_KEY = false;

	@Property(label = "Use host domain in key", boolValue = DEFAULT_USE_HOST_DOMAIN_IN_KEY)
	private static final String PROPERTY_USE_HOST_DOMAIN_IN_KEY = "cache.config.usehostdomaininkey";

	private static final int DEFAULT_DURATION = 3600;

	@Property(label = "Duration", intValue = DEFAULT_DURATION)
	private static final String PROPERTY_DURATION = "cache.config.duration";

	@Reference
	private PathAliasStore pathAliasStore;

	private boolean enabled;

	private boolean memory;

	private int capacity;

	private String algorithm;

	private boolean blocking;

	private boolean unlimitedDisk;

	private String persistenceClass;

	private String path;

	private boolean persistenceOverflowOnly;

	private String eventListeners;

	private String key;

	private boolean useHostDomainInKey;

	private int duration;

	private PathAliasReader pathAliasReader;

	private Properties cacheProperties;

	@Activate
	public void activate(ComponentContext context) {
		String[] aliasesStrings = OsgiUtil.toStringArray(readProperty(context, PROPERTY_PATH_ALIASES));
		if (aliasesStrings == null) {
			aliasesStrings = DEFAULT_PATH_ALIASES;
		}

		enabled = OsgiUtil.toBoolean(readProperty(context, PROPERTY_FILTER_ENABLED), DEFAULT_FILTER_ENABLED);
		memory = OsgiUtil.toBoolean(readProperty(context, PROPERTY_MEMORY), DEFAULT_MEMORY);
		capacity = OsgiUtil.toInteger(readProperty(context, PROPERTY_CAPACITY), DEFAULT_CAPACITY);
		algorithm = OsgiUtil.toString(readProperty(context, PROPERTY_ALGORITHM), DEFAULT_ALGORITHM);
		blocking = OsgiUtil.toBoolean(readProperty(context, PROPERTY_BLOCKING), DEFAULT_BLOCKING);
		unlimitedDisk = OsgiUtil.toBoolean(readProperty(context, PROPERTY_UNLIMITED_DISK),
				DEFAULT_UNLIMITED_DISK);
		persistenceClass = OsgiUtil.toString(readProperty(context, PROPERTY_PERSISTENCE_CLASS),
				DEFAULT_PERSISTENCE_CLASS);
		path = OsgiUtil.toString(readProperty(context, PROPERTY_PATH), DEFAULT_PATH);
		persistenceOverflowOnly = OsgiUtil.toBoolean(
				readProperty(context, PROPERTY_PERSISTENCE_OVERFLOW_ONLY), DEFAULT_PERSISTENCE_OVERFLOW_ONLY);
		eventListeners = OsgiUtil.toString(readProperty(context, PROPERTY_EVENT_LISTENERS),
				DEFAULT_EVENT_LISTENERS);
		key = OsgiUtil.toString(readProperty(context, PROPERTY_KEY), DEFAULT_KEY);
		useHostDomainInKey = OsgiUtil.toBoolean(readProperty(context, PROPERTY_USE_HOST_DOMAIN_IN_KEY),
				DEFAULT_USE_HOST_DOMAIN_IN_KEY);
		duration = OsgiUtil.toInteger(readProperty(context, PROPERTY_DURATION), DEFAULT_DURATION);

		Set<PathAlias> aliases = new PathAliasReader().readAliases(aliasesStrings);
		pathAliasStore.addAliases(aliases);

		createConfigurationProperties();
	}

	private void createConfigurationProperties() {
		cacheProperties = new Properties();
//		cacheProperties.put(AbstractCacheAdministrator.CACHE_MEMORY_KEY, Boolean.toString(memory));
//		cacheProperties.put(AbstractCacheAdministrator.CACHE_CAPACITY_KEY, Integer.toString(capacity));
//		cacheProperties.put(AbstractCacheAdministrator.CACHE_ALGORITHM_KEY, algorithm);
//		cacheProperties.put(AbstractCacheAdministrator.CACHE_BLOCKING_KEY, Boolean.toString(blocking));
//		cacheProperties.put(AbstractCacheAdministrator.CACHE_DISK_UNLIMITED_KEY,
//				Boolean.toString(unlimitedDisk));
//		cacheProperties.put(AbstractCacheAdministrator.PERSISTENCE_CLASS_KEY, persistenceClass);
//		cacheProperties.put(CACHE_PATH_KEY, path);
//		cacheProperties.put(AbstractCacheAdministrator.CACHE_PERSISTENCE_OVERFLOW_KEY,
//				Boolean.toString(persistenceOverflowOnly));
//		cacheProperties.put(AbstractCacheAdministrator.CACHE_ENTRY_EVENT_LISTENERS_KEY, eventListeners);
		cacheProperties.put(CACHE_KEY_KEY, key);
		cacheProperties.put(CACHE_USE_HOST_DOMAIN_KEY, Boolean.toString(useHostDomainInKey));
	}

	private Object readProperty(ComponentContext context, String name) {
		return context.getProperties().get(name);
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public int getDuration() {
		return duration;
	}

	@Override
	public Properties getCacheProperties() {
		return cacheProperties;
	}

}
