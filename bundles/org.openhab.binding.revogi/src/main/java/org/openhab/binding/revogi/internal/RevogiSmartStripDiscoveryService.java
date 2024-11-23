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
package org.openhab.binding.revogi.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.revogi.internal.api.DiscoveryRawResponseDTO;
import org.openhab.binding.revogi.internal.api.DiscoveryResponseDTO;
import org.openhab.binding.revogi.internal.api.RevogiDiscoveryService;
import org.openhab.binding.revogi.internal.udp.DatagramSocketWrapper;
import org.openhab.binding.revogi.internal.udp.UdpSenderService;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link RevogiSmartStripDiscoveryService} helps to discover new smart strips
 *
 * @author Andi Bräu - Initial contribution
 */
@Component(service = DiscoveryService.class, configurationPid = "discovery.revogi")
@NonNullByDefault
public class RevogiSmartStripDiscoveryService extends AbstractDiscoveryService {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set
            .of(RevogiSmartStripControlBindingConstants.SMART_STRIP_THING_TYPE);

    private final RevogiDiscoveryService revogiDiscoveryService;

    private static final int SEARCH_TIMEOUT_SEC = 10;

    public RevogiSmartStripDiscoveryService() {
        super(SUPPORTED_THING_TYPES, SEARCH_TIMEOUT_SEC);
        revogiDiscoveryService = new RevogiDiscoveryService(
                new UdpSenderService(new DatagramSocketWrapper(), scheduler));
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    protected void startScan() {
        CompletableFuture<List<DiscoveryRawResponseDTO>> discoveryResponses = revogiDiscoveryService
                .discoverSmartStrips();
        discoveryResponses.thenAccept(this::applyDiscoveryResults);
    }

    private void applyDiscoveryResults(final List<DiscoveryRawResponseDTO> discoveryRawResponses) {
        discoveryRawResponses.forEach(response -> {
            ThingUID thingUID = getThingUID(response.getData());
            if (thingUID != null) {
                Map<String, Object> properties = new HashMap<>();
                properties.put(Thing.PROPERTY_MODEL_ID, response.getData().getRegId());
                properties.put(Thing.PROPERTY_MAC_ADDRESS, response.getData().getMacAddress());
                properties.put(Thing.PROPERTY_FIRMWARE_VERSION, response.getData().getVersion());
                properties.put(Thing.PROPERTY_SERIAL_NUMBER, response.getData().getSerialNumber());
                properties.put("ipAddress", response.getIpAddress());
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                        .withThingType(RevogiSmartStripControlBindingConstants.SMART_STRIP_THING_TYPE)
                        .withProperties(properties).withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER).build();
                thingDiscovered(discoveryResult);
            }
        });
    }

    private @Nullable ThingUID getThingUID(DiscoveryResponseDTO response) {
        if (getSupportedThingTypes().contains(RevogiSmartStripControlBindingConstants.SMART_STRIP_THING_TYPE)) {
            return new ThingUID(RevogiSmartStripControlBindingConstants.SMART_STRIP_THING_TYPE,
                    response.getSerialNumber());
        } else {
            return null;
        }
    }
}
