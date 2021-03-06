package com.cognifide.cq.cache.model;

import com.cognifide.cq.cache.definition.CacheConfigurationEntryImpl;
import com.cognifide.cq.cache.definition.ResourceTypeCacheDefinition;
import com.cognifide.cq.cache.filter.osgi.CacheConfiguration;
import com.cognifide.cq.cache.model.alias.PathAliasStore;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.commons.osgi.OsgiUtil;

/**
 * @author Bartosz Rudnicki
 */
public class ResourceTypeCacheConfiguration extends CacheConfigurationEntryImpl {

	private final boolean enabled;

	private final ResourceTypeCacheDefinition resourceTypeCacheDefinition;

	private final PathAliasStore pathAliasStore;

	private final Set<String> invalidationPathPrefixes;

	private final Set<Pattern> invalidationPatterns;

	public ResourceTypeCacheConfiguration(ResourceTypeCacheDefinition resourceTypeCacheDefinition,
			CacheConfiguration cacheConfiguration, PathAliasStore pathAliasStore) {
		super(resourceTypeCacheDefinition.getResourceType(),
				OsgiUtil.toInteger(resourceTypeCacheDefinition.getValidityTimeInSeconds(), cacheConfiguration.getValidityTimeInSeconds()),
				resourceTypeCacheDefinition.getCacheLevel());

		this.enabled = resourceTypeCacheDefinition.isEnabled();
		this.resourceTypeCacheDefinition = resourceTypeCacheDefinition;
		this.pathAliasStore = pathAliasStore;

		this.invalidationPathPrefixes = new HashSet<String>();
		this.invalidationPatterns = new HashSet<Pattern>();
	}

	public boolean isEnabled() {
		return enabled;
	}

	public Iterable<String> getInvalidationPathPrefixes() {
		return Collections.unmodifiableSet(invalidationPathPrefixes);
	}

	public Iterable<Pattern> getInvalidationPatterns() {
		return Collections.unmodifiableSet(invalidationPatterns);
	}

	public void generateInvalidationPathsFor(SlingHttpServletRequest request) {
		new DefinitionPathTranslator(pathAliasStore, resourceTypeCacheDefinition, this, request.getResource())
				.translatePaths();
	}

	void addInvalidationPathPrefix(String invalidationPathPrefix) {
		if (StringUtils.isNotEmpty(invalidationPathPrefix)) {
			this.invalidationPathPrefixes.add(invalidationPathPrefix);
		}
	}

	void addInvalidationPattern(Pattern pattern) {
		this.invalidationPatterns.add(pattern);
	}

	void addInvalidationPatterns(Iterable<Pattern> patterns) {
		invalidationPatterns.addAll(invalidationPatterns);
	}
}
