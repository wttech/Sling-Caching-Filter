package com.cognifide.cq.cache.cache;

import com.cognifide.cq.cache.cache.callback.MissingCacheEntryCallback;
import com.cognifide.cq.cache.definition.CacheConfigurationEntry;
import com.cognifide.cq.cache.definition.ResourceTypeCacheDefinition;
import com.cognifide.cq.cache.expiry.collection.GuardCollectionWatcher;
import com.cognifide.cq.cache.expiry.guard.ExpiryGuard;
import com.cognifide.cq.cache.filter.osgi.CacheManagerProvider;
import com.cognifide.cq.cache.model.ResourceTypeCacheConfiguration;
import com.cognifide.cq.cache.model.key.CacheKeyGenerator;
import com.cognifide.cq.cache.model.key.CacheKeyGeneratorImpl;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.servlet.ServletException;
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
	private CacheManagerProvider cacheManagerProvider;

	@Reference
	private CacheOperations cacheOperations;

	@Reference
	private GuardCollectionWatcher guardCollectionWatcher;

	@Reference(
			referenceInterface = ResourceTypeCacheDefinition.class,
			policy = ReferencePolicy.DYNAMIC,
			cardinality = ReferenceCardinality.MANDATORY_MULTIPLE,
			updated = "updateResourceTypeCacheDefinition",
			strategy = ReferenceStrategy.EVENT)
	private final ConcurrentMap<String, ResourceTypeCacheDefinition> resourceTypeCacheDefinitions
			= new ConcurrentHashMap<String, ResourceTypeCacheDefinition>(8);

	private CacheKeyGenerator cacheKeyGenerator;

	@Activate
	protected void activate() {
		cacheKeyGenerator = new CacheKeyGeneratorImpl();
	}

	protected synchronized void bindResourceTypeCacheDefinition(ResourceTypeCacheDefinition resourceTypeCacheDefinition) {
		String resourceType = resourceTypeCacheDefinition.getResourceType();
		if (!resourceTypeCacheDefinitions.containsKey(resourceType)) {
			resourceTypeCacheDefinitions.put(resourceType, resourceTypeCacheDefinition);
		} else {
			logger.warn("Resource type cache definition was already defined for {}", resourceType);
		}
	}

	private Optional<Cache<String, CacheEntity>> createOrRecreateCache(CacheConfigurationEntry cacheConfigurationEntry)
			throws IllegalArgumentException {
		final String cacheName = cacheConfigurationEntry.getResourceType();
		cacheOperations.delete(cacheName);
		return cacheOperations.create(cacheConfigurationEntry);
	}

	protected synchronized void updateResourceTypeCacheDefinition(ResourceTypeCacheDefinition resourceTypeCacheDefinition) {
		resourceTypeCacheDefinitions.put(resourceTypeCacheDefinition.getResourceType(), resourceTypeCacheDefinition);
	}

	protected synchronized void unbindResourceTypeCacheDefinition(ResourceTypeCacheDefinition resourceTypeCacheDefinition) {
		resourceTypeCacheDefinitions.remove(resourceTypeCacheDefinition.getResourceType());
	}

	@Override
	public Optional<URI> getCacheManagerURI() {
		CacheManager cacheManager = cacheManagerProvider.getCacheManager();
		return cacheManager.isClosed() ? Optional.<URI>absent() : Optional.of(cacheManager.getURI());

	}

	@Override
	public Iterable<String> getCacheNames() {
		CacheManager cacheManager = cacheManagerProvider.getCacheManager();
		return cacheManager.isClosed() ? Collections.<String>emptySet() : cacheManager.getCacheNames();
	}

	@Override
	public Set<String> getKeysFor(String cacheName) {
		Optional<Cache<String, CacheEntity>> cache = cacheOperations.findFor(cacheName);
		return isValid(cache)
				? ImmutableSet.copyOf(Iterators.transform(cache.get().iterator(), new EntryToKeyTransform()))
				: Collections.<String>emptySet();
	}

	private boolean isValid(Optional<Cache<String, CacheEntity>> cache) {
		return cache.isPresent() && !cache.get().isClosed();
	}

	@Override
	public Map<String, CacheEntity> getValuesFor(String cacheName) {
		Optional<Cache<String, CacheEntity>> cache = cacheOperations.findFor(cacheName);
		return isValid(cache) ? cache.get().getAll(getKeysFor(cacheName))
				: Collections.<String, CacheEntity>emptyMap();
	}

	@Override
	public CacheEntity putOrGet(SlingHttpServletRequest request,
			ResourceTypeCacheConfiguration resourceTypeCacheConfiguration, MissingCacheEntryCallback callback)
			throws IOException, ServletException {
		CacheEntity result = null;

		Optional<Cache<String, CacheEntity>> cache = findOrCreateCacheFrom(resourceTypeCacheConfiguration);
		if (cache.isPresent()) {
			final String key = cacheKeyGenerator.generateKey(request, resourceTypeCacheConfiguration);
			result = cache.get().get(key);
			if (null == result) {
				if (logger.isInfoEnabled()) {
					logger.info("Key {} not in cache, generating content...", key);
				}
				result = callback.generateCacheEntity();
				cache.get().put(key, result);
				if (logger.isDebugEnabled()) {
					logger.debug("Key {} added to {} cache.", key, cache.get().getName());
				}
				guardCollectionWatcher.addGuard(
						ExpiryGuard.createDeletingExpiryGuard(request, this, resourceTypeCacheConfiguration, key));
			}
		} else {
			logger.error("Resource with resource type {} does not have any valid configuration anymore. Generating content.",
					request.getResource().getResourceType());
			result = callback.generateCacheEntity();
		}

		return result;
	}

	private Optional<Cache<String, CacheEntity>> findOrCreateCacheFrom(
			CacheConfigurationEntry cacheConfigurationEntry) {
		final String cacheName = cacheConfigurationEntry.getResourceType();

		Optional<Cache<String, CacheEntity>> result = cacheOperations.findFor(cacheName);
		if (!isValid(result)) {
			if (canCreateCache(cacheName)) {
				logger.warn("{} cache was missing. Creating cache...", cacheName);
				result = createOrRecreateCache(cacheConfigurationEntry);
			} else {
				logger.error("Resource type with {} was not defined. Cache does not exist/could not be created.", cacheName);
			}
		}

		return result;
	}

	private boolean canCreateCache(String cacheName) {
		return resourceTypeCacheDefinitions.containsKey(cacheName)
				&& resourceTypeCacheDefinitions.get(cacheName).isValid()
				&& resourceTypeCacheDefinitions.get(cacheName).isEnabled();
	}

	@Override
	public void remove(String cacheName, String key) {
		Optional<Cache<String, CacheEntity>> cache = cacheOperations.findFor(cacheName);
		if (isValid(cache)) {
			guardCollectionWatcher.removeGuard(cacheName, key);
			cache.get().remove(key);
			if (logger.isInfoEnabled()) {
				logger.info("Element {} was removed from {} cache.", key, cacheName);
			}
		} else {
			logger.warn("Could not remove element {}. Cache {} does not exist or was closed.", key, cacheName);
		}
	}

	@Override
	public void clear(String cacheName) {
		Optional<Cache<String, CacheEntity>> cache = cacheOperations.findFor(cacheName);
		if (isValid(cache)) {
			guardCollectionWatcher.removeGuards(cacheName);
			cache.get().clear();
			if (logger.isInfoEnabled()) {
				logger.info("Cache {} was cleared.", cacheName);
			}
		} else {
			logger.warn("Could not clear cache. Cache {} does not exist or was closed.", cacheName);
		}
	}
}
