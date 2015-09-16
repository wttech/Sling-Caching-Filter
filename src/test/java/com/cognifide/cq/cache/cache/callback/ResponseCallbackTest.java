package com.cognifide.cq.cache.cache.callback;

import com.cognifide.cq.cache.cache.CacheEntity;
import com.cognifide.cq.cache.filter.CacheHttpServletResponseWrapper;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.apache.sling.api.SlingHttpServletRequest;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class ResponseCallbackTest {

	@Mock
	private FilterChain filterChain;

	@Mock
	private SlingHttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@Test
	public void shouldThrowNullPointerExceptionWhenFilterChainIsNull() {
		exception.expect(NullPointerException.class);

		//when
		new ResponseCallback(null, request, response);
	}

	@Test
	public void shouldThrowNullPointerExceptionWhenRequestIsNull() {
		exception.expect(NullPointerException.class);

		//when
		new ResponseCallback(filterChain, null, response);
	}

	@Test
	public void shouldThrowNullPointerExceptionWhenResponseIsNull() {
		exception.expect(NullPointerException.class);

		//when
		new ResponseCallback(filterChain, request, null);
	}

	@Test
	public void shouldGenreateCacheEntityFromValidParameters() throws IOException, ServletException {
		//given
		when(response.getCharacterEncoding()).thenReturn("utf-8");
		when(response.getContentType()).thenReturn("text/html");
		MissingCacheEntryCallback callback = new ResponseCallback(filterChain, request, response);

		//when
		CacheEntity cacheEntity = callback.generateCacheEntity();

		//then
		assertThat(cacheEntity).isNotNull();
		verify(filterChain).doFilter(eq(request), any(CacheHttpServletResponseWrapper.class));
	}

}
