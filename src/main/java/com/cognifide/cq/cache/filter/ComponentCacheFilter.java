package com.cognifide.cq.cache.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
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
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.osgi.service.component.ComponentContext;

import com.cognifide.cq.cache.algorithm.SilentRemovalNotificator;
import com.cognifide.cq.cache.model.CacheConfigurationEntry;
import com.cognifide.cq.cache.model.CacheConfigurationParser;
import com.cognifide.cq.cache.model.CacheConfigurationParserImpl;
import com.cognifide.cq.cache.model.CacheKeyGenerator;
import com.cognifide.cq.cache.model.CacheKeyGeneratorImpl;
import com.cognifide.cq.cache.model.PathAlias;
import com.cognifide.cq.cache.model.PathAliasReader;
import com.cognifide.cq.cache.model.PathAliasStore;
import com.cognifide.cq.cache.model.ResourceTypeCacheConfiguration;
import com.cognifide.cq.cache.model.ResourceTypeCacheConfigurationReader;
import com.cognifide.cq.cache.refresh.jcr.FilterJcrRefreshPolicy;
import com.cognifide.cq.cache.refresh.jcr.JcrEventsService;
import com.opensymphony.oscache.base.AbstractCacheAdministrator;
import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.web.ServletCacheAdministrator;
import com.opensymphony.oscache.web.filter.ICacheGroupsProvider;
import com.opensymphony.oscache.web.filter.ICacheKeyProvider;

/**
 * Sling Caching Filter
 * 
 * @author Przemyslaw Pakulski
 * @author Jakub Malecki
 * @author Maciej Majchrzak
 * 
 * @scr.component immediate="true" label="Sling Caching Filter" description="Sling Caching Filter"
 * @scr.property name="filter.scope" value="component" private="true"
 * @scr.property name="filter.order" value="100" type="Integer" private="true"
 * 
 * @scr.property label="Enabled" nameRef="PROPERTY_FILTER_ENABLED" valueRef="DEFAULT_FILTER_ENABLED"
 * @scr.property label="Resource types" nameRef="PROPERTY_FILTER_RESOURCE_TYPES"
 * valueRef="DEFAULT_FILTER_RESOURCE_TYPES"
 * 
 * @scr.property label="Path aliases" nameRef="PROPERTY_PATH_ALIASES" valueRef="DEFAULT_PATH_ALIASES"
 * cardinality="Integer.MAX_INT"
 * 
 * @scr.property label="Capacity" nameRef="PROPERTY_CAPACITY" valueRef="DEFAULT_CAPACITY" type="Integer"
 * @scr.property label="Memory" nameRef="PROPERTY_MEMORY" valueRef="DEFAULT_MEMORY" type="Boolean"
 * @scr.property label="Algorithm" nameRef="PROPERTY_ALGORITHM" valueRef="DEFAULT_ALGORITHM"
 * @scr.property label="Blocking" nameRef="PROPERTY_BLOCKING" valueRef="DEFAULT_BLOCKING" type="Boolean"
 * @scr.property label="Unlimited disk" nameRef="PROPERTY_UNLIMITED_DISK" valueRef="DEFAULT_UNLIMITED_DISK"
 * type="Boolean"
 * @scr.property label="Persistence class" nameRef="PROPERTY_PERSISTENCE_CLASS"
 * valueRef="DEFAULT_PERSISTENCE_CLASS"
 * @scr.property label="Path" nameRef="PROPERTY_PATH" valueRef="DEFAULT_PATH"
 * @scr.property label="Persistence overflow only" nameRef="PROPERTY_PERSISTENCE_OVERFLOW_ONLY"
 * valueRef="DEFAULT_PERSISTENCE_OVERFLOW_ONLY" type="Boolean"
 * @scr.property label="Event listeners" nameRef="PROPERTY_EVENT_LISTENERS" valueRef="DEFAULT_EVENT_LISTENERS"
 * @scr.property label="Key" nameRef="PROPERTY_KEY" valueRef="DEFAULT_KEY"
 * @scr.property label="Use host domain in key" nameRef="PROPERTY_USE_HOST_DOMAIN_IN_KEY"
 * valueRef="DEFAULT_USE_HOST_DOMAIN_IN_KEY" type="Boolean"
 * @scr.property label="Duration" nameRef="PROPERTY_DURATION" valueRef="DEFAULT_DURATION" type="Integer" *
 * @scr.service
 */
public class ComponentCacheFilter implements Filter, ICacheKeyProvider, ICacheGroupsProvider {

	// Cache config properties

	private static final String PROPERTY_FILTER_ENABLED = "cache.config.enabled";

	private static final Boolean DEFAULT_FILTER_ENABLED = Boolean.FALSE;

	private static final String PROPERTY_FILTER_RESOURCE_TYPES = "cache.config.resource-types";

	private static final String[] DEFAULT_FILTER_RESOURCE_TYPES = new String[] {
			"foundation/components/logo", "geometrixx/components/header" };

	private static final String PROPERTY_MEMORY = "cache.config.memory";

	private static final boolean DEFAULT_MEMORY = true;

	private static final String PROPERTY_CAPACITY = "cache.config.capacity";

	private static final int DEFAULT_CAPACITY = 1000;

	private static final String PROPERTY_ALGORITHM = "cache.config.algorithm";

	private static final String DEFAULT_ALGORITHM = "com.cognifide.cq.cache.algorithm.LRUCache";

	private static final String PROPERTY_BLOCKING = "cache.config.blocking";

	private static final boolean DEFAULT_BLOCKING = false;

	private static final String PROPERTY_UNLIMITED_DISK = "cache.config.unlimiteddisk";

	private static final boolean DEFAULT_UNLIMITED_DISK = false;

	private static final String PROPERTY_PERSISTENCE_CLASS = "cache.config.persistenceclass";

	private static final String DEFAULT_PERSISTENCE_CLASS = "com.opensymphony.oscache.plugins.diskpersistence.HashDiskPersistenceListener";

	private static final String PROPERTY_PATH = "cache.config.path";

	private static final String DEFAULT_PATH = "";

	private static final String PROPERTY_PERSISTENCE_OVERFLOW_ONLY = "cache.config.persistenceoverflowonly";

	private static final boolean DEFAULT_PERSISTENCE_OVERFLOW_ONLY = true;

	private static final String PROPERTY_EVENT_LISTENERS = "cache.config.eventlisteners";

	private static final String DEFAULT_EVENT_LISTENERS = "";

	private static final String PROPERTY_KEY = "cache.config.key";

	private static final String DEFAULT_KEY = "__oscache_cache";

	private static final String PROPERTY_USE_HOST_DOMAIN_IN_KEY = "cache.config.usehostdomaininkey";

	private static final boolean DEFAULT_USE_HOST_DOMAIN_IN_KEY = false;

	private static final String PROPERTY_DURATION = "cache.config.duration";

	private static final int DEFAULT_DURATION = 3600;

	private static final String PROPERTY_PATH_ALIASES = "cache.config.pathaliases";

	private static final String[] DEFAULT_PATH_ALIASES = new String[] { "", "" };

	// Cache config keys

	private static final String CACHE_PATH_KEY = "cache.path";

	private static final String CACHE_USE_HOST_DOMAIN_KEY = "cache.use.host.domain.in.key";

	private static final String CACHE_KEY_KEY = "cache.key";

	public static final String SERVLET_CONTEXT_CACHE_ENABLED = ComponentCacheFilter.class.getName()
			+ ".cache.enabled";

	public static final String SERVLET_CONTEXT_CACHE_DURATION = ComponentCacheFilter.class.getName()
			+ ".cache.duration";
	public static final String RESPONSE_CONTENT_TYPE_POSTFIX = "_ResponseContentType";

	// Properties read from configuration

	private boolean enabled;

	private Map<String, CacheConfigurationEntry> cacheConfigurationEntries;

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

	private final static Log log = LogFactory.getLog(ComponentCacheFilter.class);

	private ServletContext servletContext;

	private ServletCacheAdministrator admin;

	private CacheConfigurationParser parser;

	private PathAliasReader pathAliasReader;

	private CacheKeyGenerator generator;

	private Properties configProperties;

	private final ResourceTypeCacheConfigurationReader configurationReader = new ResourceTypeCacheConfigurationReader();

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

		parser = new CacheConfigurationParserImpl();
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
		JcrEventsService.clearEventListeners();
	}

	/**
	 * Read OSGi component configuration
	 * 
	 * @param context osgi component context
	 */
	protected void readConfiguration(ComponentContext context) {
		String[] resourceTypes = OsgiUtil
				.toStringArray(readProperty(context, PROPERTY_FILTER_RESOURCE_TYPES));
		if (resourceTypes == null) {
			resourceTypes = DEFAULT_FILTER_RESOURCE_TYPES;
		}
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

		cacheConfigurationEntries = parser.parseEntries(resourceTypes);

		Set<PathAlias> aliases = pathAliasReader.readAliases(aliasesStrings);
		PathAliasStore pathAliasStore = PathAliasStore.getInstance();
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
				Resource resource = slingHttpServletRequest.getResource();

				String resourceType = resource.getResourceType();

				ResourceTypeCacheConfiguration cacheConfiguration = configurationReader
						.readComponentConfiguration(slingHttpServletRequest, cacheConfigurationEntries,
								duration);

				if (cacheConfiguration.isEnabled()) {
					log.info("filtering path=[{" + resource.getPath() + "}],resourceType=[{" + resourceType
							+ "}],shouldFilter=[{" + cacheConfiguration.isEnabled() + "}]");

					byte[] result = getResult(slingHttpServletRequest, response, chain, cacheConfiguration);
					response.getWriter().write(new String(result, response.getCharacterEncoding()));
				} else {
					chain.doFilter(request, response);
				}
			} else {
				log.info("NOT A SLING REQUEST");
				chain.doFilter(request, response);
			}
		} else {
			log.debug("DISABLED");
			chain.doFilter(request, response);
		}
	}

	private byte[] getResult(SlingHttpServletRequest httpRequest, ServletResponse response,
			FilterChain chain, ResourceTypeCacheConfiguration cacheConfiguration) throws ServletException,
			IOException {

		Cache cache = this.admin.getAppScopeCache(servletContext);
		String key = generator.generateKey(cacheConfiguration.getCacheLevel(), httpRequest.getResource(),
				httpRequest.getRequestPathInfo().getSelectorString());

		String contentTypeCacheKey = key + RESPONSE_CONTENT_TYPE_POSTFIX;

		ByteArrayOutputStream result = null;

		try {
			result = (ByteArrayOutputStream) cache.getFromCache(key);
			String contentType = (String)cache.getFromCache(contentTypeCacheKey);
			response.setContentType(contentType);

			if (log.isInfoEnabled()) {
				log.info("<cache>: Using cached entry for " + key);
			}
		} catch (NeedsRefreshException nre) {
			if (log.isInfoEnabled()) {
				log.info("<cache>: New cache entry, cache stale or cache scope flushed for " + key);
			}

			CacheHttpServletResponseWrapper cacheResponse = new CacheHttpServletResponseWrapper(
					(HttpServletResponse) response);
			chain.doFilter(httpRequest, cacheResponse);
			cacheResponse.getWriter().flush();
			result = cacheResponse.getContent();

			FilterJcrRefreshPolicy refreshPolicy = new FilterJcrRefreshPolicy(key, cacheConfiguration);
			try {
				cache.putInCache(key, result, refreshPolicy);
				cache.putInCache(contentTypeCacheKey, response.getContentType(), refreshPolicy);
			} finally {
				// finally block used to make sure that all data binded to the current thread is cleared
				SilentRemovalNotificator.notifyListeners(cache);
			}
			cache.addCacheEventListener(refreshPolicy);
			JcrEventsService.addEventListener(refreshPolicy);
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
