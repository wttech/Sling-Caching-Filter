package com.cognifide.cq.cache.tag;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;

/**
 * @author Bartosz Rudnicki
 */
public class PageContextStub extends PageContext {

	private ServletContext servletContext;

	private ServletRequest servletRequest;

	private JspWriter jspWriter;

	public void setJspWriter(JspWriter jspWriter) {
		this.jspWriter = jspWriter;
	}

	public void setServletRequest(ServletRequest servletRequest) {
		this.servletRequest = servletRequest;
	}

	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	@Override
	public void forward(String s) throws ServletException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Exception getException() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getPage() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ServletRequest getRequest() {
		return servletRequest;
	}

	@Override
	public ServletResponse getResponse() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ServletConfig getServletConfig() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ServletContext getServletContext() {
		return servletContext;
	}

	@Override
	public HttpSession getSession() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void handlePageException(Exception exception) throws ServletException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void handlePageException(Throwable throwable) throws ServletException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void include(String s) throws ServletException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void include(String s, boolean flag) throws ServletException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void initialize(Servlet servlet, ServletRequest servletrequest, ServletResponse servletresponse,
			String s, boolean flag, int i, boolean flag1) throws IOException, IllegalStateException,
			IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void release() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object findAttribute(String s) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getAttribute(String s) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getAttribute(String s, int i) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public Enumeration getAttributeNamesInScope(int i) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getAttributesScope(String s) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ExpressionEvaluator getExpressionEvaluator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public JspWriter getOut() {
		return jspWriter;
	}

	@Override
	public VariableResolver getVariableResolver() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeAttribute(String s) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeAttribute(String s, int i) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAttribute(String s, Object obj) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAttribute(String s, Object obj, int i) {
		throw new UnsupportedOperationException();
	}
}
