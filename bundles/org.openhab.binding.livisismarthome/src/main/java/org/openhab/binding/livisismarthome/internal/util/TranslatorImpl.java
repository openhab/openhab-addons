/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.livisismarthome.internal.util;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.osgi.framework.BundleContext;

/**
 * The {@link TranslatorImpl} is responsible for translating strings to another (supported) language.
 * It uses {@link TranslationProvider} and {@link LocaleProvider} internally to make it easier to translate strings.
 *
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
public class TranslatorImpl implements Translator {

    private final TranslationProvider translationProvider;
    private final LocaleProvider localeProvider;
    private final BundleContext bundleContext;

    public TranslatorImpl(TranslationProvider translationProvider, LocaleProvider localeProvider,
            BundleContext bundleContext) {
        this.translationProvider = translationProvider;
        this.localeProvider = localeProvider;
        this.bundleContext = bundleContext;
    }

    @Override
    public String getText(String key, String defaultValue) {
        @Nullable
        String text = translationProvider.getText(bundleContext.getBundle(), key, defaultValue,
                localeProvider.getLocale());
        if (text != null) {
            return text;
        }
        return defaultValue;
    }
}
