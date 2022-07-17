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
package org.openhab.binding.boschindego.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * {@link BoschIndegoTranslationProvider} provides i18n message lookup.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class BoschIndegoTranslationProvider {

    private final Bundle bundle;
    private final TranslationProvider i18nProvider;
    private final LocaleProvider localeProvider;

    public BoschIndegoTranslationProvider(TranslationProvider i18nProvider, LocaleProvider localeProvider) {
        this.bundle = FrameworkUtil.getBundle(this.getClass());
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
    }

    public @Nullable String getText(String key, @Nullable Object... arguments) {
        return i18nProvider.getText(bundle, key, null, localeProvider.getLocale(), arguments);
    }

    public String getText(String key, String defaultText, @Nullable Object... arguments) {
        String text = i18nProvider.getText(bundle, key, defaultText, localeProvider.getLocale(), arguments);
        if (text == null) {
            return defaultText;
        }
        return text;
    }
}
