package com.cognifide.cq.cache.test.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.request.RequestParameterMap;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.request.RequestProgressTracker;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

/**
 * @author Bartosz Rudnicki
 */
public class SlingHttpServletRequestStub implements SlingHttpServletRequest {

	private ResourceResolver resourceResolver;

	private Resource resource;

	private String selectorString;

	public String getSelectorString() {
		return selectorString;
	}

	public void setSelectorString(String selectorString) {
		this.selectorString = selectorString;
	}

	public void setResourceResolver(ResourceResolver resourceResolver) {
		this.resourceResolver = resourceResolver;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	@Override
	public Cookie getCookie(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public RequestDispatcher getRequestDispatcher(Resource resource) {
		throw new UnsupportedOperationException();
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path, RequestDispatcherOptions options) {
		throw new UnsupportedOperationException();
	}

	@Override
	public RequestDispatcher getRequestDispatcher(Resource resource, RequestDispatcherOptions options) {
		throw new UnsupportedOperationException();
	}

	@Override
	public RequestParameter getRequestParameter(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public RequestParameterMap getRequestParameterMap() {
		throw new UnsupportedOperationException();
	}

	@Override
	public RequestParameter[] getRequestParameters(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public RequestPathInfo getRequestPathInfo() {
		return new RequestPathInfo() {

			@Override
			public String getSuffix() {
				throw new UnsupportedOperationException();
			}

			@Override
			public String[] getSelectors() {
				throw new UnsupportedOperationException();
			}

			@Override
			public String getSelectorString() {
				return selectorString;
			}

			@Override
			public String getResourcePath() {
				throw new UnsupportedOperationException();
			}

			@Override
			public String getExtension() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public RequestProgressTracker getRequestProgressTracker() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Resource getResource() {
		return resource;
	}

	@Override
	public ResourceBundle getResourceBundle(Locale locale) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ResourceBundle getResourceBundle(String baseName, Locale locale) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ResourceResolver getResourceResolver() {
		return resourceResolver;
	}

	@Override
	public String getResponseContentType() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Enumeration<String> getResponseContentTypes() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getAuthType() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getContextPath() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Cookie[] getCookies() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getDateHeader(String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getHeader(String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Enumeration getHeaderNames() {
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Enumeration getHeaders(String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getIntHeader(String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getMethod() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getPathInfo() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getPathTranslated() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getQueryString() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getRemoteUser() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getRequestURI() {
		throw new UnsupportedOperationException();
	}

	@Override
	public StringBuffer getRequestURL() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getRequestedSessionId() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getServletPath() {
		throw new UnsupportedOperationException();
	}

	@Override
	public HttpSession getSession() {
		throw new UnsupportedOperationException();
	}

	@Override
	public HttpSession getSession(boolean arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Principal getUserPrincipal() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isUserInRole(String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getAttribute(String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Enumeration getAttributeNames() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getCharacterEncoding() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getContentLength() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getContentType() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getLocalAddr() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getLocalName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getLocalPort() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Locale getLocale() {
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Enumeration getLocales() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getParameter(String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Map getParameterMap() {
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Enumeration getParameterNames() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String[] getParameterValues(String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getProtocol() {
		throw new UnsupportedOperationException();
	}

	@Override
	public BufferedReader getReader() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getRealPath(String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getRemoteAddr() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getRemoteHost() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getRemotePort() {
		throw new UnsupportedOperationException();
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getScheme() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getServerName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getServerPort() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSecure() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeAttribute(String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAttribute(String arg0, Object arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
		throw new UnsupportedOperationException();
	}

	@Override
	public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
		throw new UnsupportedOperationException();
	}

}
