package com.cognifide.cq.cache.filter;

import com.google.common.base.Preconditions;
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

	private final ServletOutputStream stream;

	private PrintWriter writer;

	public CacheHttpServletResponseWrapper(final HttpServletResponse response) {
		super(Preconditions.checkNotNull(response));
		this.baos = new ByteArrayOutputStream(BUFFER_SIZE);
		this.stream = new FilterServletOutputStream(baos);
	}

	@Override
	public ServletOutputStream getOutputStream() {
		return stream;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		if (writer == null) {
			writer = new PrintWriter(new OutputStreamWriter(stream, getCharacterEncoding()), true);
		}
		return writer;
	}

	public ByteArrayOutputStream getContent() {
		return baos;
	}
}
