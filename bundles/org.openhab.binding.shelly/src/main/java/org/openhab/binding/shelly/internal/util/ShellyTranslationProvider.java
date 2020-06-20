/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.shelly.internal.util;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.osgi.framework.Bundle;

/**
 * {@link ShellyTranslationProvider} provides i18n message lookup
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyTranslationProvider {

    private final Bundle bundle;
    private final TranslationProvider i18nProvider;
    private final LocaleProvider localeProvider;

    public ShellyTranslationProvider(Bundle bundle, TranslationProvider i18nProvider, LocaleProvider localeProvider) {
        this.bundle = bundle;
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
    }

    public ShellyTranslationProvider(final ShellyTranslationProvider other) {
        this.bundle = other.bundle;
        this.i18nProvider = other.i18nProvider;
        this.localeProvider = other.localeProvider;
    }

    public @Nullable String get(String key, @Nullable Object... arguments) {
        return getText(key.contains("@text/") || key.contains(".shelly.") ? key : "message." + key, arguments);
    }

    public @Nullable String getText(String key, @Nullable Object... arguments) {
        try {
            Locale locale = localeProvider.getLocale();
            return i18nProvider.getText(bundle, key, getDefaultText(key), locale, arguments);
        } catch (IllegalArgumentException e) {
            return "Unable to load message for key " + key;
        }
    }

    public @Nullable String getDefaultText(String key) {
        return i18nProvider.getText(bundle, key, key, Locale.ENGLISH);
    }
}
