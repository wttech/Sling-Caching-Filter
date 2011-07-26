package com.cognifide.cq.cache.filter;

import static junit.framework.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Bartosz Rudnicki
 */
public class FilterServletOutputStreamTest {

	private FilterServletOutputStream stream;

	private ByteArrayOutputStream byteArrayOutputStream;

	@Before
	public void setUp() {
		byteArrayOutputStream = new ByteArrayOutputStream();
		stream = new FilterServletOutputStream(byteArrayOutputStream);
	}

	private String closeStreamAndReadConent() throws IOException {
		stream.close();
		return new String(byteArrayOutputStream.toByteArray());
	}

	@Test
	public void testWriteInt() throws IOException {
		stream.write('a');
		stream.write('b');
		stream.write('c');

		String content = closeStreamAndReadConent();
		assertEquals("abc", content);
	}

	@Test
	public void testWriteByteArray() throws IOException {
		stream.write("abc".getBytes());
		String content = closeStreamAndReadConent();
		assertEquals("abc", content);
	}

	@Test
	public void testWriteByteArrayWithParams() throws IOException {
		stream.write("--abc--".getBytes(), 2, 3);
		String content = closeStreamAndReadConent();
		assertEquals("abc", content);
	}

}
