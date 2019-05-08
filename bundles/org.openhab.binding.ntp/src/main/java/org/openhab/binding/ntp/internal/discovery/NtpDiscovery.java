/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 *
 * The {@link NtpDiscovery} is used to add a ntp Thing for the local time in the discovery inbox
 * *
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.ntp")
public class NtpDiscovery extends AbstractDiscoveryService {

    public NtpDiscovery() throws IllegalArgumentException {
        super(SUPPORTED_THING_TYPES_UIDS, 2);
    }

    @Override
    protected void activate(Map<String, Object> configProperties) {
        super.activate(configProperties);
    }

    @Override
    protected void modified(Map<String, Object> configProperties) {
        super.modified(configProperties);
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
        properties.put(PROPERTY_TIMEZONE, TimeZone.getDefault().getID());
        ThingUID uid = new ThingUID(THING_TYPE_NTP, "local");
        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel("Local Time")
                .build();
        thingDiscovered(result);
    }

    @Reference
    protected void setLocaleProvider(final LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    protected void unsetLocaleProvider(final LocaleProvider localeProvider) {
        this.localeProvider = null;
    }

    @Reference
    protected void setTranslationProvider(TranslationProvider i18nProvider) {
        this.i18nProvider = i18nProvider;
    }

    protected void unsetTranslationProvider(TranslationProvider i18nProvider) {
        this.i18nProvider = null;
    }

}
