package com.cognifide.cq.cache.filter;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.cognifide.cq.cache.model.ResourceResolverStub;
import com.cognifide.cq.cache.refresh.jcr.JcrEventListener;
import com.cognifide.cq.cache.refresh.jcr.JcrEventsService;
import com.cognifide.cq.cache.test.utils.ComponentContextStub;
import com.cognifide.cq.cache.test.utils.ReflectionHelper;
import com.cognifide.cq.cache.test.utils.ResourceStub;
import com.cognifide.cq.cache.test.utils.ServletContextStub;
import com.cognifide.cq.cache.test.utils.SlingHttpServletRequestStub;
import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.base.EntryRefreshPolicy;
import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.base.events.CacheEventListener;
import com.opensymphony.oscache.web.ServletCacheAdministrator;

/**
 * @author Bartosz Rudnicki
 */
public class ComponentCacheFilterTest {

	private ComponentCacheFilterTestWrapper filter;

	private ServletContextStub servletContext;

	private ServletCacheAdministrator cacheAdministratorMock;

	private FilterChain filterChainMock;

	private SlingHttpServletRequestStub slingHttpServletRequest;

	private ComponentContextStub componentContext;

	private ResourceResolverStub resourceResolver;

	@Before
	public void setUp() {
		filter = new ComponentCacheFilterTestWrapper();
		servletContext = new ServletContextStub();
		cacheAdministratorMock = createMock(ServletCacheAdministrator.class);
		servletContext.setAttribute(ServletContextStub.CACHE_ADMIN_KEY, cacheAdministratorMock);
		filterChainMock = createMock(FilterChain.class);
		slingHttpServletRequest = new SlingHttpServletRequestStub();
		componentContext = new ComponentContextStub();

		resourceResolver = new ResourceResolverStub();
		slingHttpServletRequest.setResourceResolver(resourceResolver);
	}

	@After
	public void tearDown() {
	}

	private void initFilter() {
		FilterConfig filterConfigMock = createMock(FilterConfig.class);
		expect(filterConfigMock.getServletContext()).andReturn(servletContext);
		replay(filterConfigMock);
		filter.init(filterConfigMock);
		verify(filterConfigMock);
	}

	private void activateFilter(Boolean enabled) {
		activateFilter(enabled, null);
	}

	private void activateFilter(Boolean enabled, Integer duration) {
		if (enabled != null) {
			componentContext.put("cache.config.enabled", enabled);
		}
		if (duration != null) {
			componentContext.put("cache.config.duration", duration);
		}
		filter.activate(componentContext);
	}

	@Test
	public void testInit() throws IllegalAccessException, NoSuchFieldException {
		initFilter();
		assertNotNull(servletContext.getAttribute(ComponentCacheFilter.SERVLET_CONTEXT_CACHE_ENABLED));
		assertFalse((Boolean) servletContext.getAttribute(ComponentCacheFilter.SERVLET_CONTEXT_CACHE_ENABLED));
		assertNull(servletContext.getAttribute(ComponentCacheFilter.SERVLET_CONTEXT_CACHE_DURATION));

		activateFilter(true, 10);

		initFilter();
		assertNotNull(servletContext.getAttribute(ComponentCacheFilter.SERVLET_CONTEXT_CACHE_ENABLED));
		assertTrue((Boolean) servletContext.getAttribute(ComponentCacheFilter.SERVLET_CONTEXT_CACHE_ENABLED));
		assertNotNull(servletContext.getAttribute(ComponentCacheFilter.SERVLET_CONTEXT_CACHE_DURATION));
		assertEquals(10, servletContext.getAttribute(ComponentCacheFilter.SERVLET_CONTEXT_CACHE_DURATION));
	}

	@Test
	public void testDestroy() {
		initFilter();

		Map<Object, Object> admins = new HashMap<Object, Object>();
		servletContext.setAttribute(ServletContextStub.CACHE_ADMINS_LIST_KEY, admins);

		assertNotNull(servletContext.getAttribute(ServletContextStub.CACHE_ADMINS_LIST_KEY));
		filter.destroy();
		assertNull(servletContext.getAttribute(ServletContextStub.CACHE_ADMINS_LIST_KEY));
	}

	@Test
	public void testActivate() {
		initFilter();
		activateFilter(true, 121);
		assertNotNull(servletContext.getAttribute(ComponentCacheFilter.SERVLET_CONTEXT_CACHE_ENABLED));
		assertTrue((Boolean) servletContext.getAttribute(ComponentCacheFilter.SERVLET_CONTEXT_CACHE_ENABLED));
		assertEquals(121, servletContext.getAttribute(ComponentCacheFilter.SERVLET_CONTEXT_CACHE_DURATION));
	}

	@Test
	public void testActivateNoInit() {
		activateFilter(false);
		assertNull(servletContext.getAttribute(ComponentCacheFilter.SERVLET_CONTEXT_CACHE_ENABLED));
	}

	@Test
	public void testDeactivate() throws IllegalAccessException, NoSuchFieldException {
		initFilter();

		Map<Object, Object> admins = new HashMap<Object, Object>();
		servletContext.setAttribute(ServletContextStub.CACHE_ADMINS_LIST_KEY, admins);

		JcrEventsService.addEventListener(createMock(JcrEventListener.class));
		List<JcrEventListener> jcrEventListenerList = ReflectionHelper.get(JcrEventsService.class,
				"listeners");
		assertEquals(1, jcrEventListenerList.size());

		assertNotNull(servletContext.getAttribute(ServletContextStub.CACHE_ADMINS_LIST_KEY));

		filter.deactivate(null);

		assertNull(servletContext.getAttribute(ServletContextStub.CACHE_ADMINS_LIST_KEY));
		assertTrue(jcrEventListenerList.isEmpty());
	}

	@Test
	public void testDoFilterContentInCache() throws IOException, ServletException, NeedsRefreshException {
		ResourceStub resource = new ResourceStub();
		slingHttpServletRequest.setResource(resource);
		resource.setPath("/content/resource/path");
		resource.setResourceType("/apps/resource/type");

		componentContext.put("cache.config.resource-types", "/apps/resource/type");

		initFilter();
		activateFilter(true, 10);

		ServletResponse response = createMock(ServletResponse.class);

		Cache cache = createMock(Cache.class);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.write("helloWorld".getBytes());

		String s = "text/html";

		expect(cacheAdministratorMock.getAppScopeCache(servletContext)).andReturn(cache);
		expect(cache.getFromCache("/content/resource/path")).andReturn(baos);
		expect(cache.getFromCache("/content/resource/path_ResponseContentType")).andReturn(s);

		PrintWriter writer = createMock(PrintWriter.class);

		response.setContentType(s);
		expect(response.getCharacterEncoding()).andReturn("UTF-8");
		expect(response.getWriter()).andReturn(writer);

		writer.write("helloWorld");

		replay(filterChainMock, response, cacheAdministratorMock, cache, writer);
		filter.doFilter(slingHttpServletRequest, response, filterChainMock);
		verify(filterChainMock, response, cacheAdministratorMock, cache, writer);
	}

	@Test
	public void testDoFilterContentNotInCache() throws IOException, ServletException, NeedsRefreshException {
		ResourceStub resource = new ResourceStub();
		slingHttpServletRequest.setResource(resource);
		resource.setPath("/content/resource/path");
		resource.setResourceType("/apps/resource/type");

		componentContext.put("cache.config.resource-types", "/apps/resource/type");

		initFilter();
		activateFilter(true, 10);

		HttpServletResponse response = createMock(HttpServletResponse.class);

		Cache cache = createMock(Cache.class);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		expect(cacheAdministratorMock.getAppScopeCache(servletContext)).andReturn(cache);

		expect(cache.getFromCache("/content/resource/path")).andThrow(new NeedsRefreshException(baos));

		FilterChain filterChainStub = new FilterChain() {
			@Override
			public void doFilter(ServletRequest request, ServletResponse response) throws IOException,
					ServletException {
				assertEquals(slingHttpServletRequest, request);
				response.getWriter().write("helloWorld");
			}
		};

		cache.putInCache((String) notNull(), (Object) notNull(), (EntryRefreshPolicy) notNull());
		cache.putInCache((String) notNull(), (Object) notNull(), (EntryRefreshPolicy) notNull());
		cache.addCacheEventListener((CacheEventListener) notNull());

		PrintWriter writer = createMock(PrintWriter.class);
		expect(response.getCharacterEncoding()).andReturn("UTF-8");
		expect(response.getCharacterEncoding()).andReturn("UTF-8");
		expect(response.getContentType()).andReturn("text/html");
		expect(response.getWriter()).andReturn(writer);

		writer.write("helloWorld");

		replay(filterChainMock, response, cacheAdministratorMock, cache, writer);
		filter.doFilter(slingHttpServletRequest, response, filterChainStub);
		verify(filterChainMock, response, cacheAdministratorMock, cache, writer);
	}

	@Test
	public void testDoFilterCacheConfigDisabled() throws IOException, ServletException {
		ResourceStub resource = new ResourceStub();
		slingHttpServletRequest.setResource(resource);
		resource.setPath("/content/resource/path");
		resource.setResourceType("/apps/resource/type");

		initFilter();
		activateFilter(true, 10);

		ServletResponse response = createMock(ServletResponse.class);

		filterChainMock.doFilter(slingHttpServletRequest, response);

		replay(filterChainMock, response);
		filter.doFilter(slingHttpServletRequest, response, filterChainMock);
		verify(filterChainMock, response);
	}

	@Test
	public void testDoFilterDisabled() throws IOException, Throwable {
		initFilter();
		activateFilter(false);

		ServletRequest request = createMock(ServletRequest.class);
		ServletResponse response = createMock(ServletResponse.class);

		filterChainMock.doFilter(request, response);

		replay(filterChainMock, request, response);
		filter.doFilter(request, response, filterChainMock);
		verify(filterChainMock, request, response);
	}

	@Test
	public void testDoFilterNoHttpRequest() throws IOException, Throwable {
		initFilter();
		activateFilter(true);

		ServletRequest request = createMock(ServletRequest.class);
		ServletResponse response = createMock(ServletResponse.class);

		filterChainMock.doFilter(request, response);

		replay(filterChainMock, request, response);
		filter.doFilter(request, response, filterChainMock);
		verify(filterChainMock, request, response);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testCreateCacheKey() {
		filter.createCacheKey(null, null, null);
	}

	@Test
	public void testCreateCacheGroups() {
		assertNull(filter.createCacheGroups(null, null, null));
	}
}
