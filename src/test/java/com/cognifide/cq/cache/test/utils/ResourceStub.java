package com.cognifide.cq.cache.test.utils;

import java.util.HashMap;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

/**
 * @author Bartosz Rudnicki
 */
public class ResourceStub extends HashMap<String, Object> implements Resource, ValueMap {

	private static final long serialVersionUID = -3914573179309723254L;

	private String path;

	private String resourceType;

	private String resourceSuperType;

	public void setPath(String path) {
		this.path = path;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public void setResourceSuperType(String resourceSuperType) {
		this.resourceSuperType = resourceSuperType;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public ResourceMetadata getResourceMetadata() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ResourceResolver getResourceResolver() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getResourceSuperType() {
		return resourceSuperType;
	}

	@Override
	public String getResourceType() {
		return resourceType;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
		if (type.equals(ValueMap.class)) {
			return (AdapterType) this;
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(String name, Class<T> type) {
		try {
			return (T) get(name);
		} catch (ClassCastException cce) {
			return null;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(String name, T defaultValue) {
		if (containsKey(name)) {
			try {
				return (T) get(name);
			} catch (ClassCastException castException) {
			}
		}
		return defaultValue;
	}
}
