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
package org.openhab.binding.tado.internal;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.osgi.framework.Bundle;

/**
 * {@link TadoTranslationProvider} provides i18n message lookup
 *
 * @author Jacob Laursen - Initial contribution
 * @author Andrew Fiddian-Green - Copied class from HDPowerview and renamed
 *
 */
@NonNullByDefault
public class TadoTranslationProvider {

    private final Bundle bundle;
    private final TranslationProvider i18nProvider;
    private final LocaleProvider localeProvider;

    public TadoTranslationProvider(Bundle bundle, TranslationProvider i18nProvider, LocaleProvider localeProvider) {
        this.bundle = bundle;
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
    }

    public String getText(String key, @Nullable Object... arguments) {
        String text = i18nProvider.getText(bundle, key, key, localeProvider.getLocale(), arguments);
        if (text != null) {
            return text;
        }
        return key;
    }

    public Locale getLocale() {
        return localeProvider.getLocale();
    }
}
