package com.cognifide.cq.cache.filter;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Bartosz Rudnicki
 */
public class CacheHttpServletResponseWrapperTest {

	private static final String ENCODING = "UTF-8";

	private CacheHttpServletResponseWrapper wrapper;

	private HttpServletResponse responseMock;

	@Before
	public void setUp() {
		responseMock = createMock(HttpServletResponse.class);
		wrapper = new CacheHttpServletResponseWrapper(responseMock);
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testGetters() throws IOException {
		expect(responseMock.getCharacterEncoding()).andReturn(ENCODING);
		replay(responseMock);
		assertNotNull(wrapper.getOutputStream());
		assertNotNull(wrapper.getWriter());
		verify(responseMock);
	}

	@Test
	public void testGetContent() throws IOException {
		expect(responseMock.getCharacterEncoding()).andReturn(ENCODING);
		replay(responseMock);

		wrapper.getOutputStream().write("one".getBytes());
		wrapper.getWriter().write("two");
		wrapper.getWriter().flush();
		wrapper.getOutputStream().write("three".getBytes());
		wrapper.getWriter().write("four");
		wrapper.getWriter().flush();

		ByteArrayOutputStream contentStream = wrapper.getContent();
		assertNotNull(contentStream);

		String content = contentStream.toString();
		assertEquals("onetwothreefour", content);

		verify(responseMock);
	}

}
