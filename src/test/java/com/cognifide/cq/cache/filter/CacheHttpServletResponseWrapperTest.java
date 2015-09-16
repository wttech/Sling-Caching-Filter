package com.cognifide.cq.cache.filter;

import java.io.IOException;
import java.io.Writer;
import javax.servlet.http.HttpServletResponse;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class CacheHttpServletResponseWrapperTest {

	@Mock
	private HttpServletResponse response;

	private CacheHttpServletResponseWrapper testedObject;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@Before
	public void setUp() {
		testedObject = new CacheHttpServletResponseWrapper(response);
	}

	@Test
	public void shouldThrowNullPointerExceptionWhenResponseIsNull() {
		exception.expect(NullPointerException.class);
		//when
		new CacheHttpServletResponseWrapper(null);
	}

	@Test
	public void responseWriterShouldBeInitialized() throws IOException {
		//given
		when(response.getCharacterEncoding()).thenReturn("utf-8");

		//when
		Writer actual = testedObject.getWriter();

		//then
		assertThat(actual).isNotNull();
	}

	@Test
	public void responseWriterShouldBeInitializedOnce() throws IOException {
		//given
		when(response.getCharacterEncoding()).thenReturn("utf-8");

		//when
		testedObject.getWriter();
		testedObject.getWriter();

		//then
		verify(response).getCharacterEncoding();
	}

}
