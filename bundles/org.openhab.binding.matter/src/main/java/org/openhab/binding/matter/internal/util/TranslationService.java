/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.matter.internal.util;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Helper service to get localized strings from the bundle
 * 
 * @author Dan Cunningham - Initial Contribution
 */
@NonNullByDefault
@Component(service = TranslationService.class, scope = ServiceScope.SINGLETON)
public class TranslationService {
    private final BundleContext bundleContext;
    private final Bundle bundle;
    private final LocaleProvider localeProvider;
    private final TranslationProvider translationProvider;

    @Activate
    public TranslationService(@Reference LocaleProvider localeProvider,
            @Reference TranslationProvider translationProvider) {
        this.bundleContext = FrameworkUtil.getBundle(TranslationService.class).getBundleContext();
        this.bundle = bundleContext.getBundle();
        this.localeProvider = localeProvider;
        this.translationProvider = translationProvider;
    }

    /**
     * Get a translation for a given key
     * 
     * @param key the key to get the translation for (with or without the @text/ prefix)
     * @return the translation
     */
    public String getTranslation(String key, Object... args) {
        String lookupKey = key.replace("@text/", "");
        String result = translationProvider.getText(bundle, lookupKey, lookupKey, localeProvider.getLocale(), args);
        return result == null ? lookupKey + " " + Arrays.toString(args) : result;
    }

    public LocaleProvider getLocaleProvider() {
        return localeProvider;
    }

    public TranslationProvider getTranslationProvider() {
        return translationProvider;
    }
}
