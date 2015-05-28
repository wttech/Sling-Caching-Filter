package com.cognifide.cq.cache.filter;

import com.cognifide.cq.cache.cache.CacheHolder;
import com.cognifide.cq.cache.cache.callback.ResponseCallback;
import com.cognifide.cq.cache.filter.osgi.CacheConfiguration;
import com.cognifide.cq.cache.model.ResourceTypeCacheConfiguration;
import com.cognifide.cq.cache.model.reader.ResourceTypeCacheConfigurationReader;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingFilter;
import org.apache.felix.scr.annotations.sling.SlingFilterScope;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sling Caching Filter
 *
 * @author Przemyslaw Pakulski
 * @author Jakub Malecki
 * @author Maciej Majchrzak
 */
@SlingFilter(scope = SlingFilterScope.COMPONENT, order = 100)
public class ComponentCacheFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(ComponentCacheFilter.class);

	// Cache config keys
	public static final String SERVLET_CONTEXT_CACHE_ENABLED = ComponentCacheFilter.class.getName()
			+ ".cache.enabled";

	public static final String SERVLET_CONTEXT_CACHE_DURATION = ComponentCacheFilter.class.getName()
			+ ".cache.duration";

	@Reference
	private ResourceTypeCacheConfigurationReader configurationReader;

	@Reference
	private CacheHolder cacheHolder;

	@Reference
	private CacheConfiguration cacheConfiguration;

	private ServletContext servletContext;

	@Override
	public void init(FilterConfig filterConfig) {
		logger.info("Initializing {}", getClass());
		servletContext = filterConfig.getServletContext();
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
		logger.info("Activate {}", getClass());
		// first time activate is called before init, so servletContext is null
		if (servletContext != null) {
			setServletContextAttributes();
		}
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (cacheConfiguration.isEnabled()) {
			if (request instanceof SlingHttpServletRequest) {
				handleRequest((SlingHttpServletRequest) request, response, chain);
			} else {
				if (logger.isInfoEnabled()) {
					logger.info("Request is not a sling request.");
				}
				chain.doFilter(request, response);
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("Filter is disabled.");
			}
			chain.doFilter(request, response);
		}
	}

	private void handleRequest(SlingHttpServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (configurationReader.hasConfigurationFor(request)) {
			ResourceTypeCacheConfiguration componentCacheConfiguration
					= configurationReader.readComponentConfiguration(request);

			if (null != componentCacheConfiguration && componentCacheConfiguration.isEnabled()) {
				cacheRequestedResource(request, response, chain, componentCacheConfiguration);
			} else {
				if (logger.isInfoEnabled()) {
					logger.info("Caching is disabled for {}", request.getResource().getResourceType());
				}
				chain.doFilter(request, response);
			}
		} else {
			if (logger.isInfoEnabled()) {
				logger.info("There is no configuration for {}", request.getResource().getResourceType());
			}
			chain.doFilter(request, response);
		}
	}

	private void cacheRequestedResource(SlingHttpServletRequest request, ServletResponse response,
			FilterChain chain, ResourceTypeCacheConfiguration cacheConfiguration) throws IOException, ServletException {
		if (logger.isInfoEnabled()) {
			Resource resource = request.getResource();
			logger.info("Filtering path={}, resourceType={}", resource.getPath(), resource.getResourceType());
		}

		byte[] result = cacheHolder.putOrGet(request, cacheConfiguration,
				new ResponseCallback(chain, request, (HttpServletResponse) response)).toByteArray();
		response.getWriter().write(new String(result, response.getCharacterEncoding()));
	}
}
