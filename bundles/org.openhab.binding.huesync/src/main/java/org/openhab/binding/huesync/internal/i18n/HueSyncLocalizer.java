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
package org.openhab.binding.huesync.internal.i18n;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.TranslationProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * 
 * @author Patrik Gfeller - Initial Contribution
 */
@NonNullByDefault
public class HueSyncLocalizer {

    private static final Locale locale = Locale.ENGLISH;

    private static final BundleContext bundleContext = FrameworkUtil.getBundle(HueSyncLocalizer.class)
            .getBundleContext();
    private static final ServiceReference<TranslationProvider> serviceReference = bundleContext
            .getServiceReference(TranslationProvider.class);

    public static String getResourceString(String key) {
        key = key.replace("@text/", "");

        Bundle bundle = bundleContext.getBundle();
        @Nullable
        TranslationProvider translationProvider = bundleContext.getService(serviceReference);

        String text = translationProvider != null ? translationProvider.getText(bundle, key, key, locale) : key;

        // TODO: Add log message in case of translation problem ...
        return text != null ? text : key;
    }
}
