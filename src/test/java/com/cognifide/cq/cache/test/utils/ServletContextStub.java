package com.cognifide.cq.cache.test.utils;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * @author Bartosz Rudnicki
 */
public class ServletContextStub implements ServletContext {

	public static final String CACHE_ADMIN_KEY = "__oscache_cache_admin";

	public static final String CACHE_ADMINS_LIST_KEY = "__oscache_admins";

	private final Map<String, Object> attributes = new HashMap<String, Object>();

	@Override
	public Object getAttribute(String key) {
		return attributes.get(key);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Enumeration getAttributeNames() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ServletContext getContext(String s) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getInitParameter(String s) {
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Enumeration getInitParameterNames() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getMajorVersion() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getMimeType(String s) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getMinorVersion() {
		throw new UnsupportedOperationException();
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String s) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getRealPath(String s) {
		throw new UnsupportedOperationException();
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String s) {
		throw new UnsupportedOperationException();
	}

	@Override
	public URL getResource(String s) throws MalformedURLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public InputStream getResourceAsStream(String s) {
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Set getResourcePaths(String s) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getServerInfo() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Servlet getServlet(String s) throws ServletException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getServletContextName() {
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Enumeration getServletNames() {
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Enumeration getServlets() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void log(String s) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void log(Exception exception, String s) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void log(String s, Throwable throwable) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeAttribute(String s) {
		attributes.remove(s);
	}

	@Override
	public void setAttribute(String key, Object value) {
		attributes.put(key, value);
	}

}
