package com.cognifide.cq.cache.definition.osgi;

import com.cognifide.cq.cache.definition.ResourceTypeCacheDefinition;
import java.util.Arrays;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.PropertyUnbounded;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

@Component(configurationFactory = true, metatype = true, immediate = true, label = "Sling Caching Filter Resource Type Definition")
@Service
public class OsgiResourceTypeCacheDefinition implements ResourceTypeCacheDefinition {

	private final static Log log = LogFactory.getLog(OsgiResourceTypeCacheDefinition.class);

	private static final boolean ENABLED_PROPERTY_DEFAULT_VALUE = false;

	private static final String CACHE_LEVEL_PROPERTY_DEFAULT_VALUE = "-1";

	private static final int INT_CACHE_LEVEL_PROPERTY_DEFAULT_VALUE = Integer.parseInt(CACHE_LEVEL_PROPERTY_DEFAULT_VALUE);

	private static final boolean INVALIDATE_ON_SELF_PROPERTY_DEFAULT_VALUE = true;

	@Property(
			label = "Active",
			description = "Activates/deactivates caching of given component",
			boolValue = ENABLED_PROPERTY_DEFAULT_VALUE)
	private static final String ACTIVE_PROPERTY = "cache.config.active";

	@Property(
			label = "Resource type",
			description = "Component resource type")
	private static final String RESOURCE_TYPE_PROPERTY = "cache.config.resource.type";

	@Property(
			label = "Validity time",
			description = "Specifies cache entry validity time (in seconds)")
	private static final String VALIDITY_TIME_PROPERTY = "cache.config.validity.time";

	@Property(
			label = "Cache level",
			description = "Specifies the level of component caching",
			value = CACHE_LEVEL_PROPERTY_DEFAULT_VALUE)
	private static final String CACHE_LEVEL_PROPERTY = "cache.config.cache.level";

	@Property(
			label = "Invalidate on self",
			description = "When set to true cached instance will be refreshed if it has been changed",
			boolValue = INVALIDATE_ON_SELF_PROPERTY_DEFAULT_VALUE)
	private static final String INVALIDATE_ON_SELF_PROPERTY = "cache.config.invalidate.on.self";

	@Property(
			label = "Invalidate on referenced fields",
			description = "List of component fields that store links to content/configuration/etc. pages. Links from those fields are loaded and each content change inside nodes pointed to by those links will invalidate cache of the current component",
			unbounded = PropertyUnbounded.ARRAY)
	private static final String INVALIDATE_ON_REFERENCED_FIELDS_PROPERTY = "cache.config.invalidate.on.referenced.fields";

	@Property(
			label = "Invalide on paths",
			description = "If a path of any changed JCR node mathes any path from the list then the cache of the current component is invalidated",
			unbounded = PropertyUnbounded.ARRAY)
	private static final String INVALIDATE_ON_PATHS_PROPERTY = "cache.config.invalidate.on.paths";

	private Boolean active;

	private String resourceType;

	private Integer validityTime;

	private String cacheLevel;

	private Boolean invalidateOnSelf;

	private String[] invalidateOnReferencedFields;

	private String[] invalidateOnPaths;

	@Activate
	public void activate(ComponentContext componentContext) {
		active = OsgiConfigurationHelper.getBooleanValueFrom(ACTIVE_PROPERTY, componentContext);
		resourceType = OsgiConfigurationHelper.getStringValueFrom(RESOURCE_TYPE_PROPERTY, componentContext);
		validityTime = OsgiConfigurationHelper.getIntegerValueFrom(VALIDITY_TIME_PROPERTY, componentContext);
		cacheLevel = OsgiConfigurationHelper.getStringValueFrom(CACHE_LEVEL_PROPERTY, componentContext);
		invalidateOnSelf = OsgiConfigurationHelper.getBooleanValueFrom(INVALIDATE_ON_SELF_PROPERTY, componentContext);
		invalidateOnReferencedFields = OsgiConfigurationHelper.getStringArrayValuesFrom(INVALIDATE_ON_REFERENCED_FIELDS_PROPERTY, componentContext);
		invalidateOnPaths = OsgiConfigurationHelper.getStringArrayValuesFrom(INVALIDATE_ON_PATHS_PROPERTY, componentContext);
	}

	@Override
	public Boolean isEnabled() {
		return active;
	}

	@Override
	public String getResourceType() {
		return resourceType;
	}

	@Override
	public Integer getValidityTimeInSeconds() {
		return validityTime;
	}

	@Override
	public Integer getCacheLevel() {
		Integer result = INT_CACHE_LEVEL_PROPERTY_DEFAULT_VALUE;
		try {
			result = Integer.parseInt(cacheLevel);
		} catch (NumberFormatException x) {
			log.error("Error while converting cache level to integer", x);
		}
		return result;
	}

	@Override
	public Boolean isInvalidateOnSelf() {
		return invalidateOnSelf;
	}

	@Override
	public String[] getInvalidateOnReferencedFields() {
		return Arrays.copyOf(invalidateOnReferencedFields, invalidateOnReferencedFields.length);
	}

	@Override
	public String[] getInvalidateOnPaths() {
		return Arrays.copyOf(invalidateOnPaths, invalidateOnPaths.length);
	}

}
