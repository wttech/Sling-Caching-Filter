/*
 * Copyright 2015 Wunderman Thompson Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cognifide.cq.cache.model;

import com.cognifide.cq.cache.definition.ResourceTypeCacheDefinition;
import com.cognifide.cq.cache.model.alias.PathAliasStore;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DefinitionPathTranslator {

	private static final Logger logger = LoggerFactory.getLogger(DefinitionPathTranslator.class);

	private static final String RESOURCE_TYPE_PATH = "/apps/%s";

	private final PathAliasStore pathAliasStore;

	private final ResourceTypeCacheDefinition definition;

	private final ResourceTypeCacheConfiguration configuration;

	private final Resource resource;

	DefinitionPathTranslator(PathAliasStore pathAliasStore, ResourceTypeCacheDefinition definition,
			ResourceTypeCacheConfiguration configuration, Resource resource) {
		this.pathAliasStore = pathAliasStore;
		this.definition = definition;
		this.configuration = configuration;
		this.resource = resource;
	}

	void translatePaths() {
		if (definition.isEnabled()) {
			addResourcePathToConfiguration();
			addContainingPageToConfiguration();
			addPathBasedOnResourceType();
			addPathsFromReferenceFields();
			addPathsReadFromResourceFields();
		}
	}

	private void addResourcePathToConfiguration() {
		if (definition.isInvalidateOnSelf()) {
			configuration.addInvalidationPathPrefix(resource.getPath());
		}
	}

	private void addContainingPageToConfiguration() {
		if (definition.isInvalidateOnContainingPage()) {
			configuration.addInvalidationPathPrefix(findPagePath());
		}
	}

	private String findPagePath() {
		Page page = resource.getResourceResolver().adaptTo(PageManager.class).getContainingPage(resource);
		return null != page ? page.getPath() : StringUtils.EMPTY;
	}

	private void addPathBasedOnResourceType() {
		Resource resourceTypeResource = getResourceTypeResource();
		if (resourceTypeResource != null) {
			configuration.addInvalidationPathPrefix(resourceTypeResource.getPath());
		}
	}

	private Resource getResourceTypeResource() {
		return resource.getResourceResolver().getResource(findAbsolutreResourceTypePathForResource());
	}

	private String findAbsolutreResourceTypePathForResource() {
		final String path = resource.getPath();
		return path.startsWith("/") ? path : String.format(RESOURCE_TYPE_PATH, path);
	}

	private void addPathsFromReferenceFields() {
		ValueMap valueMap = resource.adaptTo(ValueMap.class);
		if (valueMap != null) {
			for (String name : definition.getInvalidateOnReferencedFields()) {
				Object value = valueMap.get(name);
				if (isString(value)) {
					String stringValue = (String) value;
					if (StringUtils.isNotBlank(stringValue)) {
						configuration.addInvalidationPathPrefix(stringValue);
					}
				}
			}
		}
	}

	private boolean isString(Object value) {
		return null != value && value.getClass().isAssignableFrom(String.class);
	}

	private void addPathsReadFromResourceFields() {
		for (String path : definition.getInvalidateOnPaths()) {
			if (pathAliasStore.isAlias(path)) {
				configuration.addInvalidationPatterns(pathAliasStore.getPathsForAlias(path));
			} else {
				tryToAddPatternToConfiguration(path);
			}
		}
	}

	private void tryToAddPatternToConfiguration(String path) {
		try {
			configuration.addInvalidationPattern(Pattern.compile(path));
		} catch (PatternSyntaxException x) {
			logger.error("Pattern " + path + " is invalid.", x);
		}
	}
}
