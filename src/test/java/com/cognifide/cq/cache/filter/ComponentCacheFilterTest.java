package com.cognifide.cq.cache.filter;

import com.cognifide.cq.cache.model.PathAliasStore;
import com.cognifide.cq.cache.refresh.jcr.FilterJcrRefreshPolicy;
import com.cognifide.cq.cache.refresh.jcr.JcrEventsService;
import com.cognifide.cq.cache.test.utils.ServletContextStub;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Dictionary;
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
import org.apache.sling.api.resource.ValueMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeast;
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
public class ComponentCacheFilterTest {

	private static final String CACHE_ADMINISTRATORS_KEY = "__oscache_admins";

	@Mock
	private FilterConfig filterConfig;

	@Mock
	private ComponentContext componentContext;

	@Mock
	private Dictionary dictionary;

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
	private ValueMap valueMap;

	@Mock
	private RequestPathInfo requestPathInfo;

	@Mock
	private PrintWriter writer;

	@Mock
	private FilterChain filterChain;

	@Mock
	private JcrEventsService jcrEventsService;

	@Mock
	private PathAliasStore pathAliasStore;

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
		verify(filterConfig, times(1)).getServletContext();
		verify(servletContext, times(1)).setAttribute(ComponentCacheFilter.SERVLET_CONTEXT_CACHE_ENABLED, Boolean.FALSE);
	}

	@Test
	public void shouldInitializeAndHaveCacheEnabledWhenFilterActivated() {
		//given
		setUpFilterConfig();
		setUpComponentContextWithEnabledSettings();

		//when
		testedObject.activate(componentContext);
		testedObject.init(filterConfig);

		//then
		verify(filterConfig, times(1)).getServletContext();
		verify(servletContext, times(2)).setAttribute(ComponentCacheFilter.SERVLET_CONTEXT_CACHE_ENABLED, Boolean.TRUE);
		verify(servletContext, times(2)).setAttribute(ComponentCacheFilter.SERVLET_CONTEXT_CACHE_DURATION, 10);
	}

	private void setUpFilterConfig() {
		when(filterConfig.getServletContext()).thenReturn(servletContext);
		when(servletContext.getAttribute(ServletContextStub.CACHE_ADMINS_LIST_KEY))
				.thenReturn(new HashMap<Object, Object>(2));
	}

	private void setUpComponentContextWithEnabledSettings() {
		when(componentContext.getProperties()).thenReturn(dictionary);
		when(dictionary.get("cache.config.enabled")).thenReturn(true);
		when(dictionary.get("cache.config.duration")).thenReturn(10);
	}

	@Test
	public void shouldDisableCacheForServletContextWhenFilterIsDisabled() {
		//given
		setUpComponentContextWithDisabledSettings();

		//when
		testedObject.activate(componentContext);

		//then
		verify(servletContext, times(1))
				.setAttribute("com.cognifide.cq.cache.filter.ComponentCacheFilter.cache.enabled", false);
	}

	private void setUpComponentContextWithDisabledSettings() {
		when(componentContext.getProperties()).thenReturn(dictionary);
		when(dictionary.get("cache.config.enabled")).thenReturn(false);
	}

	@Test
	public void shouldRemoveCachedAdministratorsKeysFromServletContextOnDestroy() {
		//given
		setUpFilterConfig();
		setUpComponentContextWithEnabledSettings();

		//when
		testedObject.activate(componentContext);
		testedObject.init(filterConfig);
		testedObject.destroy();

		//then
		verify(servletContext, atLeast(1)).getAttribute(CACHE_ADMINISTRATORS_KEY);
		verify(servletContext, times(1)).removeAttribute(CACHE_ADMINISTRATORS_KEY);
	}

	@Test
	public void shouldCacheResourceIfConfigurationIsEnabledInOsgi() throws IOException, ServletException {
		//given
		setUpFilterConfig();
		setUpComponentContextWithEnabledSettings();
		setUpRequest();
		setUpResponse();

		when(dictionary.get("cache.config.resource-types")).thenReturn(new String[]{"/resource/type"});

		//when
		testedObject.init(filterConfig);
		testedObject.activate(componentContext);
		testedObject.doFilter(request, response, filterChain);

		//then
		verify(filterChain, times(1))
				.doFilter(eq(request), any(CacheHttpServletResponseWrapper.class));
		verify(jcrEventsService, times(1)).addEventListener(any(FilterJcrRefreshPolicy.class));
		verify(writer, times(1)).write(anyString());
	}

	@Test
	public void shouldCacheResourceIfConfigurationIsEnabledInJcr() throws IOException, ServletException {
		//given
		setUpFilterConfig();
		setUpComponentContextWithEnabledSettings();
		setUpRequest();
		setUpResource();
		setUpResponse();

		//when
		testedObject.init(filterConfig);
		testedObject.activate(componentContext);
		testedObject.doFilter(request, response, filterChain);

		//then
		verify(filterChain, times(1))
				.doFilter(eq(request), any(CacheHttpServletResponseWrapper.class));
		verify(jcrEventsService, times(1)).addEventListener(any(FilterJcrRefreshPolicy.class));
		verify(writer, times(1)).write(anyString());
	}

	private void setUpResponse() throws IOException {
		when(response.getCharacterEncoding()).thenReturn("utf-8");
		when(response.getWriter()).thenReturn(writer);
	}

	private void setUpResource() {
		when(resourceResolver.getResource(resource, "cache")).thenReturn(resource);
		when(resource.adaptTo(ValueMap.class)).thenReturn(valueMap);
		when(valueMap.get("cog:cacheLevel", Integer.MIN_VALUE)).thenReturn(Integer.MIN_VALUE);
		when(valueMap.get("cog:validityTime", 10)).thenReturn(10);
		when(valueMap.get("cog:cacheEnabled", false)).thenReturn(true);
		when(valueMap.get("cog:invalidateOnSelf", true)).thenReturn(true);
		when(valueMap.get("cog:invalidateOnPaths", false)).thenReturn(true);
		when(valueMap.get("cog:cacheEnabled")).thenReturn(new String[0]);
		when(valueMap.get("cog:invalidateOnReferencedFields")).thenReturn(new String[0]);
	}

	@Test
	public void shouldNotCacheResourceIfConfigurationIsDisabledInJcr() throws IOException, ServletException {
		//given
		setUpFilterConfig();
		setUpComponentContextWithEnabledSettings();
		setUpRequest();

		//when
		testedObject.init(filterConfig);
		testedObject.activate(componentContext);
		testedObject.doFilter(request, response, filterChain);

		//then
		verify(filterChain, times(1)).doFilter(request, response);
	}

	private void setUpRequest() {
		when(request.getRequestPathInfo()).thenReturn(requestPathInfo);
		when(requestPathInfo.getSelectorString()).thenReturn(StringUtils.EMPTY);
		when(request.getResourceResolver()).thenReturn(resourceResolver);
		when(resourceResolver.getResource("/resource/type")).thenReturn(resource);
		when(request.getResource()).thenReturn(resource);
		when(resource.getResourceType()).thenReturn("/resource/type");
		when(resource.getPath()).thenReturn("/path/to/resource/jcr:content/resource");
	}

	@Test
	public void shouldNotExecuteWhenRequestIsNotSlingHttpServletRequestInstance() throws IOException, ServletException {
		//given
		setUpFilterConfig();
		setUpComponentContextWithEnabledSettings();

		//when
		testedObject.init(filterConfig);
		testedObject.activate(componentContext);
		testedObject.doFilter(otherRequest, response, filterChain);

		//then
		verifyZeroInteractions(otherRequest);
		verify(filterChain, times(1)).doFilter(otherRequest, response);
	}

	@Test
	public void shouldNotExecuteWhenFitlerIsDiabled() throws IOException, ServletException {
		//given
		setUpFilterConfig();
		setUpComponentContextWithDisabledSettings();

		//when
		testedObject.init(filterConfig);
		testedObject.activate(componentContext);
		testedObject.doFilter(request, response, filterChain);

		//then
		verify(request, never()).getResource();
		verify(filterChain, times(1)).doFilter(request, response);
	}

	@Test
	public void createCacheKeyShouldThrowUnsupportedOperationException() {
		//given
		exception.expect(UnsupportedOperationException.class);
		//then
		testedObject.createCacheKey(null, null, null);
	}

	@Test
	public void createCacheGroupsShouldReturnNull() {
		//when
		String[] actual = testedObject.createCacheGroups(request, null, null);

		//then
		assertThat(actual, is(nullValue()));
	}
}
