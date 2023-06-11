/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.enphase.internal;

import static org.openhab.binding.enphase.internal.EnphaseBindingConstants.ERROR_NODATA;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * Class to get the message for the enphase message code.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class MessageTranslator {

    private final LocaleProvider localeProvider;
    private final TranslationProvider i18nProvider;
    private final Bundle bundle;

    public MessageTranslator(LocaleProvider localeProvider, TranslationProvider i18nProvider) {
        this.localeProvider = localeProvider;
        this.i18nProvider = i18nProvider;
        bundle = FrameworkUtil.getBundle(this.getClass());
    }

    /**
     * Gets the message text for the enphase message code.
     *
     * @param key the enphase message code
     * @return translated key
     */
    public @Nullable String translate(String key) {
        return i18nProvider.getText(bundle, key, ERROR_NODATA, localeProvider.getLocale());
    }
}
