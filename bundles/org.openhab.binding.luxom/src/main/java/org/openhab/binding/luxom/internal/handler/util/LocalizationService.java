/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.luxom.internal.handler.util;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kris Jespers - Initial contribution
 */
@NonNullByDefault
public class LocalizationService {
    private final Logger logger = LoggerFactory.getLogger(LocalizationService.class);
    private final Bundle bundle;

    private @NonNullByDefault({}) LocaleProvider localeProvider;
    private @NonNullByDefault({}) TranslationProvider i18nProvider;

    public LocalizationService(@NonNullByDefault({}) LocaleProvider localeProvider,
            @NonNullByDefault({}) TranslationProvider i18nProvider) {
        this.localeProvider = localeProvider;
        this.i18nProvider = i18nProvider;
        this.bundle = FrameworkUtil.getBundle(this.getClass()).getBundleContext().getBundle();
    }

    /**
     * Converts a given message into an equivalent localized message.
     *
     * @param key the message of type {@link String} to be converted,
     * @param defaultText default text if localization property is missing.
     * @param arguments (optional) arguments being referenced within the messageString.
     * @return <B>localizedMessageString</B> the resulted message of type {@link String}.
     */
    public String getText(String key, String defaultText, Object... arguments) {
        Locale locale = localeProvider.getLocale();

        String text = i18nProvider.getText(bundle, key, defaultText, locale, arguments);
        if (text == null) {
            logger.warn("localization for key {} is missing.", key);
            text = defaultText;
        }
        logger.trace("getText() returns {}.", text);
        return text;
    }

    public void setLocaleProvider(LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    public void setI18nProvider(TranslationProvider i18nProvider) {
        this.i18nProvider = i18nProvider;
    }
}
