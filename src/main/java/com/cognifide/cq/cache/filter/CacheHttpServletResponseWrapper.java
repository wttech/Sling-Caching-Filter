package com.cognifide.cq.cache.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * @author Bartosz Mordaka
 */
public class CacheHttpServletResponseWrapper extends HttpServletResponseWrapper {

	private static final int BUFFER_SIZE = 1024;

	private final ByteArrayOutputStream baos;

	private final ServletOutputStream outstr;

	private PrintWriter writer;

	public CacheHttpServletResponseWrapper(final HttpServletResponse response) {
		super(response);
		this.baos = new ByteArrayOutputStream(BUFFER_SIZE);
		this.outstr = new FilterServletOutputStream(baos);
	}

	/**
	 * Gets the outputstream.
	 *
	 * @return
	 */
	@Override
	public ServletOutputStream getOutputStream() {
		return outstr;
	}

	/**
	 * Gets the print writer.
	 *
	 * @return
	 * @throws java.io.IOException
	 */
	@Override
	public PrintWriter getWriter() throws IOException {
		if (writer == null) {
			writer = new PrintWriter(new OutputStreamWriter(outstr, getCharacterEncoding()), true);
		}
		return writer;
	}

	public ByteArrayOutputStream getContent() {
		return baos;
	}
}
