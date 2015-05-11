package com.cognifide.cq.cache.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.sling.SlingFilter;
import org.apache.felix.scr.annotations.sling.SlingFilterScope;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.osgi.service.component.ComponentContext;

import com.cognifide.cq.cache.algorithm.SilentRemovalNotificator;
import com.cognifide.cq.cache.model.CacheKeyGenerator;
import com.cognifide.cq.cache.model.CacheKeyGeneratorImpl;
import com.cognifide.cq.cache.model.alias.PathAlias;
import com.cognifide.cq.cache.model.alias.PathAliasReader;
import com.cognifide.cq.cache.model.alias.PathAliasStore;
import com.cognifide.cq.cache.model.ResourceTypeCacheConfiguration;
import com.cognifide.cq.cache.model.reader.ResourceTypeCacheConfigurationReader;
import com.cognifide.cq.cache.refresh.jcr.FilterJcrRefreshPolicy;
import com.cognifide.cq.cache.refresh.jcr.JcrEventsService;
import com.opensymphony.oscache.base.AbstractCacheAdministrator;
import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.web.ServletCacheAdministrator;
import com.opensymphony.oscache.web.filter.ICacheGroupsProvider;
import com.opensymphony.oscache.web.filter.ICacheKeyProvider;
import org.apache.felix.scr.annotations.Reference;

/**
 * Sling Caching Filter
 *
 * @author Przemyslaw Pakulski
 * @author Jakub Malecki
 * @author Maciej Majchrzak
 */
@SlingFilter(label = "Sling Caching Filter", description = "Sling Caching Filter", scope = SlingFilterScope.COMPONENT, order = 100, metatype = true)
public class ComponentCacheFilter implements Filter, ICacheKeyProvider, ICacheGroupsProvider {

	private static final Log log = LogFactory.getLog(ComponentCacheFilter.class);

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

	// Cache config keys
	private static final String CACHE_PATH_KEY = "cache.path";

	private static final String CACHE_USE_HOST_DOMAIN_KEY = "cache.use.host.domain.in.key";

	private static final String CACHE_KEY_KEY = "cache.key";

	public static final String SERVLET_CONTEXT_CACHE_ENABLED = ComponentCacheFilter.class.getName()
			+ ".cache.enabled";

	public static final String SERVLET_CONTEXT_CACHE_DURATION = ComponentCacheFilter.class.getName()
			+ ".cache.duration";

	@Reference
	private JcrEventsService jcrEventsService;

	@Reference
	private PathAliasStore pathAliasStore;

	@Reference
	private ResourceTypeCacheConfigurationReader configurationReader;

	// Properties read from configuration
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

	private ServletContext servletContext;

	private ServletCacheAdministrator admin;

	private PathAliasReader pathAliasReader;

	private CacheKeyGenerator generator;

	private Properties configProperties;

	@Override
	public void init(FilterConfig filterConfig) {
		log.info("init " + getClass());
		servletContext = filterConfig.getServletContext();
		if (admin == null) { // first time, see activate method
			admin = ServletCacheAdministrator.getInstance(servletContext, configProperties);
		}
		if (enabled) {
			servletContext.setAttribute(SERVLET_CONTEXT_CACHE_ENABLED, Boolean.TRUE);
			servletContext.setAttribute(SERVLET_CONTEXT_CACHE_DURATION, duration);
		} else {
			servletContext.setAttribute(SERVLET_CONTEXT_CACHE_ENABLED, Boolean.FALSE);
		}
	}

	@Override
	public void destroy() {
		log.info("destroy " + getClass());
		ServletCacheAdministrator.destroyInstance(servletContext);
		admin = null;
	}

	/**
	 * Handle OSGi activation
	 *
	 * @param context osgi component context
	 */
	protected void activate(ComponentContext context) {
		log.info("activate " + getClass());

		pathAliasReader = new PathAliasReader();
		generator = new CacheKeyGeneratorImpl();

		enabled = OsgiUtil.toBoolean(readProperty(context, PROPERTY_FILTER_ENABLED), DEFAULT_FILTER_ENABLED);
		if (enabled) {
			readConfiguration(context);
		}

		if (servletContext != null) { // first time activate is called before init, so servletContext is null
			admin = ServletCacheAdministrator.getInstance(servletContext, configProperties);
			if (enabled) {
				servletContext.setAttribute(SERVLET_CONTEXT_CACHE_ENABLED, Boolean.TRUE);
				servletContext.setAttribute(SERVLET_CONTEXT_CACHE_DURATION, duration);
			} else {
				servletContext.setAttribute(SERVLET_CONTEXT_CACHE_ENABLED, Boolean.FALSE);
			}
		}
	}

	/**
	 * Handle OSGi deactivation
	 *
	 * @param context osgi component context
	 */
	protected void deactivate(ComponentContext context) {
		log.info("deactivate " + getClass());
		ServletCacheAdministrator.destroyInstance(servletContext);
		jcrEventsService.clearEventListeners();
	}

	/**
	 * Read OSGi component configuration
	 *
	 * @param context osgi component context
	 */
	protected void readConfiguration(ComponentContext context) {
		String[] aliasesStrings = OsgiUtil.toStringArray(readProperty(context, PROPERTY_PATH_ALIASES));
		if (aliasesStrings == null) {
			aliasesStrings = DEFAULT_PATH_ALIASES;
		}

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

		Set<PathAlias> aliases = pathAliasReader.readAliases(aliasesStrings);
		pathAliasStore.addAliases(aliases);

		configProperties = new Properties();
		configProperties.put(AbstractCacheAdministrator.CACHE_MEMORY_KEY, Boolean.toString(memory));
		configProperties.put(AbstractCacheAdministrator.CACHE_CAPACITY_KEY, Integer.toString(capacity));
		configProperties.put(AbstractCacheAdministrator.CACHE_ALGORITHM_KEY, algorithm);
		configProperties.put(AbstractCacheAdministrator.CACHE_BLOCKING_KEY, Boolean.toString(blocking));
		configProperties.put(AbstractCacheAdministrator.CACHE_DISK_UNLIMITED_KEY,
				Boolean.toString(unlimitedDisk));
		configProperties.put(AbstractCacheAdministrator.PERSISTENCE_CLASS_KEY, persistenceClass);
		configProperties.put(CACHE_PATH_KEY, path);
		configProperties.put(AbstractCacheAdministrator.CACHE_PERSISTENCE_OVERFLOW_KEY,
				Boolean.toString(persistenceOverflowOnly));
		configProperties.put(AbstractCacheAdministrator.CACHE_ENTRY_EVENT_LISTENERS_KEY, eventListeners);
		configProperties.put(CACHE_KEY_KEY, key);
		configProperties.put(CACHE_USE_HOST_DOMAIN_KEY, Boolean.toString(useHostDomainInKey));
	}

	private Object readProperty(ComponentContext context, String name) {
		return context.getProperties().get(name);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		if (enabled) {
			if (request instanceof SlingHttpServletRequest) {
				SlingHttpServletRequest slingHttpServletRequest = (SlingHttpServletRequest) request;

				if (configurationReader.hasConfigurationFor(slingHttpServletRequest)) {
					ResourceTypeCacheConfiguration cacheConfiguration
							= configurationReader.readComponentConfiguration(slingHttpServletRequest, duration);

					if (null != cacheConfiguration && cacheConfiguration.isEnabled()) {
						cacheRequestedResource(slingHttpServletRequest, response, chain, cacheConfiguration);
					} else {
						if (log.isInfoEnabled()) {
							log.info("Caching is disabled for "
									+ slingHttpServletRequest.getResource().getResourceType());
						}
						chain.doFilter(request, response);
					}
				} else {
					if (log.isInfoEnabled()) {
						log.info("There is no configuration for "
								+ slingHttpServletRequest.getResource().getResourceType());
					}
					chain.doFilter(request, response);
				}
			} else {
				if (log.isInfoEnabled()) {
					log.info("Request is not a sling request.");
				}
				chain.doFilter(request, response);
			}
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Filter is disabled.");
			}
			chain.doFilter(request, response);
		}
	}

	private void cacheRequestedResource(
			SlingHttpServletRequest slingHttpServletRequest, ServletResponse response, FilterChain chain,
			ResourceTypeCacheConfiguration cacheConfiguration) throws IOException, ServletException {
		Resource resource = slingHttpServletRequest.getResource();
		if (log.isInfoEnabled()) {
			log.info("filtering path=[{" + resource.getPath() + "}],resourceType=[{" + resource.getResourceType()
					+ "}],shouldFilter=[{true}]");
		}

		byte[] result = getResult(slingHttpServletRequest, response, chain, cacheConfiguration);
		response.getWriter().write(new String(result, response.getCharacterEncoding()));
	}

	private byte[] getResult(SlingHttpServletRequest httpRequest, ServletResponse response,
			FilterChain chain, ResourceTypeCacheConfiguration cacheConfiguration) throws ServletException,
			IOException {

		Cache cache = this.admin.getAppScopeCache(servletContext);
		String generatedKey = generator.generateKey(cacheConfiguration.getCacheLevel(), httpRequest.getResource(),
				httpRequest.getRequestPathInfo().getSelectorString());

		ByteArrayOutputStream result = null;

		try {
			result = (ByteArrayOutputStream) cache.getFromCache(generatedKey);

			if (log.isInfoEnabled()) {
				log.info("<cache>: Using cached entry for " + generatedKey);
			}
		} catch (NeedsRefreshException nre) {
			if (log.isInfoEnabled()) {
				log.info("<cache>: New cache entry, cache stale or cache scope flushed for " + generatedKey);
			}

			CacheHttpServletResponseWrapper cacheResponse = new CacheHttpServletResponseWrapper(
					(HttpServletResponse) response);
			chain.doFilter(httpRequest, cacheResponse);
			cacheResponse.getWriter().flush();
			result = cacheResponse.getContent();

			FilterJcrRefreshPolicy refreshPolicy = new FilterJcrRefreshPolicy(jcrEventsService, key, cacheConfiguration);
			try {
				cache.putInCache(generatedKey, result, refreshPolicy);
			} finally {
				// finally block used to make sure that all data binded to the current thread is cleared
				SilentRemovalNotificator.notifyListeners(cache);
			}
			cache.addCacheEventListener(refreshPolicy);
			jcrEventsService.addEventListener(refreshPolicy);
		}

		return result.toByteArray();
	}

	@Override
	public String createCacheKey(HttpServletRequest request, ServletCacheAdministrator cacheAdmin, Cache cache) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String[] createCacheGroups(HttpServletRequest request, ServletCacheAdministrator cacheAdmin,
			Cache cache) {
		return null;
	}
}
