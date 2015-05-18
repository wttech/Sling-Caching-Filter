package com.cognifide.cq.cache.plugins.statistics.html;

public class Element {

	private static final String OPENING_TAG_SIGN = "<";

	private static final String CLOSING_TAG_SIGN = ">";

	private static final String SLASH = "/";

	private static final String DIV_OPENING_TAG = OPENING_TAG_SIGN + "div" + CLOSING_TAG_SIGN;

	private static final String DIV_CLOSING_TAG = OPENING_TAG_SIGN + SLASH + "div" + CLOSING_TAG_SIGN;

	private final StringBuilder markup;

	public Element() {
		this(new StringBuilder());
	}

	public Element(StringBuilder markup) {
		this.markup = markup;
	}

	public Element div() {
		markup.append(DIV_OPENING_TAG).append(DIV_CLOSING_TAG);
		return this;
	}

	public Element div(Element element) {
		markup.append(DIV_OPENING_TAG).append(element.markup()).append(DIV_CLOSING_TAG);
		return this;
	}

	public Element div(String text) {
		markup.append(DIV_OPENING_TAG).append(text).append(DIV_CLOSING_TAG);
		return this;
	}

	StringBuilder markup() {
		return markup;
	}

	@Override
	public String toString() {
		return markup.toString();
	}

}
