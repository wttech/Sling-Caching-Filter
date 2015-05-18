package com.cognifide.cq.cache.plugins.statistics.html;

import org.apache.commons.lang.StringUtils;

public class HtmlBuilder {

	private static final String OPENING_TAG_SIGN = "<";

	private static final String CLOSING_TAG_SIGN = ">";

	private static final String SLASH = "/";

	private static final String CLASS_ATTRIBUTE = " class=\"%s\"";

	private static final String STYLE_ATTRIBUTE = " style=\"%s\"";

	private static final String DIV_OPENING_TAG = OPENING_TAG_SIGN + "div" + CLOSING_TAG_SIGN;

	private static final String DIV_OPENING_TAG_WITH_CLASS_ATTRIBUTE = OPENING_TAG_SIGN + "div" + CLASS_ATTRIBUTE + CLOSING_TAG_SIGN;

	private static final String DIV_OPENING_TAG_WITH_CLASS_AND_STYLE_ATTRIBUTE = OPENING_TAG_SIGN + "div" + CLASS_ATTRIBUTE + STYLE_ATTRIBUTE + CLOSING_TAG_SIGN;

	private static final String DIV_CLOSING_TAG = OPENING_TAG_SIGN + SLASH + "div" + CLOSING_TAG_SIGN;

	private static final String LI_OPENING_TAG = OPENING_TAG_SIGN + "li" + CLOSING_TAG_SIGN;

	private static final String LI_CLOSING_TAG = OPENING_TAG_SIGN + SLASH + "li" + CLOSING_TAG_SIGN;

	private static final String SPAN_OPENING_TAG = OPENING_TAG_SIGN + "span" + CLOSING_TAG_SIGN;

	private static final String SPAN_OPENING_TAG_WITH_CLASS_ATTRIBUTE = OPENING_TAG_SIGN + "span" + CLASS_ATTRIBUTE + CLOSING_TAG_SIGN;

	private static final String SPAN_CLOSING_TAG = OPENING_TAG_SIGN + SLASH + "span" + CLOSING_TAG_SIGN;

	private static final String TD_OPENING_TAG = OPENING_TAG_SIGN + "td" + CLOSING_TAG_SIGN;

	private static final String TD_CLOSING_TAG = OPENING_TAG_SIGN + SLASH + "td" + CLOSING_TAG_SIGN;

	private static final String TH_OPENING_TAG_WITH_CLASS_ATTRIBUTE = OPENING_TAG_SIGN + "th" + CLASS_ATTRIBUTE + CLOSING_TAG_SIGN;

	private static final String TH_CLOSING_TAG = OPENING_TAG_SIGN + SLASH + "th" + CLOSING_TAG_SIGN;

	public static CharSequence div(Object... objects) {
		return DIV_OPENING_TAG + StringUtils.join(objects) + DIV_CLOSING_TAG;
	}

	public static CharSequence div(String cssClasses, Object o) {
		return String.format(DIV_OPENING_TAG_WITH_CLASS_ATTRIBUTE, cssClasses) + o.toString() + DIV_CLOSING_TAG;
	}

	public static CharSequence div(String cssClasses, String styles, Object o) {
		return String.format(DIV_OPENING_TAG_WITH_CLASS_AND_STYLE_ATTRIBUTE, cssClasses, styles) + o.toString() + DIV_CLOSING_TAG;
	}

	public static CharSequence li(Object o) {
		return LI_OPENING_TAG + o.toString() + LI_CLOSING_TAG;
	}

	public static CharSequence span(Object... objects) {
		return SPAN_OPENING_TAG + StringUtils.join(objects) + SPAN_CLOSING_TAG;
	}

	public static CharSequence span(String cssClasses, Object o) {
		return String.format(SPAN_OPENING_TAG_WITH_CLASS_ATTRIBUTE, cssClasses) + o.toString() + SPAN_CLOSING_TAG;
	}

	public static CharSequence td(Object... objects) {
		return TD_OPENING_TAG + StringUtils.join(objects) + TD_CLOSING_TAG;
	}

	public static CharSequence th(String cssClasses, Object o) {
		return String.format(TH_OPENING_TAG_WITH_CLASS_ATTRIBUTE, cssClasses) + o.toString() + TH_CLOSING_TAG;
	}
}
