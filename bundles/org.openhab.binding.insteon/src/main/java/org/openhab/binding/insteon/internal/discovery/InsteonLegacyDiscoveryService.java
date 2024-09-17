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
package org.openhab.binding.insteon.internal.discovery;

import static org.openhab.binding.insteon.internal.InsteonBindingConstants.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.handler.InsteonLegacyNetworkHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link InsteonLegacyDiscoveryService} is responsible for legacy device discovery.
 *
 * @author Rob Nielsen - Initial contribution
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public class InsteonLegacyDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(InsteonLegacyDiscoveryService.class);

    private final InsteonLegacyNetworkHandler handler;

    public InsteonLegacyDiscoveryService(InsteonLegacyNetworkHandler handler) {
        super(DISCOVERABLE_LEGACY_THING_TYPES_UIDS, 0, false);
        this.handler = handler;

        logger.debug("Initializing InsteonLegacyDiscoveryService");

        handler.setInsteonDiscoveryService(this);
    }

    @Override
    protected void startScan() {
    }

    public void addInsteonDevices(List<InsteonAddress> addresses) {
        for (InsteonAddress address : addresses) {
            ThingUID bridgeUID = handler.getThing().getUID();
            String id = address.toString().replace(".", "");
            ThingUID thingUID = new ThingUID(THING_TYPE_LEGACY_DEVICE, bridgeUID, id);
            String label = "Insteon Device (Legacy) " + address;
            Map<String, Object> properties = new HashMap<>();
            properties.put(PROPERTY_DEVICE_ADDRESS, address.toString());

            thingDiscovered(DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID).withLabel(label)
                    .withProperties(properties).withRepresentationProperty(PROPERTY_DEVICE_ADDRESS).build());

            logger.debug("added Insteon device {} to inbox", address);
        }
    }

    public void removeAllResults() {
        removeOlderResults(Instant.now().toEpochMilli(), handler.getThing().getUID());
    }
}
