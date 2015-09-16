package com.cognifide.cq.cache.filter;

import com.cognifide.cq.cache.cache.ByteStreamEntity;
import com.cognifide.cq.cache.cache.CacheEntity;
import com.cognifide.cq.cache.cache.CacheHolder;
import com.cognifide.cq.cache.cache.callback.MissingCacheEntryCallback;
import com.cognifide.cq.cache.filter.osgi.CacheConfiguration;
import com.cognifide.cq.cache.model.ResourceTypeCacheConfiguration;
import com.cognifide.cq.cache.model.reader.ResourceTypeCacheConfigurationReader;
import com.google.common.base.Optional;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.sling.api.SlingHttpServletRequest;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * @author Bartosz Rudnicki
 */
public class ComponentCacheFilterTest {

	private static final String CONTENT = "content";

	private static final String CONTENT_TYPE = "text/html";

	@Mock
	private SlingHttpServletRequest request;

	@Mock
	private ServletRequest noSlingHttpServletRequest;

	@Mock
	private HttpServletResponse response;

	@Mock
	private PrintWriter printWriter;

	@Mock
	private FilterChain filterChain;

	@Mock
	private ResourceTypeCacheConfigurationReader configurationReader;

	@Mock
	private ResourceTypeCacheConfiguration resourceTypeCacheConfiguration;

	@Mock
	private CacheHolder cacheHolder;

	@Mock
	private CacheConfiguration cacheConfiguration;

	@InjectMocks
	private ComponentCacheFilter testedObject = new ComponentCacheFilter();

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@Test
	public void shouldNotHandleWhenCacheConfigurationIsDisabled() throws IOException, ServletException {
		//given
		when(cacheConfiguration.isEnabled()).thenReturn(false);

		//when
		testedObject.doFilter(noSlingHttpServletRequest, response, filterChain);

		//then
		verify(filterChain).doFilter(noSlingHttpServletRequest, response);
		verifyNoMoreInteractions(noSlingHttpServletRequest, response, filterChain);
	}

	@Test
	public void shouldNotHandleWhenRequestIsNotSlingRequest() throws IOException, ServletException {
		//given
		when(cacheConfiguration.isEnabled()).thenReturn(true);

		//when
		testedObject.doFilter(noSlingHttpServletRequest, response, filterChain);

		//then
		verify(filterChain).doFilter(noSlingHttpServletRequest, response);
		verifyNoMoreInteractions(noSlingHttpServletRequest, response, filterChain);
	}

	@Test
	public void shouldNotHandleWhenConfigurationReaderDoNotHaveConfigurationForGivenResource() throws IOException, ServletException {
		//given
		when(cacheConfiguration.isEnabled()).thenReturn(true);
		when(configurationReader.readComponentConfiguration(request)).thenReturn(Optional.<ResourceTypeCacheConfiguration>absent());

		//when
		testedObject.doFilter(request, response, filterChain);

		//then
		verify(configurationReader).readComponentConfiguration(request);
		verify(filterChain).doFilter(request, response);
		verifyNoMoreInteractions(request, response, filterChain, configurationReader);
	}

	@Test
	public void shouldNotHandleWhenResourceTypeCacheConfigurationIsDisabled() throws IOException, ServletException {
		//given
		when(cacheConfiguration.isEnabled()).thenReturn(true);
		setUpConfigurationReader();
		when(resourceTypeCacheConfiguration.isEnabled()).thenReturn(false);

		//when
		testedObject.doFilter(request, response, filterChain);

		//then
		verify(configurationReader).readComponentConfiguration(request);
		verify(resourceTypeCacheConfiguration).isEnabled();
		verify(filterChain).doFilter(request, response);
		verifyNoMoreInteractions(request, response, filterChain, configurationReader, resourceTypeCacheConfiguration);
	}

	private void setUpConfigurationReader() {
		when(configurationReader.readComponentConfiguration(request)).thenReturn(Optional.of(resourceTypeCacheConfiguration));
	}

	@Test
	public void shouldHandleWhenResourceTypeCacheConfigurationIsConfigured() throws IOException, ServletException {
		//given
		when(cacheConfiguration.isEnabled()).thenReturn(true);
		setUpConfigurationReader();
		when(resourceTypeCacheConfiguration.isEnabled()).thenReturn(true);
		when(cacheHolder.putOrGet(eq(request), eq(resourceTypeCacheConfiguration), any(MissingCacheEntryCallback.class)))
				.thenReturn(createCacheEntity());
		setUpResponse();

		//when
		testedObject.doFilter(request, response, filterChain);

		//then
		verify(response).setContentType(CONTENT_TYPE);
		verify(printWriter).write(CONTENT);
	}

	private CacheEntity createCacheEntity() throws IOException {
		return new ByteStreamEntity(CONTENT_TYPE, createByteArray());
	}

	private ByteArrayOutputStream createByteArray() throws IOException {
		ByteArrayOutputStream array = new ByteArrayOutputStream();
		array.write(CONTENT.getBytes(Charset.forName("utf-8")));
		return array;
	}

	private void setUpResponse() throws IOException {
		when(response.getWriter()).thenReturn(printWriter);
		when(response.getCharacterEncoding()).thenReturn("utf-8");
	}
}
