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
package org.openhab.binding.clearone.internal;

import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.clearone.internal.config.ZoneConfiguration;
import org.openhab.binding.clearone.internal.handler.ClearOneUnitHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;

/**
 * This class is responsible for discovering zones via the bridge.
 *
 * @author Garry Mitchell - Initial Contribution
 *
 */
public class ClearOneUnitDiscoveryService extends AbstractDiscoveryService {

    /**
     * Bridge handler.
     */
    ClearOneUnitHandler bridgeHandler;

    /**
     * Constructor.
     *
     * @param ampHandler
     */
    public ClearOneUnitDiscoveryService(ClearOneUnitHandler stackHandler) {
        super(ClearOneBindingConstants.SUPPORTED_THING_TYPES_UIDS, 15, true);
        this.bridgeHandler = stackHandler;
    }

    /**
     * Activates the Discovery Service.
     */
    public void activate() {
        bridgeHandler.registerDiscoveryService(this);
    }

    /**
     * Deactivates the Discovery Service.
     */
    @Override
    public void deactivate() {
        bridgeHandler.unregisterDiscoveryService();
    }

    /**
     * Method to add a Zone to the Smarthome Inbox.
     *
     * @param bridge
     * @param zone
     */
    public void addZone(Bridge bridge, String zone, String name) {
        ThingUID thingUID = null;
        String thingID = "";
        String thingLabel = "";
        Map<String, Object> properties = null;

        thingID = "zone" + String.valueOf(zone);
        if (name.equals("")) {
            thingLabel = "Zone " + String.valueOf(zone);
        } else {
            thingLabel = name;
        }

        properties = new HashMap<>(0);
        thingUID = new ThingUID(ClearOneBindingConstants.THING_TYPE_ZONE, bridge.getUID(), thingID);
        properties.put(ZoneConfiguration.ZONE_NUMBER, zone);

        DiscoveryResult discoveryResult;

        discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties).withBridge(bridge.getUID())
                .withLabel(thingLabel).build();

        thingDiscovered(discoveryResult);
    }

    @Override
    protected void startScan() {
        // Can be ignored here as discovery is via the bridge
        for (int i = 1; i <= 12; i++) {
            bridgeHandler.sendCommand(ClearOneBindingConstants.XAP_CMD_LABEL, String.format("%d O", i));
        }
    }
}
