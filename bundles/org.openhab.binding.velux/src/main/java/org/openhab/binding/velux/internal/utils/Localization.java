/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.velux.internal.utils;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a utility class for dealing with localization.
 *
 * It provides the following methods:
 * <ul>
 * <li>{@link #getText} returns the localized message.</li>
 * </ul>
 * <p>
 *
 * @author Guenther Schreiner - Initial contribution
 */
@NonNullByDefault
public class Localization {
    private final Logger logger = LoggerFactory.getLogger(Localization.class);

    // Public definition

    public static final Localization UNKNOWN = new Localization();

    /*
     * ***************************
     * ***** Private Objects *****
     */
    private static final String OPENBRACKET = "(";
    private static final String CLOSEBRACKET = ")";
    private LocaleProvider localeProvider;
    private @NonNullByDefault({}) TranslationProvider i18nProvider;

    /**
     * Class, which is needed to maintain a @NonNullByDefault for class {@link Localization}.
     */
    private class UnknownLocale implements LocaleProvider {
        @Override
        public Locale getLocale() {
            return java.util.Locale.ROOT;
        }
    }

    /*
     * ************************
     * ***** Constructors *****
     */

    /**
     * Constructor
     * <P>
     * Initializes the {@link Localization} module without any framework informations.
     */
    Localization() {
        this.localeProvider = new UnknownLocale();
    }

    /**
     * Constructor
     * <P>
     * Initializes the {@link Localization} module with framework informations.
     *
     * @param localeProvider providing a locale,
     * @param i18nProvider as service interface for internationalization.
     */
    public Localization(final LocaleProvider localeProvider, final TranslationProvider i18nProvider) {
        logger.trace("Localization(Constructor w/ {},{}) called.", localeProvider, i18nProvider);
        this.localeProvider = localeProvider;
        this.i18nProvider = i18nProvider;
    }

    /**
     * Converts a given message into an equivalent localized message.
     *
     * @param key the message of type {@link String} to be converted,
     * @param arguments (optional) arguments being referenced within the messageString.
     * @return <B>localizedMessageString</B> the resulted message of type {@link String}.
     */
    public String getText(String key, Object... arguments) {
        if (i18nProvider == null) {
            logger.trace("getText() returns default as no i18nProvider existant.");
            return key;
        }
        Bundle bundle = FrameworkUtil.getBundle(this.getClass()).getBundleContext().getBundle();
        Locale locale = localeProvider.getLocale();
        String defaultText = OPENBRACKET.concat(key).concat(CLOSEBRACKET);

        String text = i18nProvider.getText(bundle, key, defaultText, locale, arguments);
        if (text == null) {
            logger.warn("Internal error: localization for key {} is missing.", key);
            text = defaultText;
        }
        logger.trace("getText() returns {}.", text);
        return text;
    }
}
