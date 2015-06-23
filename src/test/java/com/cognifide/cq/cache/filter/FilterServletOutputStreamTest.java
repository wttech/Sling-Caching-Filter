package com.cognifide.cq.cache.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Bartosz Rudnicki
 */
public class FilterServletOutputStreamTest {

	private ByteArrayOutputStream byteArrayOutputStream;

	private FilterServletOutputStream testedObject;

	@Before
	public void setUp() {
		byteArrayOutputStream = new ByteArrayOutputStream();
		testedObject = new FilterServletOutputStream(byteArrayOutputStream);
	}

	private String closeStreamAndReadConent() throws IOException {
		testedObject.close();
		return new String(byteArrayOutputStream.toByteArray());
	}

	@Test
	public void testWriteInt() throws IOException {
		//when
		testedObject.write('a');
		testedObject.write('b');
		testedObject.write('c');
		String actual = closeStreamAndReadConent();

		//then
		assertThat(actual, is("abc"));
	}

	@Test
	public void testWriteByteArray() throws IOException {
		//when
		testedObject.write("abc".getBytes());
		String actual = closeStreamAndReadConent();

		//then
		assertThat(actual, is("abc"));
	}

	@Test
	public void testWriteByteArrayWithParams() throws IOException {
		//when
		testedObject.write("--abc--".getBytes(), 2, 3);
		String actual = closeStreamAndReadConent();

		//then
		assertThat(actual, is("abc"));
	}

}
