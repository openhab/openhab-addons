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
package org.openhab.binding.emotiva.internal;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * This class provides translated texts.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
@Component(service = EmotivaTranslationProvider.class)
public class EmotivaTranslationProvider {

    private final Bundle bundle;
    private final TranslationProvider i18nProvider;
    private final LocaleProvider localeProvider;

    @Activate
    public EmotivaTranslationProvider(@Reference TranslationProvider i18nProvider,
            @Reference LocaleProvider localeProvider) {
        this.bundle = FrameworkUtil.getBundle(this.getClass());
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
    }

    public EmotivaTranslationProvider(final EmotivaTranslationProvider other) {
        this.bundle = other.bundle;
        this.i18nProvider = other.i18nProvider;
        this.localeProvider = other.localeProvider;
    }

    public String getText(String key, @Nullable Object... arguments) {
        Locale locale = localeProvider.getLocale();
        String message = i18nProvider.getText(bundle, key, this.getDefaultText(key), locale, arguments);
        if (message != null) {
            return message;
        }
        return key;
    }

    public @Nullable String getDefaultText(String key) {
        return i18nProvider.getText(bundle, key, key, Locale.ENGLISH);
    }
}
