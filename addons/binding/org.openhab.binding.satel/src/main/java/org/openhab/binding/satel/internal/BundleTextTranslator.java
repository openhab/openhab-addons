/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.satel.internal;

import java.util.Locale;
import java.util.ResourceBundle;

import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * Helper class for translating I18n texts for a bundle.
 * This class should use I18nProvider service, but for some reason the service is unknown while compiling the whole
 * project.
 *
 * @author Krzysztof Goworek - Initial contribution
 *
 */
public class BundleTextTranslator implements AutoCloseable {

    private BundleContext bundleContext;
    private ServiceReference<LocaleProvider> localeProviderRef;
    private LocaleProvider localeProvider;

    /**
     * Creates new translator object for the specified bundle. Only translations from the specified bundle will be used.
     *
     * @param bundleContext the bundle context
     */
    public BundleTextTranslator(BundleContext bundleContext) {
        this.bundleContext = bundleContext;

        localeProviderRef = bundleContext.getServiceReference(LocaleProvider.class);
        if (localeProviderRef != null) {
            localeProvider = bundleContext.getService(localeProviderRef);
        }
    }

    /**
     * Returns a translation for the specified key.
     *
     * @param key the key to be translated
     * @param defaultText the default text to be used
     * @return the translated text or the default one
     */
    public String getText(String key, String defaultText) {
        Locale locale = (localeProvider != null) ? localeProvider.getLocale() : null;
        ResourceBundle resourceBundle = ResourceBundle.getBundle("ESH-INF/i18n/satel", locale);
        return resourceBundle.containsKey(key) ? resourceBundle.getString(key) : defaultText;
    }

    @Override
    public void close() {
        if (localeProviderRef != null) {
            bundleContext.ungetService(localeProviderRef);
        }
    }
}
