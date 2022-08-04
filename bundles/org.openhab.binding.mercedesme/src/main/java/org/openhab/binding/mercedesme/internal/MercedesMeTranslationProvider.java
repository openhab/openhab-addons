/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.mercedesme.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * {@link MercedesMeTranslationProvider} provides i18n message lookup.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MercedesMeTranslationProvider {
    public static final String PREFIX = "mercedesme.";
    public static final String STATUS_AUTH_NEEDED = ".status.authorization-needed";
    public static final String STATUS_IP_MISSING = ".status.ip-missing";
    public static final String STATUS_PORT_MISSING = ".status.port-missing";
    public static final String STATUS_CLIENT_ID_MISSING = ".status.client-id-missing";
    public static final String STATUS_CLIENT_SECRET_MISSING = ".status.client-secret-missing";
    public static final String STATUS_BRIDGE_MISSING = ".status.bridge-missing";
    public static final String STATUS_BRIDGEHANDLER_MISSING = ".status.bridge-handler-missing";

    private final Bundle bundle;
    private final TranslationProvider i18nProvider;
    private final LocaleProvider localeProvider;

    public MercedesMeTranslationProvider(TranslationProvider i18nProvider, LocaleProvider localeProvider) {
        this.bundle = FrameworkUtil.getBundle(this.getClass());
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
    }

    public String getText(String key) {
        String text = i18nProvider.getText(bundle, key, key, localeProvider.getLocale());
        if (text == null) {
            return key;
        }
        return text;
    }
}
