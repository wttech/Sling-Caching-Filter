package com.cognifide.cq.cache.model;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;

/**
 * 
 * @author Jakub Malecki
 */
public class CacheKeyGeneratorImpl implements CacheKeyGenerator {

	private static final String RESOURCE_TYPE_PATH = "/apps/%s";

	@Override
	public String generateKey(int cacheLevel, Resource resource, String selectorString) {
		if (cacheLevel < 0) {
			// caching only this one instance
			return resource.getPath() + getSelectorStringKeyPart(selectorString);
		} else if (cacheLevel == 0) {
			// site-wide
			return getAbsoluteTypePath(resource.getResourceType()) + getSelectorStringKeyPart(selectorString);
		} else {
			// path defines the scope of caching
			return getAbsoluteTypePath(resource.getResourceType())
					+ getCutPath(resource.getPath(), cacheLevel) + getSelectorStringKeyPart(selectorString);
		}
	}

	@Override
	public String generateKey(int cacheLevel, String prefix, String pagePath, String selectorString) {
		if (cacheLevel < 0) {
			// caching only this one instance
			return prefix + pagePath + getSelectorStringKeyPart(selectorString);
		} else if (cacheLevel == 0) {
			// site-wide
			return prefix + getSelectorStringKeyPart(selectorString);
		} else {
			// path defines the scope of caching
			return prefix + getCutPath(pagePath, cacheLevel) + getSelectorStringKeyPart(selectorString);
		}
	}

	private String getSelectorStringKeyPart(String selectorString) {
		if (StringUtils.isBlank(selectorString)) {
			return "";
		} else {
			return "." + selectorString;
		}
	}

	private String getCutPath(String path, int cacheLevel) {
		int index = getIndexOfOccurence(path, '/', cacheLevel);
		if (index < 0) {
			return path;
		} else {
			return path.substring(0, index);
		}
	}

	private int getIndexOfOccurence(String path, char character, int occurence) {
		int count = 0;
		if (path.startsWith("/")) {
			count = -1;
		}
		for (int i = 0; i < path.length(); i++) {
			if (path.charAt(i) == character) {
				count++;
			}
			if (count == occurence) {
				return i;
			}
		}
		return -1;
	}

	private String getAbsoluteTypePath(String path) {
		if (path.startsWith("/")) {
			return path;
		} else {
			return String.format(RESOURCE_TYPE_PATH, path);
		}
	}
}
