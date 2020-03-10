/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.revogismartstripcontrol.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.revogismartstripcontrol.internal.api.DiscoveryRawResponse;
import org.openhab.binding.revogismartstripcontrol.internal.api.DiscoveryResponse;
import org.openhab.binding.revogismartstripcontrol.internal.api.RevogiDiscoveryService;
import org.openhab.binding.revogismartstripcontrol.internal.udp.DatagramSocketWrapper;
import org.openhab.binding.revogismartstripcontrol.internal.udp.UdpSenderService;
import org.osgi.service.component.annotations.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.openhab.binding.revogismartstripcontrol.internal.RevogiSmartStripControlBindingConstants.SERIAL_NUMBER;
import static org.openhab.binding.revogismartstripcontrol.internal.RevogiSmartStripControlBindingConstants.SMART_STRIP_THING_TYPE;

/**
 * The {@link RevogiSmartStripDiscoveryService} helps to discover new smart strips
 *
 * @author Andi Bräu - Initial contribution
 */
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.revogismartstripcontrol")
@NonNullByDefault
public class RevogiSmartStripDiscoveryService extends AbstractDiscoveryService {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(Collections.singleton(SMART_STRIP_THING_TYPE));

    private final RevogiDiscoveryService revogiDiscoveryService;

    private static final int SEARCH_TIME = 10;

    public RevogiSmartStripDiscoveryService() {
        super(SUPPORTED_THING_TYPES, SEARCH_TIME);
        revogiDiscoveryService = new RevogiDiscoveryService(new UdpSenderService(new DatagramSocketWrapper()));
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    protected void startScan() {
        List<DiscoveryRawResponse> discoveryResponses = revogiDiscoveryService.discoverSmartStrips();
        discoveryResponses.forEach(response -> {
            ThingUID thingUID = getThingUID(response.getData());
                    if (thingUID != null) {
                        Map<String, Object> properties = new HashMap<>();
                        properties.put(Thing.PROPERTY_MODEL_ID, response.getData().getRegId());
                        properties.put(Thing.PROPERTY_MAC_ADDRESS, response.getData().getMacAddress());
                        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, response.getData().getVersion());
                        properties.put(Thing.PROPERTY_SERIAL_NUMBER, response.getData().getSerialNumber());
                        properties.put("ipAddress", response.getIpAddress());
                        DiscoveryResult discoveryResult = DiscoveryResultBuilder
                                .create(thingUID)
                                .withThingType(SMART_STRIP_THING_TYPE)
                                .withProperties(properties)
                                .withRepresentationProperty(SERIAL_NUMBER)
                                .build();
                        thingDiscovered(discoveryResult);
                    }
                }
        );
    }

    private @Nullable ThingUID getThingUID(DiscoveryResponse response) {

        if (getSupportedThingTypes().contains(SMART_STRIP_THING_TYPE)) {
            return new ThingUID(SMART_STRIP_THING_TYPE, response.getSerialNumber());
        } else {
            return null;
        }
    }
}
