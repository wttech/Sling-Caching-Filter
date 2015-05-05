package com.cognifide.cq.cache.definition.osgi;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.osgi.service.component.ComponentContext;

public class OsgiConfigurationHelper {

	private static final String[] EMPTY_ARRAY = new String[0];

	private OsgiConfigurationHelper() {
		throw new AssertionError();
	}

	public static String getStringValueFrom(String propertyName, ComponentContext componentContext) {
		return OsgiUtil.toString(getProperty(propertyName, componentContext), StringUtils.EMPTY);
	}

	public static String[] getStringArrayValuesFrom(String propertyName, ComponentContext componentContext) {
		return OsgiUtil.toStringArray(getProperty(propertyName, componentContext), EMPTY_ARRAY);
	}

	public static Boolean getBooleanValueFrom(String propertyName, ComponentContext componentContext) {
		Object object = componentContext.getProperties().get(propertyName);
		return null == object ? null : OsgiUtil.toBoolean(object, false);
	}

	public static Integer getIntegerValueFrom(String propertyName, ComponentContext componentContext) {
		Object object = componentContext.getProperties().get(propertyName);
		return null == object ? null : OsgiUtil.toInteger(object, 0);
	}

	private static Object getProperty(String propertyName, ComponentContext componentContext) {
		return componentContext.getProperties().get(propertyName);
	}
}
