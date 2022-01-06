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
package org.openhab.binding.dsmr.internal.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * Tracks the i18n provider dependencies of the {@link DSMRMeterDiscoveryService}. The {@link DSMRMeterDiscoveryService}
 * is a {@link ThingHandlerService} so its i18n dependencies cannot be injected using service component annotations.
 *
 * @author Wouter Born - Initial contribution
 */
@Component
@NonNullByDefault
public class DSMRI18nProviderTracker {

    static @Nullable TranslationProvider i18nProvider;
    static @Nullable LocaleProvider localeProvider;

    @Activate
    public DSMRI18nProviderTracker(final @Reference TranslationProvider i18nProvider,
            final @Reference LocaleProvider localeProvider) {
        DSMRI18nProviderTracker.i18nProvider = i18nProvider;
        DSMRI18nProviderTracker.localeProvider = localeProvider;
    }

    @Deactivate
    public void deactivate() {
        i18nProvider = null;
        localeProvider = null;
    }
}
