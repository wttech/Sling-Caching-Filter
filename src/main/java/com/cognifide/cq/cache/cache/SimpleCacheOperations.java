package com.cognifide.cq.cache.cache;

import com.cognifide.cq.cache.cache.listener.CacheGuardListenerConfiguration;
import com.cognifide.cq.cache.definition.CacheConfigurationEntry;
import com.cognifide.cq.cache.expiry.collection.GuardCollectionWatcher;
import com.cognifide.cq.cache.filter.osgi.CacheManagerProvider;
import com.google.common.base.Optional;
import java.util.concurrent.TimeUnit;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.Factory;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.expiry.TouchedExpiryPolicy;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Component
public class SimpleCacheOperations implements CacheOperations {

	private static final Logger logger = LoggerFactory.getLogger(SimpleCacheOperations.class);

	@Reference
	private CacheManagerProvider cacheManagerProvider;

	@Reference
	private GuardCollectionWatcher guardCollectionWatcher;

	@Override
	public Optional<Cache<String, CacheEntity>> create(CacheConfigurationEntry cacheConfigurationEntry) {
		Optional<Cache<String, CacheEntity>> cache = Optional.<Cache<String, CacheEntity>>absent();

		final String cacheName = cacheConfigurationEntry.getResourceType();
		if (isValid(cacheName)) {
			CacheManager cacheManager = cacheManagerProvider.getCacheManager();
			if (isLive(cacheManager) && !findFor(cacheName, cacheManager).isPresent()) {
				if (logger.isInfoEnabled()) {
					logger.info("Creating {} cache", cacheName);
				}

				cache = Optional.of(cacheManager.createCache(cacheName, buildBasicCacheConfiguration(cacheConfigurationEntry)));
				cacheManagerProvider.updateCacheConfiguration(cache.get());

				if (logger.isDebugEnabled()) {
					logger.debug("Cache {} was created", cacheName);
				}
			}
		}

		return cache;
	}

	private MutableConfiguration<String, CacheEntity> buildBasicCacheConfiguration(CacheConfigurationEntry cacheConfigurationEntry) {
		return new MutableConfiguration<String, CacheEntity>()
				.setTypes(String.class, CacheEntity.class)
				.setStoreByValue(false)
				.setStatisticsEnabled(true)
				.setExpiryPolicyFactory(createExpiryPolicyFactory(cacheConfigurationEntry))
				.addCacheEntryListenerConfiguration(new CacheGuardListenerConfiguration(guardCollectionWatcher));
	}

	private Factory<ExpiryPolicy> createExpiryPolicyFactory(CacheConfigurationEntry cacheConfigurationEntry) {
		Duration duration = new Duration(TimeUnit.SECONDS, cacheConfigurationEntry.getValidityTimeInSeconds());
		return new FactoryBuilder.SingletonFactory<ExpiryPolicy>(new TouchedExpiryPolicy(duration));
	}

	private boolean isValid(String cacheName) {
		return StringUtils.isNotEmpty(cacheName);
	}

	private boolean isLive(CacheManager cacheManager) {
		return !cacheManager.isClosed();
	}

	@Override
	public Optional<Cache<String, CacheEntity>> findFor(String cacheName) {
		CacheManager cacheManager = cacheManagerProvider.getCacheManager();
		return findFor(cacheName, cacheManager);
	}

	private Optional<Cache<String, CacheEntity>> findFor(String cacheName, CacheManager cacheManager) {
		return isValid(cacheName) && isLive(cacheManager)
				? Optional.fromNullable(cacheManager.getCache(cacheName, String.class, CacheEntity.class))
				: Optional.<Cache<String, CacheEntity>>absent();
	}

	@Override
	public void delete(String cacheName) {
		CacheManager cacheManager = cacheManagerProvider.getCacheManager();
		if (findFor(cacheName, cacheManager).isPresent()) {
			if (logger.isInfoEnabled()) {
				logger.info("Destroying {} cache.", cacheName);
			}
			cacheManager.destroyCache(cacheName);
			if (logger.isDebugEnabled()) {
				logger.debug("Cache {} was destroyed", cacheName);
			}
		}
	}
}
