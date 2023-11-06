/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.icloud.internal.utilities;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.osgi.framework.Bundle;

/**
 * Utility class to translate strings (i18n).
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class ICloudTextTranslator {

    private final Bundle bundle;
    private final TranslationProvider i18nProvider;
    private final LocaleProvider localeProvider;

    public ICloudTextTranslator(Bundle bundle, TranslationProvider i18nProvider, LocaleProvider localeProvider) {
        this.bundle = bundle;
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
    }

    public String getText(String key, Object... arguments) {
        Locale locale = localeProvider.getLocale();
        String retText = i18nProvider.getText(bundle, key, getDefaultText(key), locale, arguments);
        return retText != null ? retText : key;
    }

    public String getDefaultText(@Nullable String key) {
        String retText = i18nProvider.getText(bundle, key, key, Locale.ENGLISH);
        return retText != null ? retText : key != null ? key : "UNKNOWN_TEXT";
    }
}
