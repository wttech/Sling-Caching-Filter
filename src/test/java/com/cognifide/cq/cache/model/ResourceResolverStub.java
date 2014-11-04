package com.cognifide.cq.cache.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

/**
 * @author Bartosz Rudnicki
 */
public class ResourceResolverStub implements ResourceResolver {

	private Map<String, Resource> resources = new HashMap<String, Resource>();

	public Map<String, Resource> getResources() {
		return resources;
	}

	@Override
	public Iterator<Resource> findResources(String query, String language) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Resource getResource(String path) {
		return resources.get(path);
	}

	/**
	 * Direct code copied from the JcrResourceResolver2.
	 */
	@Override
	public Resource getResource(Resource base, String path) {
		if (!path.startsWith("/") && (base != null)) {
			path = (new StringBuilder()).append(base.getPath()).append("/").append(path).toString();
		}
		return getResource(path);
	}

	@Override
	public String[] getSearchPath() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<Resource> listChildren(Resource parent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String map(String resourcePath) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String map(HttpServletRequest request, String resourcePath) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<Map<String, Object>> queryResources(String query, String language) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Resource resolve(String absPath) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Resource resolve(HttpServletRequest request) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Resource resolve(HttpServletRequest request, String absPath) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ResourceResolver clone(Map<String, Object> arg0) throws LoginException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getAttribute(String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<String> getAttributeNames() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getUserID() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isLive() {
		throw new UnsupportedOperationException();
	}

}
