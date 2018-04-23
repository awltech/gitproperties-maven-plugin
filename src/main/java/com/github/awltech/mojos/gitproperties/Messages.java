/**
 *
 */
package com.github.awltech.mojos.gitproperties;

import java.util.Locale;
import java.util.ResourceBundle;

import java.text.MessageFormat;

/**
 * Enumeration containing internationalisation-related messages and API.
 *
 * @generated com.worldline.awltech.i18ntools.wizard
 */
public enum Messages {
	NO_PROJECT_RESOLVED("NO_PROJECT_RESOLVED"), NO_PROJECT_EXISTING("NO_PROJECT_EXISTING"), NO_REPOSITORY_IN_PROJECT("NO_REPOSITORY_IN_PROJECT"), INFO_INJECTED_PROPERTY("INFO_INJECTED_PROPERTY"), ENCOUNTERED_EXCEPTION("ENCOUNTERED_EXCEPTION")
	, DATE_TIMESTAMP("DATE_TIMESTAMP"), DATE_FORMAT("DATE_FORMAT");

	/*
	 * Value of the key
	 */
	private final String messageKey;

	/*
	 * Constant ResourceBundle instance
	 */
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("Messages", Locale.getDefault());

	/**
	 * Private Enumeration Literal constructor
	 * 
	 * @param messageKey
	 *            value
	 */
	private Messages(final String messageKey) {
		this.messageKey = messageKey;
	}

	/**
	 * @return the message associated with the current value
	 */
	public String value() {
		if (Messages.RESOURCE_BUNDLE == null || !Messages.RESOURCE_BUNDLE.containsKey(this.messageKey)) {
			return "!!" + this.messageKey + "!!";
		}
		return Messages.RESOURCE_BUNDLE.getString(this.messageKey);
	}

	/**
	 * Formats and returns the message associated with the current value.
	 *
	 * @see java.text.MessageFormat
	 * @param parameters
	 *            to use during formatting phase
	 * @return formatted message
	 */
	public String value(final Object... args) {
		if (Messages.RESOURCE_BUNDLE == null || !Messages.RESOURCE_BUNDLE.containsKey(this.messageKey)) {
			return "!!" + this.messageKey + "!!";
		}
		return MessageFormat.format(Messages.RESOURCE_BUNDLE.getString(this.messageKey), args);
	}

}
