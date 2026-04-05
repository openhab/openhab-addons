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
package org.openhab.binding.huesync.internal.i18n;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.i18n.TranslationProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * 
 * @author Patrik Gfeller - Initial Contribution
 * @author Patrik Gfeller - Issue #18376, Exception message is not resolved using language resource strings
 */
@NonNullByDefault
public class ResourceHelper {
    @SuppressWarnings("null")
    public static String getResourceString(String key) {
        String lookupKey = key.replace("@text/", "");

        String missingKey = "Missing Translation: " + key;

        Bundle bundle = FrameworkUtil.getBundle(ResourceHelper.class);
        if (bundle == null) {
            return missingKey;
        }

        BundleContext context = bundle.getBundleContext();
        if (context == null) {
            return missingKey;
        }

        ServiceReference<TranslationProvider> ref = context.getServiceReference(TranslationProvider.class);
        if (ref == null) {
            return missingKey;
        }

        TranslationProvider provider = context.getService(ref);
        if (provider == null) {
            context.ungetService(ref);
            return missingKey;
        }

        String localizedString = provider.getText(bundle, lookupKey, missingKey, null);
        context.ungetService(ref);

        return localizedString == null ? missingKey : localizedString;
    }
}
