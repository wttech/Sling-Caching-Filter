package com.cognifide.cq.cache.filter;

import com.cognifide.cq.cache.cache.CacheHolder;
import com.cognifide.cq.cache.filter.osgi.CacheConfiguration;
import com.cognifide.cq.cache.model.ResourceTypeCacheConfiguration;
import com.cognifide.cq.cache.model.reader.ResourceTypeCacheConfigurationReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.osgi.service.component.ComponentContext;

/**
 * @author Bartosz Rudnicki
 */
@Ignore
public class ComponentCacheFilterTest {

	private static final String CACHE_ADMINISTRATORS_KEY = "__oscache_admins";

	private static final String RESOURCE_TYPE = "/resource/type";

	private static final int CACHE_DURATION = 10;

	@Mock
	private FilterConfig filterConfig;

	@Mock
	private ComponentContext componentContext;

	@Mock
	private ServletContext servletContext;

	@Mock
	private SlingHttpServletRequest request;

	@Mock
	private ServletRequest otherRequest;

	@Mock
	private SlingHttpServletResponse response;

	@Mock
	private Resource resource;

	@Mock
	private ResourceResolver resourceResolver;

	@Mock
	private RequestPathInfo requestPathInfo;

	@Mock
	private PrintWriter writer;

	@Mock
	private FilterChain filterChain;

	@Mock
	private ResourceTypeCacheConfiguration resourceTypeCacheConfiguration;

	@Mock
	private ResourceTypeCacheConfigurationReader configurationReader;

	@Mock
	private CacheHolder cacheHolder;

	@Mock
	private CacheConfiguration cacheConfiguration;

	@InjectMocks
	private ComponentCacheFilter testedObject = new ComponentCacheFilter();

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void shouldInitializeAndHaveCacheDisabledWhenFilterNotActivated() {
		//given
		setUpFilterConfig();

		//when
		testedObject.init(filterConfig);

		//then
		verify(filterConfig).getServletContext();
//		verify(cacheHolder).create(servletContext, false);
		verify(servletContext).setAttribute(ComponentCacheFilter.SERVLET_CONTEXT_CACHE_ENABLED, false);
	}

	@Test
	public void shouldInitializeAndHaveCacheEnabledWhenFilterActivated() {
		//given
		setUpCacheConfiguration();
		setUpFilterConfig();

		//when
		testedObject.init(filterConfig);
		testedObject.activate(componentContext);

		//then
		verify(filterConfig).getServletContext();
//		verify(cacheHolder).create(servletContext, false);
//		verify(cacheHolder).create(servletContext, true);
		verify(servletContext, times(2)).setAttribute(ComponentCacheFilter.SERVLET_CONTEXT_CACHE_ENABLED, true);
		verify(servletContext, times(2)).setAttribute(ComponentCacheFilter.SERVLET_CONTEXT_CACHE_DURATION, CACHE_DURATION);
	}

	private void setUpCacheConfiguration() {
		when(cacheConfiguration.isEnabled()).thenReturn(true);
		when(cacheConfiguration.getDuration()).thenReturn(CACHE_DURATION);
		when(cacheConfiguration.getKey()).thenReturn(CACHE_ADMINISTRATORS_KEY);
	}

	private void setUpFilterConfig() {
		when(filterConfig.getServletContext()).thenReturn(servletContext);
		when(servletContext.getAttribute(CACHE_ADMINISTRATORS_KEY)).thenReturn(new HashMap<Object, Object>(2));
	}

	@Test
	public void shouldDisableCacheForServletContextWhenFilterIsDisabled() {
		//when
		testedObject.activate(componentContext);

		//then
		verify(servletContext).setAttribute("com.cognifide.cq.cache.filter.ComponentCacheFilter.cache.enabled", false);
	}

	@Test
	public void shouldCallDestoryOnCacheHolder() {
		//given
		setUpFilterConfig();

		//when
		testedObject.activate(componentContext);
		testedObject.init(filterConfig);
		testedObject.destroy();

		//then
//		verify(cacheHolder).destroy();
	}

	@Test
	public void shouldCacheResourceIfConfigurationIsEnabled() throws IOException, ServletException {
		//given
		setUpFilterConfig();
		setUpCacheConfiguration();
		setUpConfigurationReaderWithCachedConfigurationEnabled();
		setUpRequest();
		setUpResponse();
//		when(cacheHolder.get(eq(RESOURCE_TYPE), anyString())).thenThrow(NeedsRefreshException.class);

		//when
		doFilter();

		//then
		verify(filterChain).doFilter(eq(request), any(CacheHttpServletResponseWrapper.class));
		verify(writer).write(anyString());
	}

	private void doFilter() throws IOException, ServletException {
		testedObject.init(filterConfig);
		testedObject.activate(componentContext);
		testedObject.doFilter(request, response, filterChain);
	}

	private void setUpConfigurationReaderWithCachedConfigurationEnabled() {
		when(configurationReader.hasConfigurationFor(request)).thenReturn(true);
		when(configurationReader.readComponentConfiguration(request))
				.thenReturn(resourceTypeCacheConfiguration);
		when(resourceTypeCacheConfiguration.isEnabled()).thenReturn(true);
	}

	private void setUpRequest() {
		when(request.getRequestPathInfo()).thenReturn(requestPathInfo);
		when(requestPathInfo.getSelectorString()).thenReturn(StringUtils.EMPTY);
		when(request.getResourceResolver()).thenReturn(resourceResolver);
		when(resourceResolver.getResource(RESOURCE_TYPE)).thenReturn(resource);
		when(request.getResource()).thenReturn(resource);
		when(resource.getResourceType()).thenReturn("/resource/type");
		when(resource.getPath()).thenReturn("/path/to/resource/jcr:content/resource");
	}

	private void setUpResponse() throws IOException {
		when(response.getCharacterEncoding()).thenReturn("utf-8");
		when(response.getWriter()).thenReturn(writer);
	}

	@Test
	public void shouldGoFurtherOnFilterChainWhenCacheConfigurationIsDisabled() throws IOException, ServletException {
		//given
		setUpFilterConfig();
		setUpConfigurationReaderWithCachedConfigurationDisabled();
		setUpRequest();

		//when
		doFilter();

		//then
		verify(filterChain).doFilter(request, response);
		verify(writer, never()).write(anyString());
	}

	private void setUpConfigurationReaderWithCachedConfigurationDisabled() {
		when(configurationReader.hasConfigurationFor(request)).thenReturn(true);
		when(configurationReader.readComponentConfiguration(request))
				.thenReturn(resourceTypeCacheConfiguration);
		when(resourceTypeCacheConfiguration.isEnabled()).thenReturn(false);
	}

	@Test
	public void shouldNotCacheResourceIfConfigurationIsDisabled() throws IOException, ServletException {
		//given
		setUpFilterConfig();
		setUpCacheConfiguration();
		setUpConfigurationReaderWithNoConfigurationForGivenRequest();
		setUpRequest();

		//when
		doFilter();

		//then
		verify(configurationReader).hasConfigurationFor(request);
		verify(configurationReader, never()).readComponentConfiguration(request);
		verify(filterChain).doFilter(request, response);
	}

	private void setUpConfigurationReaderWithNoConfigurationForGivenRequest() {
		when(configurationReader.hasConfigurationFor(request)).thenReturn(false);
	}

	@Test
	public void shouldNotExecuteWhenRequestIsNotSlingHttpServletRequestInstance() throws IOException, ServletException {
		//given
		setUpFilterConfig();

		//when
		testedObject.init(filterConfig);
		testedObject.activate(componentContext);
		testedObject.doFilter(otherRequest, response, filterChain);

		//then
		verifyZeroInteractions(otherRequest);
		verify(filterChain).doFilter(otherRequest, response);
	}

	@Test
	public void shouldNotExecuteWhenFitlerIsDiabled() throws IOException, ServletException {
		//given
		setUpFilterConfig();

		//when
		doFilter();

		//then
		verify(request, never()).getResource();
		verify(filterChain).doFilter(request, response);
	}
}
