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
package org.openhab.binding.ntp.internal.discovery;

import static org.openhab.binding.ntp.internal.NtpBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 *
 * The {@link NtpDiscovery} is used to add a ntp Thing for the local time in the discovery inbox
 * *
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.ntp")
public class NtpDiscovery extends AbstractDiscoveryService {

    private final TimeZoneProvider timeZoneProvider;

    @Activate
    public NtpDiscovery(final @Reference LocaleProvider localeProvider,
            final @Reference TranslationProvider i18nProvider, final @Reference TimeZoneProvider timeZoneProvider,
            @Nullable Map<String, Object> configProperties) throws IllegalArgumentException {
        super(SUPPORTED_THING_TYPES_UIDS, 2);
        this.localeProvider = localeProvider;
        this.i18nProvider = i18nProvider;
        this.timeZoneProvider = timeZoneProvider;
        activate(configProperties);
    }

    @Override
    protected void startBackgroundDiscovery() {
        scheduler.schedule(() -> {
            discoverNtp();
        }, 1, TimeUnit.SECONDS);
    }

    @Override
    protected void startScan() {
        discoverNtp();
    }

    /**
     * Add a ntp Thing for the local time in the discovery inbox
     */
    private void discoverNtp() {
        Map<String, Object> properties = new HashMap<>(4);
        properties.put(PROPERTY_TIMEZONE, timeZoneProvider.getTimeZone().getId());
        ThingUID uid = new ThingUID(THING_TYPE_NTP, "local");
        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel("Local Time")
                .build();
        thingDiscovered(result);
    }
}
