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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.TranslationProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * 
 * @author Patrik Gfeller - Initial Contribution
 */
public class HueSyncLocalizer {

    private static final Locale locale = Locale.ENGLISH;

    private static final BundleContext bundleContext = FrameworkUtil.getBundle(HueSyncLocalizer.class)
            .getBundleContext();
    private static final @Nullable ServiceReference<@NonNull TranslationProvider> serviceReference = bundleContext != null
            ? bundleContext.getServiceReference(TranslationProvider.class)
            : null;

    private static final TranslationProvider translationProvider = serviceReference != null
            ? bundleContext.getService(serviceReference)
            : null;

    public static String getResourceString(String key) {
        key = key.replace("@text/", "");

        return translationProvider != null ? translationProvider.getText(bundleContext.getBundle(), key, key, locale)
                : key;
    }
}
