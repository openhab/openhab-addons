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
package org.openhab.binding.growatt.internal.discovery;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.growatt.internal.GrowattBindingConstants;
import org.openhab.binding.growatt.internal.config.GrowattInverterConfiguration;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.ThingUID;

/**
 * The {@link GrowattDiscoveryService} does discovery for Growatt inverters.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class GrowattDiscoveryService extends AbstractDiscoveryService {

    private final Map<ThingUID, Set<String>> bridgeInverterIds = new ConcurrentHashMap<>();

    public GrowattDiscoveryService(TranslationProvider i18nProvider, LocaleProvider localeProvider)
            throws IllegalArgumentException {
        super(Set.of(GrowattBindingConstants.THING_TYPE_INVERTER), 5, false);
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
    }

    public void putInverters(ThingUID bridgeUID, Set<String> inverterIds) {
        if (inverterIds.isEmpty()) {
            bridgeInverterIds.remove(bridgeUID);
        } else {
            bridgeInverterIds.put(bridgeUID, inverterIds);
            startScan();
        }
    }

    @Override
    protected void startScan() {
        bridgeInverterIds.forEach((bridgeUID, inverterIds) -> {
            inverterIds.forEach(inverterId -> {
                DiscoveryResult inverter = DiscoveryResultBuilder
                        .create(new ThingUID(GrowattBindingConstants.THING_TYPE_INVERTER, bridgeUID, inverterId))
                        .withBridge(bridgeUID).withLabel("@text/discovery.growatt-inverter")
                        .withProperty(GrowattInverterConfiguration.DEVICE_ID, inverterId)
                        .withRepresentationProperty(GrowattInverterConfiguration.DEVICE_ID).build();
                thingDiscovered(inverter);
            });
        });
    }
}
