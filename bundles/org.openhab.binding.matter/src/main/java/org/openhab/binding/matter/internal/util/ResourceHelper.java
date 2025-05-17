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

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.i18n.TranslationProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * Helper class to get localized strings from the bundle
 * borrowed and slightly modified from the huesync binding
 * 
 * @author Dan Cunningham - Initial Contribution
 */
@NonNullByDefault
public class ResourceHelper {
    private static final Locale LOCALE = Locale.ENGLISH;
    private static final BundleContext BUNDLE_CONTEXT = FrameworkUtil.getBundle(ResourceHelper.class)
            .getBundleContext();
    private static final ServiceReference<TranslationProvider> SERVICE_REFERENCE = BUNDLE_CONTEXT
            .getServiceReference(TranslationProvider.class);
    private static final Bundle BUNDLE = BUNDLE_CONTEXT.getBundle();

    public static String getResourceString(String key) {
        String lookupKey = key.replace("@text/", "");

        String result = (BUNDLE_CONTEXT
                .getService(SERVICE_REFERENCE) instanceof TranslationProvider translationProvider)
                        ? translationProvider.getText(BUNDLE, lookupKey, lookupKey, LOCALE)
                        : lookupKey;

        return result == null ? lookupKey : result;
    }
}
