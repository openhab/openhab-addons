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

import org.openhab.binding.clearone.internal.config.UnitConfiguration;
import org.openhab.binding.clearone.internal.handler.ClearOneStackHandler;
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
public class ClearOneStackDiscoveryService extends AbstractDiscoveryService {
    /**
     * Bridge handler.
     */
    ClearOneStackHandler bridgeHandler;

    /**
     * Constructor.
     *
     * @param ampHandler
     */
    public ClearOneStackDiscoveryService(ClearOneStackHandler stackHandler) {
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
     * Method to add a Unit to the Smarthome Inbox.
     *
     * @param bridge
     * @param typeId
     * @param unitId
     * @param uid
     */
    public void addUnit(Bridge bridge, String typeId, String unitId, String name) {
        ThingUID thingUID = null;
        String thingID = "";
        String thingLabel = "";
        Map<String, Object> properties = null;

        thingID = "unit" + String.valueOf(unitId);
        if (name.equals("")) {
            thingLabel = "Unit " + String.valueOf(unitId);
        } else {
            thingLabel = name;
        }

        properties = new HashMap<>(0);
        thingUID = new ThingUID(ClearOneBindingConstants.THING_TYPE_UNIT, bridge.getUID(), thingID);
        properties.put(UnitConfiguration.UNIT_TYPE, typeId);
        properties.put(UnitConfiguration.UNIT_NUMBER, unitId);

        DiscoveryResult discoveryResult;

        discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties).withBridge(bridge.getUID())
                .withLabel(thingLabel).build();

        thingDiscovered(discoveryResult);
    }

    @Override
    protected void startScan() {
        bridgeHandler.sendCommand("#** LABEL 0 U\r");
    }
}
