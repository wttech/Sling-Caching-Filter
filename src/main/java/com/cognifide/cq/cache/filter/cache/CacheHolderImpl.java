package com.cognifide.cq.cache.filter.cache;

import com.cognifide.cq.cache.algorithm.SilentRemovalNotificator;
import com.cognifide.cq.cache.filter.cache.action.DeleteAction;
import com.cognifide.cq.cache.plugins.statistics.Statistics;
import com.cognifide.cq.cache.refresh.jcr.JcrRefreshPolicy;
import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.web.ServletCacheAdministrator;
import java.io.ByteArrayOutputStream;
import java.util.Properties;
import javax.servlet.ServletContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

@Component(immediate = true)
@Service
public class CacheHolderImpl implements CacheHolder {

	private static final Log log = LogFactory.getLog(CacheHolderImpl.class);

	@Reference
	private Statistics statistics;

	private ServletContext servletContext;

	private ServletCacheAdministrator cacheAdministrator;

	@Override
	public void create(ServletContext servletContext, Properties properties, boolean overwrite) {
		if (null == this.cacheAdministrator || overwrite) {
			this.servletContext = servletContext;
			this.cacheAdministrator = ServletCacheAdministrator.getInstance(servletContext, properties);
			if (log.isInfoEnabled()) {
				log.info("Instance of servlet cache administrator was retrived");
			}
		}
	}

	@Override
	public void put(String key, ByteArrayOutputStream data, JcrRefreshPolicy refreshPolicy) {
		Cache cache = findCache();
		try {
			cache.putInCache(key, data, refreshPolicy);
		} finally {
			// finally block used to make sure that all data binded to the current thread is cleared
			SilentRemovalNotificator.notifyListeners(cache);
		}
		cache.addCacheEventListener(refreshPolicy);
	}

	@Override
	public ByteArrayOutputStream get(String resourceType, String key) throws NeedsRefreshException {
		ByteArrayOutputStream result = null;
		try {
			result = (ByteArrayOutputStream) findCache().getFromCache(key);

			if (log.isInfoEnabled()) {
				log.info("Cache hit. Key " + key + " found");
			}

			statistics.cacheHit(resourceType);
		} catch (NeedsRefreshException x) {

			if (log.isInfoEnabled()) {
				log.info("Cache miss. New cahe entry, cache stale or cache scope flused for key " + key);
			}

			statistics.cacheMiss(resourceType, key, new DeleteAction(this, key));
			throw x;
		}
		return result;
	}

	private Cache findCache() {
		return cacheAdministrator.getAppScopeCache(servletContext);
	}

	@Override
	public void remove(String key) {
		findCache().removeEntry(key);
	}

	@Override
	public void destroy() {
		statistics.clearStatistics();
		ServletCacheAdministrator.destroyInstance(servletContext);
		cacheAdministrator = null;
		if (log.isInfoEnabled()) {
			log.info("Instance of servlet cache administrator was destroyed");
		}
	}

	@Deactivate
	protected void deactivate() {
		destroy();
	}
}
