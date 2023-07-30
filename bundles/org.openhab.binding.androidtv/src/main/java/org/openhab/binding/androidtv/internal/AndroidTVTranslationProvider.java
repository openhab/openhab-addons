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
package org.openhab.binding.androidtv.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AndroidTVTranslationProvider} provides i18n message lookup.
 * 
 * @author Ben Rosenblum - Initial contribution
 */
@NonNullByDefault
public class AndroidTVTranslationProvider {

    private final Bundle bundle;
    private final TranslationProvider i18nProvider;
    private final LocaleProvider localeProvider;
    private final Logger logger = LoggerFactory.getLogger(AndroidTVTranslationProvider.class);

    public AndroidTVTranslationProvider(TranslationProvider i18nProvider, LocaleProvider localeProvider) {
        this.bundle = FrameworkUtil.getBundle(this.getClass());
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
    }

    public String getText(String key, @Nullable Object... arguments) {
        @Nullable
        String text = i18nProvider.getText(bundle, key, null, localeProvider.getLocale(), arguments);
        if (text != null) {
            logger.trace("Translated: {} as {}", key, text);
            return text;
        } else {
            logger.trace("Failed to translate: {}", key);
            return key;
        }
    }
}
