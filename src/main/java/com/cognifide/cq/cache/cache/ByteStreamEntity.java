package com.cognifide.cq.cache.cache;

import com.google.common.base.Preconditions;
import java.io.ByteArrayOutputStream;

public class ByteStreamEntity implements CacheEntity {

	private final String contentType;

	private final ByteArrayOutputStream stream;

	public ByteStreamEntity(String contentType, ByteArrayOutputStream baos) {
		this.contentType = Preconditions.checkNotNull(contentType);
		this.stream = Preconditions.checkNotNull(baos);
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public ByteArrayOutputStream getContent() {
		return stream;
	}

	@Override
	public long sizeInBytes() {
		return stream.size();
	}

}
