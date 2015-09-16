package com.cognifide.cq.cache.definition.osgi;

import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.osgi.service.component.ComponentContext;

public class OsgiConfigurationHelper {

	private static final String[] EMPTY_ARRAY = new String[0];

	private OsgiConfigurationHelper() {
		throw new AssertionError();
	}

	public static String getStringValueFrom(String propertyName, ComponentContext componentContext) {
		validate(propertyName, componentContext);
		return OsgiUtil.toString(getProperty(propertyName, componentContext), StringUtils.EMPTY);
	}

	private static void validate(String propertyName, ComponentContext componentContext) {
		Preconditions.checkNotNull(propertyName);
		Preconditions.checkNotNull(componentContext);
	}

	public static String[] getStringArrayValuesFrom(String propertyName, ComponentContext componentContext) {
		validate(propertyName, componentContext);
		return OsgiUtil.toStringArray(getProperty(propertyName, componentContext), EMPTY_ARRAY);
	}

	public static boolean getBooleanValueFrom(String propertyName, ComponentContext componentContext) {
		validate(propertyName, componentContext);
		return OsgiUtil.toBoolean(getProperty(propertyName, componentContext), false);
	}

	public static int getIntegerValueFrom(String propertyName, ComponentContext componentContext) {
		return getIntegerValueFrom(propertyName, componentContext, 0);
	}

	public static int getIntegerValueFrom(String propertyName, ComponentContext componentContext, int defaultValue) {
		validate(propertyName, componentContext);
		return OsgiUtil.toInteger(getProperty(propertyName, componentContext), defaultValue);
	}

	private static Object getProperty(String propertyName, ComponentContext componentContext) {
		validate(propertyName, componentContext);
		return componentContext.getProperties().get(propertyName);
	}
}
