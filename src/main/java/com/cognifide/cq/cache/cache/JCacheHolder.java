package com.cognifide.cq.cache.cache;

import com.cognifide.cq.cache.cache.callback.MissingCacheEntryCallback;
import com.cognifide.cq.cache.cache.listener.CacheGuardListenerConfiguration;
import com.cognifide.cq.cache.definition.CacheConfigurationEntry;
import com.cognifide.cq.cache.definition.ResourceTypeCacheDefinition;
import com.cognifide.cq.cache.expiry.collection.GuardCollectionWatcher;
import com.cognifide.cq.cache.expiry.guard.ExpiryGuard;
import com.cognifide.cq.cache.filter.osgi.CacheConfiguration;
import com.cognifide.cq.cache.filter.osgi.CacheManagerProvider;
import com.cognifide.cq.cache.model.ResourceTypeCacheConfiguration;
import com.cognifide.cq.cache.model.key.CacheKeyGenerator;
import com.cognifide.cq.cache.model.key.CacheKeyGeneratorImpl;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.Factory;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.expiry.TouchedExpiryPolicy;
import javax.servlet.ServletException;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.ReferenceStrategy;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Component
public class JCacheHolder implements CacheHolder {

	private static final Logger logger = LoggerFactory.getLogger(JCacheHolder.class);

	@Reference
	private CacheConfiguration cacheConfiguration;

	@Reference
	private CacheManagerProvider cacheManagerProvider;

	@Reference(
			referenceInterface = ResourceTypeCacheDefinition.class,
			policy = ReferencePolicy.DYNAMIC,
			cardinality = ReferenceCardinality.MANDATORY_MULTIPLE,
			strategy = ReferenceStrategy.EVENT)
	private final ConcurrentMap<String, ResourceTypeCacheDefinition> resourceTypeCacheDefinitions
			= new ConcurrentHashMap<String, ResourceTypeCacheDefinition>(8);

	@Reference
	private GuardCollectionWatcher guardCollectionWatcher;

	private CacheKeyGenerator cacheKeyGenerator;

	@Activate
	public void activate() {
		cacheKeyGenerator = new CacheKeyGeneratorImpl();
	}

	public synchronized void bindResourceTypeCacheDefinition(ResourceTypeCacheDefinition resourceTypeCacheDefinition) {
		if (resourceTypeCacheDefinition.isValid() && resourceTypeCacheDefinition.isEnabled()) {
			resourceTypeCacheDefinitions.put(resourceTypeCacheDefinition.getResourceType(), resourceTypeCacheDefinition);
			createOrRecreateCache(resourceTypeCacheDefinition);
		}
	}

	private Cache<String, ByteArrayOutputStream> createOrRecreateCache(CacheConfigurationEntry cacheConfigurationEntry)
			throws IllegalArgumentException {
		final String cacheName = cacheConfigurationEntry.getResourceType();

		if (null != findCacheFor(cacheName)) {
			deleteCache(cacheName);
		}

		return createCache(cacheName, cacheConfigurationEntry);
	}

	private void deleteCache(String cacheName) {
		CacheManager cacheManager = cacheManagerProvider.getCacheManger();
		if (canWorkWithCacheManager(cacheName, cacheManager)) {
			logger.info("Destroying {} cache.", cacheName);
			cacheManager.destroyCache(cacheName);
			logger.debug("Cache {} was destroyed", cacheName);
		}
	}

	private boolean canWorkWithCacheManager(String cacheName, CacheManager cacheManager) {
		return StringUtils.isNotEmpty(cacheName) && !cacheManager.isClosed();
	}

	private Cache<String, ByteArrayOutputStream> createCache(
			String cacheName, CacheConfigurationEntry cacheConfigurationEntry) {
		Cache<String, ByteArrayOutputStream> cache = null;

		CacheManager cacheManager = cacheManagerProvider.getCacheManger();
		if (canWorkWithCacheManager(cacheName, cacheManager)) {
			logger.info("Creating {} cache", cacheName);
			cache = cacheManager.createCache(cacheName, buildBasicCacheConfiguration(cacheConfigurationEntry));
			cacheManagerProvider.updateCacheConfiguration(cache);
			logger.debug("Cache {} was created", cacheName);
		}

		return cache;
	}

	private Cache<String, ByteArrayOutputStream> findCacheFor(String cacheName) {
		CacheManager cacheManager = cacheManagerProvider.getCacheManger();
		return canWorkWithCacheManager(cacheName, cacheManager)
				? cacheManager.getCache(cacheName, String.class, ByteArrayOutputStream.class) : null;
	}

	private MutableConfiguration<String, ByteArrayOutputStream>
			buildBasicCacheConfiguration(CacheConfigurationEntry cacheConfigurationEntry) {
		return new MutableConfiguration<String, ByteArrayOutputStream>()
				.setTypes(String.class, ByteArrayOutputStream.class)
				.setStoreByValue(false)
				.setStatisticsEnabled(true)
				.setExpiryPolicyFactory(createExpiryPolicyFactory(cacheConfigurationEntry))
				.addCacheEntryListenerConfiguration(new CacheGuardListenerConfiguration(guardCollectionWatcher));
	}

	private Factory<? extends ExpiryPolicy> createExpiryPolicyFactory(
			CacheConfigurationEntry cacheConfigurationEntry) {
		int validityTimeInSeconds = null == cacheConfigurationEntry.getValidityTimeInSeconds()
				? cacheConfiguration.getValidityTimeInSeconds() : cacheConfigurationEntry.getValidityTimeInSeconds();
		Duration duration = new Duration(TimeUnit.SECONDS, validityTimeInSeconds);
		return new FactoryBuilder.SingletonFactory<ExpiryPolicy>(new TouchedExpiryPolicy(duration));
	}

	public synchronized void unbindResourceTypeCacheDefinition(ResourceTypeCacheDefinition resourceTypeCacheDefinition) {
		if (resourceTypeCacheDefinition.isValid()) {
			String cacheName = resourceTypeCacheDefinition.getResourceType();
			resourceTypeCacheDefinitions.remove(cacheName);
			deleteCache(cacheName);
		}
	}

	@Override
	public URI getCacheManagerURI() {
		return cacheManagerProvider.getCacheManger().getURI();
	}

	@Override
	public Iterable<String> getCacheNames() {
		CacheManager cacheManager = cacheManagerProvider.getCacheManger();
		return cacheManager.isClosed() ? Collections.<String>emptySet() : cacheManager.getCacheNames();
	}

	@Override
	public Collection<String> getKeysFor(String cacheName) {
		Set<String> keys = Collections.emptySet();

		Cache<String, ByteArrayOutputStream> cache = findCacheFor(cacheName);
		if (cacheIsValid(cache)) {
			keys = new HashSet<String>();
			Iterator<Cache.Entry<String, ByteArrayOutputStream>> iterator = cache.iterator();
			while (iterator.hasNext()) {
				keys.add(iterator.next().getKey());
			}
		}

		return Collections.unmodifiableSet(keys);
	}

	private boolean cacheIsValid(Cache<String, ByteArrayOutputStream> cache) {
		return null != cache && !cache.isClosed();
	}

	@Override
	public ByteArrayOutputStream putOrGet(SlingHttpServletRequest request,
			ResourceTypeCacheConfiguration resourceTypeCacheConfiguration, MissingCacheEntryCallback callback)
			throws IOException, ServletException {
		ByteArrayOutputStream result = null;

		Cache<String, ByteArrayOutputStream> cache = findOrCreateCacheFrom(resourceTypeCacheConfiguration);

		final String key = cacheKeyGenerator.generateKey(request, resourceTypeCacheConfiguration);
		if (null != cache) {
			result = cache.get(key);
		}

		if (null == result) {
			logger.info("Key {} not in cache, generating content...", key);
			result = callback.doCallback();
			if (null != cache) {
				cache.put(key, result);
				logger.debug("Key {} added to {} cache.", key, cache.getName());
				guardCollectionWatcher.addGuard(
						ExpiryGuard.createDeletingExpiryGuard(request, this, resourceTypeCacheConfiguration, key));
			}
		}

		return result;
	}

	private Cache<String, ByteArrayOutputStream> findOrCreateCacheFrom(
			CacheConfigurationEntry cacheConfigurationEntry) {
		final String cacheName = cacheConfigurationEntry.getResourceType();

		Cache<String, ByteArrayOutputStream> cache = findCacheFor(cacheName);
		if (null == cache) {
			if (resourceTypeCacheDefinitions.containsKey(cacheName)) {
				logger.warn("{} cache was missing. Creating cache...", cacheName);
				cache = createOrRecreateCache(cacheConfigurationEntry);
			} else {
				logger.error("Resource type with {} was not defined. Cache could not be created.", cacheName);
			}
		}

		return cache;
	}

	@Override
	public void remove(String cacheName, String key) {
		Cache<String, ByteArrayOutputStream> cache = findCacheFor(cacheName);
		if (cacheIsValid(cache)) {
			cache.remove(key);
		} else {
			logger.warn("Could not remove element {}. Cache {} does not exist or was closed.", key, cacheName);
		}
	}

	@Override
	public void clear(String cacheName) {
		Cache<String, ByteArrayOutputStream> cache = findCacheFor(cacheName);
		if (cacheIsValid(cache)) {
			cache.clear();
		} else {
			logger.warn("Could not clear cache. Cache {} does not exist or was closed.", cacheName);
		}
	}
}
