package com.cognifide.cq.cache.definition.osgi;

import com.google.common.base.Predicate;
import org.apache.commons.lang.StringUtils;

class NotBlankPredicate implements Predicate<String> {

	@Override
	public boolean apply(String input) {
		return StringUtils.isNotBlank(input);
	}

}
