package com.cognifide.cq.cache.filter;

import com.cognifide.cq.cache.definition.ResourceTypeCacheDefinition;
import com.cognifide.cq.cache.model.reader.ResourceTypeCacheConfigurationReaderImpl;
import com.cognifide.cq.cache.test.utils.ReflectionHelper;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;

/**
 * @author Bartosz Rudnicki
 */
public class ComponentCacheFilterTestWrapper extends ComponentCacheFilter {

	private final static Log log = LogFactory.getLog(ComponentCacheFilterTestWrapper.class);

	private static final String CONFIGURATION_READER_FIELD_NAME = "configurationReader";

	private static final String BIND_RESOURCE_TYPE_CACHE_DEFINITION_METHOD_NAME = "bindResourceTypeCacheDefinition";

	private final ResourceTypeCacheConfigurationReaderImpl reader;

	public ComponentCacheFilterTestWrapper() {
		super();

		reader = new ResourceTypeCacheConfigurationReaderImpl();

		setInternalState();
	}

	private void setInternalState() {
		try {
			ReflectionHelper.set(ComponentCacheFilter.class, CONFIGURATION_READER_FIELD_NAME, this, reader);
		} catch (NoSuchFieldException x) {
			log.error("Error while instantiating class ComponentCacheFilterTestWrapper", x);
		} catch (SecurityException x) {
			log.error("Error while instantiating class ComponentCacheFilterTestWrapper", x);
		} catch (IllegalArgumentException x) {
			log.error("Error while instantiating class ComponentCacheFilterTestWrapper", x);
		} catch (IllegalAccessException x) {
			log.error("Error while instantiating class ComponentCacheFilterTestWrapper", x);
		}
	}

	@Override
	public void activate(ComponentContext context) {
		super.activate(context);
	}

	@Override
	public void deactivate(ComponentContext context) {
		super.deactivate(context);
	}

	public void bindResourceTypeCacheDefinition(ResourceTypeCacheDefinition resourceTypeCacheDefinition) {
		try {
			ReflectionHelper.invoke(ResourceTypeCacheConfigurationReaderImpl.class, BIND_RESOURCE_TYPE_CACHE_DEFINITION_METHOD_NAME,
					new Class<?>[]{ResourceTypeCacheDefinition.class},
					reader,
					new Object[]{resourceTypeCacheDefinition});
		} catch (IllegalAccessException x) {
			log.error("Error while binding ResourceTypeCacheDefinition", x);
		} catch (InvocationTargetException x) {
			log.error("Error while binding ResourceTypeCacheDefinition", x);
		} catch (NoSuchMethodException x) {
			log.error("Error while binding ResourceTypeCacheDefinition", x);
		}
	}

}
