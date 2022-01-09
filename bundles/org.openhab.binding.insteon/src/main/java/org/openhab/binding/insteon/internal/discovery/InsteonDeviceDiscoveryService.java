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
package org.openhab.binding.insteon.internal.discovery;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.insteon.internal.InsteonBindingConstants;
import org.openhab.binding.insteon.internal.handler.InsteonNetworkHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link InsteonDeviceDiscoveryService} is responsible for device discovery.
 *
 * @author Rob Nielsen - Initial contribution
 */
@NonNullByDefault
public class InsteonDeviceDiscoveryService extends AbstractDiscoveryService {
    private static final String ADDRESS = "address";

    private final Logger logger = LoggerFactory.getLogger(InsteonDeviceDiscoveryService.class);

    public InsteonDeviceDiscoveryService(InsteonNetworkHandler handler) {
        super(new HashSet<>(Arrays.asList(InsteonBindingConstants.DEVICE_THING_TYPE)), 0, false);

        handler.setInsteonDeviceDiscoveryService(this);

        logger.debug("Initializing InsteonNetworkDiscoveryService");
    }

    @Override
    protected void startScan() {
    }

    public void addInsteonDevices(List<String> addresses, ThingUID bridgeUid) {
        for (String address : addresses) {
            String[] parts = address.split("\\.");
            if (parts.length != 3) {
                logger.warn("Address {} must be in the format XX.XX.XX", address);

                continue;
            }

            String name = parts[0] + parts[1] + parts[2];
            ThingUID uid = new ThingUID(InsteonBindingConstants.DEVICE_THING_TYPE, bridgeUid, name);
            Map<String, Object> properties = new HashMap<>();
            properties.put(ADDRESS, address);

            thingDiscovered(
                    DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel("Insteon Device  " + name)
                            .withBridge(bridgeUid).withRepresentationProperty(ADDRESS).build());

            logger.debug("Added Insteon device {} with the address {}", name, address);
        }
    }
}
