/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gpstracker.internal.handler;

import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.util.Locale;

/**
 * Utility class used to translate dynamic channel names.
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class TranslationUtil {
    /**
     * Locale provider
     */
    private LocaleProvider localeProvider;

    /**
     * Translation provider to support dynamic channel names
     */
    private TranslationProvider i18nProvider;

    /**
     * Context
     */
    private BundleContext bundleContext;

    /**
     * Constructor.
     *
     * @param bundleContext Bundle context
     */
    public TranslationUtil(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        //get locale provider
        ServiceReference<LocaleProvider> locServiceReference = bundleContext.getServiceReference(LocaleProvider.class);
        if (locServiceReference != null) {
            localeProvider = bundleContext.getService(locServiceReference);
        }

        //get translation provider
        ServiceReference<TranslationProvider> trServiceReference = bundleContext.getServiceReference(TranslationProvider.class);
        if (trServiceReference != null) {
            i18nProvider = bundleContext.getService(trServiceReference);
        }
    }

    String getText(String key, Object... arguments) {
        Locale locale = localeProvider != null ? localeProvider.getLocale() : Locale.ENGLISH;
        return i18nProvider != null ? i18nProvider.getText(bundleContext.getBundle(), key, getDefaultText(key), locale, arguments) : key;
    }

    private String getDefaultText(String key) {
        return i18nProvider.getText(bundleContext.getBundle(), key, key, Locale.ENGLISH);
    }
}
