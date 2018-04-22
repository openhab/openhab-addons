/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.internal.utilities;

import java.util.Locale;

import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.osgi.framework.Bundle;

/**
 * Utility class to translate strings (i18n).
 *
 * @author Patrik Gfeller
 */
public class ICloudTextTranslator {

    final private Bundle bundle;
    final private TranslationProvider i18nProvider;
    final private LocaleProvider localeProvider;

    public ICloudTextTranslator(Bundle bundle, TranslationProvider i18nProvider, LocaleProvider localeProvider) {
        this.bundle = bundle;
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
    }

    public String getText(String key, Object... arguments) {
        Locale locale = localeProvider != null ? localeProvider.getLocale() : Locale.ENGLISH;
        return i18nProvider != null ? i18nProvider.getText(bundle, key, getDefaultText(key), locale, arguments) : key;
    }

    public String getDefaultText(String key) {
        return i18nProvider.getText(bundle, key, key, Locale.ENGLISH);
    }

}
