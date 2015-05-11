package com.cognifide.cq.cache.filter;

import com.cognifide.cq.cache.model.alias.PathAliasStoreImpl;
import com.cognifide.cq.cache.model.ResourceTypeCacheConfiguration;
import com.cognifide.cq.cache.model.reader.ResourceTypeCacheConfigurationReader;
import com.cognifide.cq.cache.refresh.jcr.FilterJcrRefreshPolicy;
import com.cognifide.cq.cache.refresh.jcr.JcrEventsService;
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

	private static final String CACHE_CONFIG_DURATION_PROPERTY_NAME = "cache.config.duration";

	private static final String CACHE_CONFIG_ENABLED_PROPERTY_NAME = "cache.config.enabled";

	private static final int CACHE_DURATION = 10;

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
	private RequestPathInfo requestPathInfo;

	@Mock
	private PrintWriter writer;

	@Mock
	private FilterChain filterChain;

	@Mock
	private JcrEventsService jcrEventsService;

	@Mock
	private PathAliasStoreImpl pathAliasStore;

	@Mock
	private ResourceTypeCacheConfigurationReader configurationReader;

	@Mock
	private ResourceTypeCacheConfiguration resourceTypeCacheConfiguration;

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
		verify(servletContext).setAttribute(ComponentCacheFilter.SERVLET_CONTEXT_CACHE_ENABLED, Boolean.FALSE);
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
		verify(filterConfig).getServletContext();
		verify(servletContext, times(2)).setAttribute(ComponentCacheFilter.SERVLET_CONTEXT_CACHE_ENABLED, Boolean.TRUE);
		verify(servletContext, times(2))
				.setAttribute(ComponentCacheFilter.SERVLET_CONTEXT_CACHE_DURATION, CACHE_DURATION);
	}

	private void setUpFilterConfig() {
		when(filterConfig.getServletContext()).thenReturn(servletContext);
		when(servletContext.getAttribute(CACHE_ADMINISTRATORS_KEY)).thenReturn(new HashMap<Object, Object>(2));
	}

	private void setUpComponentContextWithEnabledSettings() {
		when(componentContext.getProperties()).thenReturn(dictionary);
		when(dictionary.get(CACHE_CONFIG_ENABLED_PROPERTY_NAME)).thenReturn(true);
		when(dictionary.get(CACHE_CONFIG_DURATION_PROPERTY_NAME)).thenReturn(CACHE_DURATION);
	}

	@Test
	public void shouldDisableCacheForServletContextWhenFilterIsDisabled() {
		//given
		setUpComponentContextWithDisabledSettings();

		//when
		testedObject.activate(componentContext);

		//then
		verify(servletContext).setAttribute("com.cognifide.cq.cache.filter.ComponentCacheFilter.cache.enabled", false);
	}

	private void setUpComponentContextWithDisabledSettings() {
		when(componentContext.getProperties()).thenReturn(dictionary);
		when(dictionary.get(CACHE_CONFIG_ENABLED_PROPERTY_NAME)).thenReturn(false);
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
		verify(servletContext).removeAttribute(CACHE_ADMINISTRATORS_KEY);
	}

	@Test
	public void shouldCacheResourceIfConfigurationIsEnabled() throws IOException, ServletException {
		//given
		setUpFilterConfig();
		setUpComponentContextWithEnabledSettings();
		setUpConfigurationReaderWithCachedConfigurationEnabled();
		setUpRequest();
		setUpResponse();

		//when
		testedObject.init(filterConfig);
		testedObject.activate(componentContext);
		testedObject.doFilter(request, response, filterChain);

		//then
		verify(filterChain).doFilter(eq(request), any(CacheHttpServletResponseWrapper.class));
		verify(jcrEventsService).addEventListener(any(FilterJcrRefreshPolicy.class));
		verify(writer).write(anyString());
	}

	private void setUpResponse() throws IOException {
		when(response.getCharacterEncoding()).thenReturn("utf-8");
		when(response.getWriter()).thenReturn(writer);
	}

	private void setUpConfigurationReaderWithCachedConfigurationEnabled() {
		when(configurationReader.hasConfigurationFor(request)).thenReturn(true);
		when(configurationReader.readComponentConfiguration(request, CACHE_DURATION))
				.thenReturn(resourceTypeCacheConfiguration);
		when(resourceTypeCacheConfiguration.isEnabled()).thenReturn(true);
	}

	@Test
	public void shouldNotCacheResourceIfConfigurationIsDisabled() throws IOException, ServletException {
		//given
		setUpFilterConfig();
		setUpComponentContextWithEnabledSettings();
		setUpConfigurationReaderWithCachedConfigurationDisabled();
		setUpRequest();

		//when
		testedObject.init(filterConfig);
		testedObject.activate(componentContext);
		testedObject.doFilter(request, response, filterChain);

		//then
		verify(configurationReader).hasConfigurationFor(request);
		verify(configurationReader, never()).readComponentConfiguration(request, CACHE_DURATION);
		verify(filterChain).doFilter(request, response);
	}

	private void setUpConfigurationReaderWithCachedConfigurationDisabled() {
		when(configurationReader.hasConfigurationFor(request)).thenReturn(false);
		when(configurationReader.readComponentConfiguration(request, CACHE_DURATION))
				.thenReturn(resourceTypeCacheConfiguration);
		when(resourceTypeCacheConfiguration.isEnabled()).thenReturn(false);
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
		verify(filterChain).doFilter(otherRequest, response);
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
		verify(filterChain).doFilter(request, response);
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
