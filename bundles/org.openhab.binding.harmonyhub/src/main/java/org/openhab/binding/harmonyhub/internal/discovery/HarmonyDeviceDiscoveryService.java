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
package org.openhab.binding.harmonyhub.internal.discovery;

import static org.openhab.binding.harmonyhub.internal.HarmonyHubBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.harmonyhub.internal.handler.HarmonyHubHandler;
import org.openhab.binding.harmonyhub.internal.handler.HubStatusListener;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.harmony.config.Device;
import com.digitaldan.harmony.config.HarmonyConfig;

/**
 * The {@link HarmonyDeviceDiscoveryService} class discovers Harmony Devices connected to a Harmony Hub
 *
 * @author Dan Cunningham - Initial contribution
 * @author Wouter Born - Add null annotations
 */
@NonNullByDefault
public class HarmonyDeviceDiscoveryService extends AbstractDiscoveryService implements HubStatusListener {

    private static final int TIMEOUT = 5;

    private final Logger logger = LoggerFactory.getLogger(HarmonyDeviceDiscoveryService.class);
    private final HarmonyHubHandler bridge;

    public HarmonyDeviceDiscoveryService(HarmonyHubHandler bridge) {
        super(HarmonyHubHandler.SUPPORTED_THING_TYPES_UIDS, TIMEOUT, true);
        logger.debug("HarmonyDeviceDiscoveryService {}", bridge);
        this.bridge = bridge;
        this.bridge.addHubStatusListener(this);
    }

    @Override
    protected void startScan() {
        discoverDevices();
    }

    @Override
    protected void startBackgroundDiscovery() {
        discoverDevices();
    }

    @Override
    public void hubStatusChanged(ThingStatus status) {
        if (status.equals(ThingStatus.ONLINE)) {
            discoverDevices();
        }
    }

    @Override
    protected void deactivate() {
        super.deactivate();
        bridge.removeHubStatusListener(this);
    }

    /**
     * Discovers devices connected to a hub
     */
    private void discoverDevices() {
        if (bridge.getThing().getStatus() != ThingStatus.ONLINE) {
            logger.debug("Harmony Hub not online, scanning postponed");
            return;
        }
        logger.debug("getting devices on {}", bridge.getThing().getUID().getId());
        bridge.getConfigFuture().thenAccept(this::addDiscoveryResults).exceptionally(e -> {
            logger.debug("Could not get harmony config for discovery, skipping");
            return null;
        });
    }

    private void addDiscoveryResults(@Nullable HarmonyConfig config) {
        if (config == null) {
            logger.debug("addDiscoveryResults: skipping null config");
            return;
        }

        for (Device device : config.getDevices()) {
            String label = device.getLabel();
            int id = device.getId();

            ThingUID bridgeUID = bridge.getThing().getUID();
            ThingUID thingUID = new ThingUID(HARMONY_DEVICE_THING_TYPE, bridgeUID, String.valueOf(id));

            // @formatter:off
            thingDiscovered(DiscoveryResultBuilder.create(thingUID)
                    .withLabel(label)
                    .withBridge(bridgeUID)
                    .withProperty(DEVICE_PROPERTY_ID, id)
                    .withProperty(DEVICE_PROPERTY_NAME, label)
                    .build());
            // @formatter:on
        }
    }
}
