package com.cognifide.cq.cache.filter;

import com.cognifide.cq.cache.filter.cache.CacheHolder;
import com.cognifide.cq.cache.filter.osgi.CacheConfiguration;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
import org.apache.felix.scr.annotations.sling.SlingFilter;
import org.apache.felix.scr.annotations.sling.SlingFilterScope;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.ComponentContext;

import com.cognifide.cq.cache.model.key.CacheKeyGenerator;
import com.cognifide.cq.cache.model.key.CacheKeyGeneratorImpl;
import com.cognifide.cq.cache.model.ResourceTypeCacheConfiguration;
import com.cognifide.cq.cache.model.reader.ResourceTypeCacheConfigurationReader;
import com.cognifide.cq.cache.refresh.jcr.FilterJcrRefreshPolicy;
import com.cognifide.cq.cache.refresh.jcr.JcrEventsService;
import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.web.ServletCacheAdministrator;
import com.opensymphony.oscache.web.filter.ICacheGroupsProvider;
import com.opensymphony.oscache.web.filter.ICacheKeyProvider;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;

/**
 * Sling Caching Filter
 *
 * @author Przemyslaw Pakulski
 * @author Jakub Malecki
 * @author Maciej Majchrzak
 */
@SlingFilter(scope = SlingFilterScope.COMPONENT, order = 100)
public class ComponentCacheFilter implements Filter, ICacheKeyProvider, ICacheGroupsProvider {

	private static final Log log = LogFactory.getLog(ComponentCacheFilter.class);

	// Cache config keys
	public static final String SERVLET_CONTEXT_CACHE_ENABLED = ComponentCacheFilter.class.getName()
			+ ".cache.enabled";

	public static final String SERVLET_CONTEXT_CACHE_DURATION = ComponentCacheFilter.class.getName()
			+ ".cache.duration";

	@Reference
	private JcrEventsService jcrEventsService;

	@Reference
	private ResourceTypeCacheConfigurationReader configurationReader;

	@Reference
	private CacheHolder cacheHolder;

	@Reference
	private CacheConfiguration cacheConfiguration;

	private ServletContext servletContext;

	private CacheKeyGenerator generator;

	@Override
	public void init(FilterConfig filterConfig) {
		log.info("init " + getClass());
		servletContext = filterConfig.getServletContext();
		cacheHolder.create(servletContext, false);
		setServletContextAttributes();
	}

	private void setServletContextAttributes() {
		if (cacheConfiguration.isEnabled()) {
			servletContext.setAttribute(SERVLET_CONTEXT_CACHE_ENABLED, Boolean.TRUE);
			servletContext.setAttribute(SERVLET_CONTEXT_CACHE_DURATION, cacheConfiguration.getDuration());
		} else {
			servletContext.setAttribute(SERVLET_CONTEXT_CACHE_ENABLED, Boolean.FALSE);
		}
	}

	@Activate
	protected void activate(ComponentContext context) {
		log.info("activate " + getClass());

		generator = new CacheKeyGeneratorImpl();

		// first time activate is called before init, so servletContext is null
		if (servletContext != null) {
			cacheHolder.create(servletContext, true);
			setServletContextAttributes();
		}
	}

	@Override
	public void destroy() {
		log.info("destroy " + getClass());
		cacheHolder.destroy();
	}

	@Deactivate
	protected void deactivate(ComponentContext context) {
		log.info("deactivate " + getClass());
		cacheHolder.destroy();
		jcrEventsService.clearEventListeners();
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		if (cacheConfiguration.isEnabled()) {
			if (request instanceof SlingHttpServletRequest) {
				handleRequest((SlingHttpServletRequest) request, response, chain);
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

	private void handleRequest(
			SlingHttpServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (configurationReader.hasConfigurationFor(request)) {
			ResourceTypeCacheConfiguration componentCacheConfiguration
					= configurationReader.readComponentConfiguration(request);

			if (null != componentCacheConfiguration && componentCacheConfiguration.isEnabled()) {
				cacheRequestedResource(request, response, chain, componentCacheConfiguration);
			} else {
				if (log.isInfoEnabled()) {
					log.info("Caching is disabled for " + request.getResource().getResourceType());
				}
				chain.doFilter(request, response);
			}
		} else {
			if (log.isInfoEnabled()) {
				log.info("There is no configuration for " + request.getResource().getResourceType());
			}
			chain.doFilter(request, response);
		}
	}

	private void cacheRequestedResource(SlingHttpServletRequest request, ServletResponse response,
			FilterChain chain, ResourceTypeCacheConfiguration cacheConfiguration) throws IOException, ServletException {
		Resource resource = request.getResource();
		if (log.isInfoEnabled()) {
			log.info("filtering path=[{" + resource.getPath() + "}],resourceType=[{" + resource.getResourceType()
					+ "}],shouldFilter=[{true}]");
		}

		byte[] result = getResult(request, response, chain, cacheConfiguration);
		response.getWriter().write(new String(result, response.getCharacterEncoding()));
	}

	private byte[] getResult(SlingHttpServletRequest request, ServletResponse response, FilterChain chain,
			ResourceTypeCacheConfiguration componentCacheConfiguration) throws ServletException, IOException {

		String generatedKey = generator.generateKey(componentCacheConfiguration.getCacheLevel(), request);
		ByteArrayOutputStream result = null;

		try {
			result = cacheHolder.get(request.getResource().getResourceType(), generatedKey);
		} catch (NeedsRefreshException nre) {
			CacheHttpServletResponseWrapper cacheResponse = new CacheHttpServletResponseWrapper(
					(HttpServletResponse) response);
			chain.doFilter(request, cacheResponse);
			cacheResponse.getWriter().flush();
			result = cacheResponse.getContent();

			FilterJcrRefreshPolicy refreshPolicy = new FilterJcrRefreshPolicy(
					jcrEventsService, cacheConfiguration.getKey(), componentCacheConfiguration);
			cacheHolder.put(generatedKey, result, refreshPolicy);
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
