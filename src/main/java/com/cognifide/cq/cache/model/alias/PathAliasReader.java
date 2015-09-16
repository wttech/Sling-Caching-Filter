package com.cognifide.cq.cache.model.alias;

import com.google.common.collect.Sets;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PathAliasReader {

	private static final Logger logger = LoggerFactory.getLogger(PathAliasReader.class);

	private static final Pattern ALIAS_NAME_PATTERN = Pattern.compile("^\\$[\\w_-]+$");

	private static final char SEPARATOR = '|';

	Set<PathAlias> readAliases(String[] aliasStrings) {
		Set<PathAlias> aliases = Sets.newHashSet();
		for (String aliasString : aliasStrings) {
			PathAlias alias = readAlias(aliasString);
			if (alias != null) {
				aliases.add(alias);
			}
		}
		return aliases;
	}

	private PathAlias readAlias(String aliasString) {
		PathAlias alias = null;
		String[] tokens = StringUtils.split(aliasString, SEPARATOR);
		if ((tokens != null) && (tokens.length > 1)) {
			String aliasName = tokens[0];
			if (isAliasNameValid(aliasName)) {
				Set<Pattern> patterns = readPaths(tokens);
				if (!patterns.isEmpty()) {
					alias = new PathAlias(aliasName, patterns);
				}
			}
		}
		return alias;
	}

	private boolean isAliasNameValid(String aliasName) {
		return ALIAS_NAME_PATTERN.matcher(aliasName).matches();
	}

	private Set<Pattern> readPaths(String[] tokens) {
		Set<Pattern> patterns = Sets.newHashSet();
		for (int i = 1; i < tokens.length; i++) {
			if (isPathValid(tokens[i])) {
				tryToAddPattern(patterns, tokens[i]);
			}
		}
		return patterns;
	}

	private boolean isPathValid(String path) {
		return StringUtils.isNotBlank(path);
	}

	private void tryToAddPattern(Set<Pattern> patterns, String path) {
		try {
			patterns.add(Pattern.compile(path));
		} catch (PatternSyntaxException x) {
			logger.error("Pattern " + path + " is invalid.", x);
		}
	}
}
