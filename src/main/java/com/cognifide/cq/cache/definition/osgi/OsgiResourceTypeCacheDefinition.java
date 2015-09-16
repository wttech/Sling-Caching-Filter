package com.cognifide.cq.cache.definition.osgi;

import com.cognifide.cq.cache.definition.ResourceTypeCacheDefinition;
import com.cognifide.cq.cache.filter.osgi.CacheConfiguration;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import java.util.Arrays;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.PropertyUnbounded;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

@Component(
		configurationFactory = true,
		metatype = true,
		label = "Sling Caching Filter Resource Type Definition")
@Service
public class OsgiResourceTypeCacheDefinition implements ResourceTypeCacheDefinition {

	private static final boolean ENABLED_PROPERTY_DEFAULT_VALUE = false;

	private static final String CACHE_LEVEL_PROPERTY_DEFAULT_VALUE = "-1";

	private static final int INT_CACHE_LEVEL_PROPERTY_DEFAULT_VALUE = Integer.parseInt(CACHE_LEVEL_PROPERTY_DEFAULT_VALUE);

	private static final boolean INVALIDATE_ON_SELF_PROPERTY_DEFAULT_VALUE = true;

	private static final boolean INVALIDATE_ON_CONTAINING_PAGE_PROPERTY_DEFAULT_VALUE = false;

	private static final Predicate<String> NOT_BLANK_PREDICATE = new NotBlankPredicate();

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
			label = "Invalidate on containing page",
			description = "When set to true cached instance will be refreshed if page containing this instance has been changed",
			boolValue = INVALIDATE_ON_CONTAINING_PAGE_PROPERTY_DEFAULT_VALUE)
	private static final String INVALIDATE_ON_CONTAINING_PAGE_PROPERTY = "cache.config.invalidate.on.containing.page";

	@Property(
			label = "Invalidate on referenced fields",
			description = "List of component fields that store links to content/configuration/etc. pages. Links from those fields are loaded and each content change inside nodes pointed to by those links will invalidate cache of the current component",
			unbounded = PropertyUnbounded.ARRAY)
	private static final String INVALIDATE_ON_REFERENCED_FIELDS_PROPERTY = "cache.config.invalidate.on.referenced.fields";

	@Property(
			label = "Invalide on paths (Regex)",
			description = "If a path of any changed JCR node mathes any path from the list then the cache of the current component is invalidated",
			unbounded = PropertyUnbounded.ARRAY)
	private static final String INVALIDATE_ON_PATHS_PROPERTY = "cache.config.invalidate.on.paths";

	@Reference
	private CacheConfiguration cacheConfiguration;

	private boolean active;

	private String resourceType;

	private int validityTime;

	private int cacheLevel;

	private boolean invalidateOnSelf;

	private boolean invalidateOnContainingPage;

	private Iterable<String> invalidateOnReferencedFields;

	private Iterable<String> invalidateOnPaths;

	@Activate
	@Modified
	protected void activate(ComponentContext componentContext) {
		active = OsgiConfigurationHelper.getBooleanValueFrom(ACTIVE_PROPERTY, componentContext);
		resourceType = OsgiConfigurationHelper.getStringValueFrom(RESOURCE_TYPE_PROPERTY, componentContext);
		validityTime = OsgiConfigurationHelper.getIntegerValueFrom(VALIDITY_TIME_PROPERTY, componentContext);
		cacheLevel = OsgiConfigurationHelper.getIntegerValueFrom(CACHE_LEVEL_PROPERTY, componentContext, INT_CACHE_LEVEL_PROPERTY_DEFAULT_VALUE);
		invalidateOnSelf = OsgiConfigurationHelper.getBooleanValueFrom(INVALIDATE_ON_SELF_PROPERTY, componentContext);
		invalidateOnContainingPage = OsgiConfigurationHelper.getBooleanValueFrom(INVALIDATE_ON_CONTAINING_PAGE_PROPERTY, componentContext);
		invalidateOnReferencedFields = transformAndClean(OsgiConfigurationHelper.getStringArrayValuesFrom(INVALIDATE_ON_REFERENCED_FIELDS_PROPERTY, componentContext));
		invalidateOnPaths = transformAndClean(OsgiConfigurationHelper.getStringArrayValuesFrom(INVALIDATE_ON_PATHS_PROPERTY, componentContext));
	}

	private Iterable<String> transformAndClean(String[] array) {
		return Iterables.filter(Arrays.asList(array), NOT_BLANK_PREDICATE);
	}

	@Override
	public boolean isEnabled() {
		return active;
	}

	@Override
	public String getResourceType() {
		return resourceType;
	}

	@Override
	public int getValidityTimeInSeconds() {
		return 0 < validityTime ? validityTime : cacheConfiguration.getValidityTimeInSeconds();
	}

	@Override
	public int getCacheLevel() {
		return cacheLevel;
	}

	@Override
	public boolean isInvalidateOnSelf() {
		return invalidateOnSelf;
	}

	@Override
	public boolean isInvalidateOnContainingPage() {
		return invalidateOnContainingPage;
	}

	@Override
	public Iterable<String> getInvalidateOnReferencedFields() {
		return invalidateOnReferencedFields;
	}

	@Override
	public Iterable<String> getInvalidateOnPaths() {
		return invalidateOnPaths;
	}

	@Override
	public boolean isValid() {
		return StringUtils.isNotEmpty(resourceType);
	}

}
