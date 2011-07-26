package com.cognifide.cq.cache.test.utils;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentInstance;

/**
 * @author Bartosz Rudnicki
 */
public class ComponentContextStub extends Hashtable<Object, Object> implements ComponentContext {

	private static final long serialVersionUID = -8892821271996383559L;

	@Override
	public void disableComponent(String s) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void enableComponent(String s) {
		throw new UnsupportedOperationException();
	}

	@Override
	public BundleContext getBundleContext() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ComponentInstance getComponentInstance() {
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Dictionary getProperties() {
		return this;
	}

	@Override
	public ServiceReference getServiceReference() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Bundle getUsingBundle() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object locateService(String s) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object locateService(String s, ServiceReference servicereference) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object[] locateServices(String s) {
		throw new UnsupportedOperationException();
	}
}
