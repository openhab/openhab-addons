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
package org.openhab.binding.shelly.internal.provider;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.util.ShellyUtils;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * {@link ShellyTranslationProvider} provides i18n message lookup
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@Component(service = ShellyTranslationProvider.class)
public class ShellyTranslationProvider {
    private final Bundle bundle;
    private final TranslationProvider i18nProvider;
    private final LocaleProvider localeProvider;

    @Activate
    public ShellyTranslationProvider(@Reference TranslationProvider i18nProvider,
            @Reference LocaleProvider localeProvider) {
        this.bundle = FrameworkUtil.getBundle(this.getClass());
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
    }

    public String get(String key, @Nullable Object... arguments) {
        return getText(key.contains("@text/") || key.contains(".shelly.") ? key : "message." + key, arguments);
    }

    public String getText(String key, @Nullable Object... arguments) {
        try {
            Locale locale = localeProvider.getLocale();
            String message = i18nProvider.getText(bundle, key, getDefaultText(key), locale, arguments);
            return ShellyUtils.getString(message);
        } catch (IllegalArgumentException e) {
            return "Unable to load message for key " + key;
        }
    }

    public @Nullable String getDefaultText(String key) {
        return i18nProvider.getText(bundle, key, key, Locale.ENGLISH);
    }
}
