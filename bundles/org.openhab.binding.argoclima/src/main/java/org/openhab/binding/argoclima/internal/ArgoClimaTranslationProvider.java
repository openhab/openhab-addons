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
package org.openhab.binding.argoclima.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Provides a convenience wrapper around framework-provided {@link TranslationProvider}, pre-filling the bundle and
 * locale parameters
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
@Component(service = { ArgoClimaTranslationProvider.class })
public class ArgoClimaTranslationProvider {
    private final TranslationProvider i18nProvider;
    private final LocaleProvider localeProvider;
    private final @Nullable Bundle bundle;

    @Activate
    public ArgoClimaTranslationProvider(final @Reference TranslationProvider i18nProvider,
            final @Reference LocaleProvider localeProvider, final BundleContext context) {
        this.bundle = context.getBundle();
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
    }

    /**
     * Similar to {@link TranslationProvider#getText(Bundle, String, String, java.util.Locale, Object...)}.
     * Pre-fills {@code Bundle} and {@code Locale} params to reduce boilerplate.
     *
     * @param key the key to be translated (can be empty)
     * @param defaultText the default text to be used (can be null or empty)
     * @param arguments the arguments to be injected into the translation (each arg can be null)
     * @return the translated text or the default text (can be null or empty)
     */
    public @Nullable String getText(String key, @Nullable String defaultText, Object @Nullable... arguments) {
        return i18nProvider.getText(bundle, key, defaultText, localeProvider.getLocale(), arguments);
    }

    /**
     * Similar to {@link TranslationProvider#getText(Bundle, String, String, java.util.Locale)}.
     * Pre-fills {@code Bundle} and {@code Locale} params to reduce boilerplate.
     *
     * @param key the key to be translated (can be empty)
     * @param defaultText the default text to be used (can be null or empty)
     * @return the translated text or the default text (can be null or empty)
     */
    public @Nullable String getText(String key, @Nullable String defaultText) {
        return i18nProvider.getText(bundle, key, defaultText, localeProvider.getLocale());
    }
}
