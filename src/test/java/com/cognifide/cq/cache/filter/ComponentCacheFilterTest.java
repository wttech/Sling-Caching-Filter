package com.cognifide.cq.cache.filter;

import com.cognifide.cq.cache.cache.CacheHolder;
import com.cognifide.cq.cache.cache.callback.MissingCacheEntryCallback;
import com.cognifide.cq.cache.filter.osgi.CacheConfiguration;
import com.cognifide.cq.cache.model.ResourceTypeCacheConfiguration;
import com.cognifide.cq.cache.model.reader.ResourceTypeCacheConfigurationReader;
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

	@Mock
	private SlingHttpServletRequest request;

	@Mock
	private ServletRequest otherRequest;

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
		testedObject.doFilter(otherRequest, response, filterChain);

		//then
		verify(filterChain).doFilter(otherRequest, response);
		verifyNoMoreInteractions(otherRequest, response, filterChain);
	}

	@Test
	public void shouldNotHandleWhenRequestIsNotSlingRequest() throws IOException, ServletException {
		//given
		when(cacheConfiguration.isEnabled()).thenReturn(true);

		//when
		testedObject.doFilter(otherRequest, response, filterChain);

		//then
		verify(filterChain).doFilter(otherRequest, response);
		verifyNoMoreInteractions(otherRequest, response, filterChain);
	}

	@Test
	public void shouldNotHandleWhenConfigurationReaderDoNotHaveConfigurationForGivenResource() throws IOException, ServletException {
		//given
		when(cacheConfiguration.isEnabled()).thenReturn(true);
		when(configurationReader.hasConfigurationFor(request)).thenReturn(false);

		//when
		testedObject.doFilter(request, response, filterChain);

		//then
		verify(configurationReader).hasConfigurationFor(request);
		verify(filterChain).doFilter(request, response);
		verifyNoMoreInteractions(request, response, filterChain, configurationReader);
	}

	@Test
	public void shouldNotHandleWhenConfigurationReaderAnswersWithNullConfiguration() throws IOException, ServletException {
		//given
		when(cacheConfiguration.isEnabled()).thenReturn(true);
		when(configurationReader.hasConfigurationFor(request)).thenReturn(true);
		when(configurationReader.readComponentConfiguration(request)).thenReturn(null);

		//when
		testedObject.doFilter(request, response, filterChain);

		//then
		verify(configurationReader).hasConfigurationFor(request);
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
		verify(resourceTypeCacheConfiguration).isEnabled();
		verify(filterChain).doFilter(request, response);
		verifyNoMoreInteractions(request, response, filterChain, resourceTypeCacheConfiguration);
	}

	private void setUpConfigurationReader() {
		when(configurationReader.hasConfigurationFor(request)).thenReturn(true);
		when(configurationReader.readComponentConfiguration(request)).thenReturn(resourceTypeCacheConfiguration);
	}

	@Test
	public void shouldHandleWhenResourceTypeCacheConfigurationIsConfigured() throws IOException, ServletException {
		//given
		when(cacheConfiguration.isEnabled()).thenReturn(true);
		setUpConfigurationReader();
		when(resourceTypeCacheConfiguration.isEnabled()).thenReturn(true);
		when(cacheHolder.putOrGet(eq(request), eq(resourceTypeCacheConfiguration), any(MissingCacheEntryCallback.class)))
				.thenReturn(createByteArray());
		setUpResponse();

		//when
		testedObject.doFilter(request, response, filterChain);

		//then
		verify(printWriter).write(CONTENT);
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
