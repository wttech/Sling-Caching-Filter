package com.cognifide.cq.cache.plugins.statistics.html;

import org.apache.commons.lang.StringUtils;

public class HtmlBuilder {

	private static final String OPENING_TAG_SIGN = "<";

	private static final String CLOSING_TAG_SIGN = ">";

	private static final String SLASH = "/";

	private static final String CLASS_ATTRIBUTE = " class=\"%s\"";

	private static final String STYLE_ATTRIBUTE = " style=\"%s\"";

	private static final String ID_ATTRIBUTE = " id =\"%s\"";

	private static final String BR_TAG = OPENING_TAG_SIGN + "br " + SLASH + CLOSING_TAG_SIGN;

	private static final String DIV_OPENING_TAG = OPENING_TAG_SIGN + "div" + CLOSING_TAG_SIGN;

	private static final String DIV_CLOSING_TAG = OPENING_TAG_SIGN + SLASH + "div" + CLOSING_TAG_SIGN;

	private static final String SCRIPT_OPENING_TAG = OPENING_TAG_SIGN + "script type=\"text/javascript\"" + CLOSING_TAG_SIGN;

	private static final String SCRIPT_CLOSING_TAG = OPENING_TAG_SIGN + SLASH + "script" + CLOSING_TAG_SIGN;

	private static final String SPAN_OPENING_TAG = OPENING_TAG_SIGN + "span" + CLOSING_TAG_SIGN;

	private static final String SPAN_CLOSING_TAG = OPENING_TAG_SIGN + SLASH + "span" + CLOSING_TAG_SIGN;

	private static final String TABLE_OPENING_TAG_WITH_CLASS_ATTRIBUTE = OPENING_TAG_SIGN + "table" + CLASS_ATTRIBUTE + CLOSING_TAG_SIGN;

	private static final String TABLE_CLOSING_TAG = OPENING_TAG_SIGN + SLASH + "table" + CLOSING_TAG_SIGN;

	private static final String TABLE_CSS_CLASS = "tablesorter nicetable noauto ui-widget";

	private static final String TBODY_OPENING_TAG_WITH_CLASS_ATTRIBUTE = OPENING_TAG_SIGN + "tbody" + CLASS_ATTRIBUTE + CLOSING_TAG_SIGN;

	private static final String TBODY_CLOSING_TAG = OPENING_TAG_SIGN + SLASH + "tbody" + CLOSING_TAG_SIGN;

	private static final String TBODY_CSS_CLASS = "ui-widget-content";

	private static final String TD_OPENING_TAG = OPENING_TAG_SIGN + "td" + CLOSING_TAG_SIGN;

	private static final String TD_CLOSING_TAG = OPENING_TAG_SIGN + SLASH + "td" + CLOSING_TAG_SIGN;

	private static final String TH_OPENING_TAG_WITH_CLASS_ATTRIBUTE = OPENING_TAG_SIGN + "th" + CLASS_ATTRIBUTE + CLOSING_TAG_SIGN;

	private static final String TH_CLOSING_TAG = OPENING_TAG_SIGN + SLASH + "th" + CLOSING_TAG_SIGN;

	private static final String TH_CSS_CLASS = "ui-widget-header";

	private static final String THEAD_OPENING_TAG = OPENING_TAG_SIGN + "thead" + CLOSING_TAG_SIGN;

	private static final String THEAD_CLOSING_TAG = OPENING_TAG_SIGN + SLASH + "thead" + CLOSING_TAG_SIGN;

	private static final String TR_OPENING_TAG = OPENING_TAG_SIGN + "tr" + CLOSING_TAG_SIGN;

	private static final String TR_CLOSING_TAG = OPENING_TAG_SIGN + SLASH + "tr" + CLOSING_TAG_SIGN;

	private HtmlBuilder() {
		throw new AssertionError();
	}

	public static String br() {
		return BR_TAG;
	}

	public static String div(Object... objects) {
		return DIV_OPENING_TAG + StringUtils.join(objects) + DIV_CLOSING_TAG;
	}

	public static String script(Object o) {
		return SCRIPT_OPENING_TAG + o.toString() + SCRIPT_CLOSING_TAG;
	}

	public static String span(Object... objects) {
		return SPAN_OPENING_TAG + StringUtils.join(objects) + SPAN_CLOSING_TAG;
	}

	public static String table(Object... objects) {
		return String.format(TABLE_OPENING_TAG_WITH_CLASS_ATTRIBUTE, TABLE_CSS_CLASS) + StringUtils.join(objects) + TABLE_CLOSING_TAG;
	}

	public static String tbody(Object... objects) {
		return String.format(TBODY_OPENING_TAG_WITH_CLASS_ATTRIBUTE, TBODY_CSS_CLASS) + StringUtils.join(objects) + TBODY_CLOSING_TAG;
	}

	public static String td(Object... objects) {
		return TD_OPENING_TAG + StringUtils.join(objects) + TD_CLOSING_TAG;
	}

	public static String th(Object o) {
		return String.format(TH_OPENING_TAG_WITH_CLASS_ATTRIBUTE, TH_CSS_CLASS) + o.toString() + TH_CLOSING_TAG;
	}

	public static String thead(Object o) {
		return THEAD_OPENING_TAG + o.toString() + THEAD_CLOSING_TAG;
	}

	public static String tr(Object... objects) {
		return TR_OPENING_TAG + StringUtils.join(objects) + TR_CLOSING_TAG;
	}

	public static String css(String markup, String... cssClassess) {
		return insertAttribute(markup, CLASS_ATTRIBUTE, cssClassess);
	}

	private static String insertAttribute(String markup, String attributeName, String... values) {
		return StringUtils.replaceOnce(markup, CLOSING_TAG_SIGN, String.format(attributeName, StringUtils.join(values, ' ')) + CLOSING_TAG_SIGN);
	}

	public static String style(String markup, String... styles) {
		return insertAttribute(markup, STYLE_ATTRIBUTE, styles);
	}

	public static String id(String markup, String... styles) {
		return insertAttribute(markup, ID_ATTRIBUTE, styles);
	}
}
