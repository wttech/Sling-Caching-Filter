package com.cognifide.cq.cache.filter;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;

/**
 * @author Bartosz Mordaka
 */
public class FilterServletOutputStream extends ServletOutputStream {

	OutputStream outputStream;

	public FilterServletOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	@Override
	public void write(int value) throws IOException {
		this.outputStream.write(value);
	}

	@Override
	public void write(byte[] value) throws IOException {
		this.outputStream.write(value);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		this.outputStream.write(b, off, len);
	}
}