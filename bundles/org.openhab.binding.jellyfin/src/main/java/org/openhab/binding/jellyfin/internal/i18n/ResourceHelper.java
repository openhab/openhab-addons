/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.jellyfin.internal.i18n;

import org.eclipse.jdt.annotation.NonNullByDefault;
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
public class ResourceHelper {
    private static volatile TranslationProvider translationProvider = null;
    private static volatile Bundle bundle = null;

    private static TranslationProvider lookupProvider() {
        try {
            BundleContext ctx = null;
            Bundle b = FrameworkUtil.getBundle(ResourceHelper.class);
            if (b != null) {
                ctx = b.getBundleContext();
            }

            if (ctx == null) {
                return null;
            }

            ServiceReference<TranslationProvider> ref = ctx.getServiceReference(TranslationProvider.class);
            if (ref == null) {
                return null;
            }

            TranslationProvider tp = ctx.getService(ref);
            if (tp != null) {
                bundle = b;
                return tp;
            }
        } catch (Throwable t) {
            // defensive: any OSGi issues should not break callers
        }
        return null;
    }

    public static String getResourceString(String key) {
        String lookupKey = key.replace("@text/", "");

        String missingKey = "Missing Translation: " + key;

        TranslationProvider provider = translationProvider;
        if (provider == null) {
            provider = lookupProvider();
            translationProvider = provider; // cache (may be null)
        }

        if (provider == null || bundle == null) {
            return missingKey;
        }

        try {
            String localizedString = provider.getText(bundle, lookupKey, missingKey, null);
            return localizedString == null ? missingKey : localizedString;
        } catch (Throwable t) {
            return missingKey;
        }
    }
}
