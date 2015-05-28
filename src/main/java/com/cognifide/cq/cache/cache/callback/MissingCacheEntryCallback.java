package com.cognifide.cq.cache.cache.callback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.servlet.ServletException;

public interface MissingCacheEntryCallback {

	ByteArrayOutputStream doCallback() throws IOException, ServletException;

}
