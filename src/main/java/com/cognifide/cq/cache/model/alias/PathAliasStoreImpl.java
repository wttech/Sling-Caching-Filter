package com.cognifide.cq.cache.model.alias;

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;

@Service
@Component
public class PathAliasStoreImpl implements PathAliasStore {

	private Map<String, PathAlias> aliases;

	@Activate
	protected void activate() {
		aliases = Maps.newHashMap();
	}

	@Override
	public void populate(String[] aliasesString) {
		for (PathAlias pathAlias : new PathAliasReader().readAliases(aliasesString)) {
			aliases.put(pathAlias.getName(), pathAlias);
		}
	}

	@Override
	public Collection<Pattern> getPathsForAlias(String aliasName) {
		Collection<Pattern> paths = Collections.emptySet();
		if (StringUtils.isNotEmpty(aliasName)) {
			PathAlias pathAlias = aliases.get(aliasName);
			if (null != pathAlias) {
				paths = pathAlias.getPaths();
			}
		}
		return paths;
	}

	@Override
	public boolean isAlias(String path) {
		return aliases.containsKey(path);
	}

	@Deactivate
	protected void deactivate() {
		aliases.clear();
	}
}
