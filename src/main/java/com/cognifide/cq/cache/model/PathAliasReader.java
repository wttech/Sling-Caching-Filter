package com.cognifide.cq.cache.model;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class PathAliasReader {

	private static final Pattern ALIAS_NAME_PATTERN = Pattern.compile("^\\$[\\w_-]+$");

	private static final char SEPARATOR = '|';

	public Set<PathAlias> readAliases(String[] aliasStrings) {
		Set<PathAlias> aliases = new HashSet<PathAlias>();
		for (String aliasString : aliasStrings) {
			PathAlias alias = readAlias(aliasString);
			if (alias != null) {
				aliases.add(alias);
			}
		}
		return aliases;
	}

	public PathAlias readAlias(String aliasString) {
		PathAlias alias = null;
		String[] tokens = StringUtils.split(aliasString, SEPARATOR);
		if ((tokens != null) && (tokens.length > 1)) {
			String aliasName = tokens[0];
			if (isAliasNameValid(aliasName)) {
				Set<String> paths = getPaths(tokens);
				if (paths.size() > 0) {
					alias = new PathAlias(aliasName, paths);
				}
			}
		}
		return alias;
	}

	private Set<String> getPaths(String[] tokens) {
		Set<String> paths = new HashSet<String>();
		for (int i = 1; i < tokens.length; i++) {
			if (isPathValid(tokens[i])) {
				paths.add(tokens[i]);
			}
		}
		return paths;
	}

	private boolean isPathValid(String path) {
		return StringUtils.isNotBlank(path);
	}

	private boolean isAliasNameValid(String aliasName) {
		return ALIAS_NAME_PATTERN.matcher(aliasName).matches();
	}

}
