package com.cognifide.cq.cache.filter.osgi;

import com.cognifide.cq.cache.cache.CacheEntity;
import com.cognifide.cq.cache.definition.osgi.OsgiConfigurationHelper;
import com.cognifide.cq.cache.model.alias.PathAliasStore;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.DiskStoreConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.PropertyOption;
import org.apache.felix.scr.annotations.PropertyUnbounded;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(label = "Sling Caching Filter", description = "Sling Caching Filter", metatype = true, immediate = true)
@Service(value = {CacheConfiguration.class, CacheManagerProvider.class})
public class EhCacheConfiguration implements CacheConfiguration, CacheManagerProvider {

	private static final Logger logger = LoggerFactory.getLogger(EhCacheConfiguration.class);

	private static final String CACHING_PROVIDER = "org.ehcache.jcache.JCacheCachingProvider";

	private static final boolean FILTER_ENABLED_DEFAULT_VALUE = false;

	private static final int VALIDITY_TIME_DEFAULT_VALUE = 3600;

	private static final int MAX_ENTRIES_IN_CACHE_DEFAULT_VALUE = 1000;

	private static final boolean USE_DISK_PERSISTANCE_DEFAULT_VALUE = false;

	private static final int MAX_ENTRIES_ON_DISK_DEFAULT_VALUE = 0;

	private static final String EVICTION_POLICY_DEFAULT_VALUE = "LRU";

	@Property(
			label = "Enabled",
			description = "Turns on caching filter",
			boolValue = FILTER_ENABLED_DEFAULT_VALUE)
	private static final String FILTER_ENABLED_PROPERTY = "cache.config.enabled";

	@Property(
			label = "Validity time",
			description = "Specifies cache entry validity time (in seconds). Used when Resource Type Definition has validity time not set.",
			intValue = VALIDITY_TIME_DEFAULT_VALUE)
	private static final String VALIDITY_TIME_PROPERTY = "cache.config.validity.time";

	@Property(
			label = "Max entries in cache",
			description = "Max entries in cache. 0 means unlimited.",
			intValue = MAX_ENTRIES_IN_CACHE_DEFAULT_VALUE)
	private static final String MAX_ENTRIES_IN_CACHE_PROPERTY = "cache.config.max.entries.in.cache";

	@Property(
			label = "Use disk persistance",
			description = "",
			boolValue = USE_DISK_PERSISTANCE_DEFAULT_VALUE)
	private static final String USE_DISK_PERSISTANCE_PROPERTY = "cache.config.use.disk.persistance";

	@Property(
			label = "Max entries on disk",
			description = "Max entries on disk. 0 means unlimited.",
			intValue = MAX_ENTRIES_ON_DISK_DEFAULT_VALUE)
	private static final String MAX_ENTRIES_ON_DISK_PROPERTY = "cache.config.max.entries.on.disk";

	@Property(
			label = "Disk store path",
			description = "The path to the directory where any required disk files will be created.")
	private static final String DISK_STORE_PATH_PROPERTY = "cache.config.disk.store.path";

	@Property(
			label = "Eviction policy",
			description = "Sets the eviction policy",
			options = {
				@PropertyOption(name = "LRU", value = "LRU"),
				@PropertyOption(name = "LFU", value = "LFU"),
				@PropertyOption(name = "FIFO", value = "FIFO")
			},
			value = EVICTION_POLICY_DEFAULT_VALUE)
	private static final String EVICTION_POLICY_PROPERTY = "cache.config.eviction.policy";

	@Property(label = "Path aliases",
			description = "Path aliases format: $<alias name>|<path 1>|<path 2>|...",
			unbounded = PropertyUnbounded.ARRAY)
	private static final String PROPERTY_PATH_ALIASES = "cache.config.pathaliases";

	@Reference
	private PathAliasStore pathAliasStore;

	private CacheManager cacheManager;

	private boolean enabled;

	private int validityTime;

	private int maxEntriesInCache;

	private boolean useDiskPersistance;

	private int maxEntriesOnDisk;

	private String diskStorePath;

	private String evictionPolicy;

	@Activate
	protected void activate(ComponentContext context) {
		pathAliasStore.populate(OsgiConfigurationHelper.getStringArrayValuesFrom(PROPERTY_PATH_ALIASES, context));

		enabled = OsgiConfigurationHelper.getBooleanValueFrom(FILTER_ENABLED_PROPERTY, context);
		validityTime = OsgiConfigurationHelper.getIntegerValueFrom(VALIDITY_TIME_PROPERTY, context);
		maxEntriesInCache = OsgiConfigurationHelper.getIntegerValueFrom(MAX_ENTRIES_IN_CACHE_PROPERTY, context);
		useDiskPersistance = OsgiConfigurationHelper.getBooleanValueFrom(USE_DISK_PERSISTANCE_PROPERTY, context);
		maxEntriesOnDisk = OsgiConfigurationHelper.getIntegerValueFrom(MAX_ENTRIES_ON_DISK_PROPERTY, context);
		diskStorePath = OsgiConfigurationHelper.getStringValueFrom(DISK_STORE_PATH_PROPERTY, context);
		evictionPolicy = OsgiConfigurationHelper.getStringValueFrom(EVICTION_POLICY_PROPERTY, context);

		createCacheManager();
	}

	private void createCacheManager() {
		if (null == cacheManager || cacheManager.isClosed()) {
			Caching.setDefaultClassLoader(getClass().getClassLoader());
			CachingProvider cachingProvider = Caching.getCachingProvider(CACHING_PROVIDER);
			cacheManager = cachingProvider.getCacheManager();
			configureCacheManagerVendorSpecific();
			if (logger.isInfoEnabled()) {
				logger.info("Cache manager {} was created.", cacheManager.getURI());
			}
		}
	}

	private void configureCacheManagerVendorSpecific() {
		net.sf.ehcache.config.CacheConfiguration cacheConfiguration = new net.sf.ehcache.config.CacheConfiguration();
		cacheConfiguration.setName("defaultCache");
		setCacheConfiguration(cacheConfiguration);

		Configuration cacheManagerConfiguration
				= cacheManager.unwrap(net.sf.ehcache.CacheManager.class).getConfiguration();
		if (useDiskPersistance) {
			DiskStoreConfiguration diskStoreConfiguration = cacheManagerConfiguration.getDiskStoreConfiguration();
			if (null == diskStoreConfiguration) {
				cacheManagerConfiguration.diskStore(new DiskStoreConfiguration().path(diskStorePath));
			} else {
				diskStoreConfiguration.path(diskStorePath);
			}
		}

		cacheManagerConfiguration.setDefaultCacheConfiguration(cacheConfiguration);
	}

	private void setCacheConfiguration(net.sf.ehcache.config.CacheConfiguration cacheConfiguration) {
		cacheConfiguration.setMaxEntriesInCache(maxEntriesInCache);
		cacheConfiguration.setMaxEntriesLocalDisk(maxEntriesOnDisk);
		cacheConfiguration.setMaxEntriesLocalHeap(maxEntriesInCache);
		cacheConfiguration.setMemoryStoreEvictionPolicy(evictionPolicy);

		if (useDiskPersistance) {
			// This is the only available persistance configuration strategy
			cacheConfiguration.persistence(new PersistenceConfiguration().strategy(
					PersistenceConfiguration.Strategy.LOCALTEMPSWAP));
		}
	}

	@Modified
	protected void modified(ComponentContext context) {
		deactivate();
		activate(context);
	}

	@Override
	public void updateCacheConfiguration(Cache<String, CacheEntity> cache) {
		net.sf.ehcache.Cache ehCache = cache.unwrap(net.sf.ehcache.Cache.class);
		net.sf.ehcache.config.CacheConfiguration cacheConfiguration = ehCache.getCacheConfiguration();
		setCacheConfiguration(cacheConfiguration);
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public int getValidityTimeInSeconds() {
		return validityTime;
	}

	@Override
	public CacheManager getCacheManager() {
		return cacheManager;
	}

	@Deactivate
	protected void deactivate() {
		if (null != cacheManager && !cacheManager.isClosed()) {
			cacheManager.close();
		}
	}

}
