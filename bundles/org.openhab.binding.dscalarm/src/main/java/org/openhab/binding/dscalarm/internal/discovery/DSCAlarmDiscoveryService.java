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
package org.openhab.binding.dscalarm.internal.discovery;

import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.dscalarm.internal.DSCAlarmBindingConstants;
import org.openhab.binding.dscalarm.internal.DSCAlarmEvent;
import org.openhab.binding.dscalarm.internal.DSCAlarmMessage.DSCAlarmMessageInfoType;
import org.openhab.binding.dscalarm.internal.config.DSCAlarmPartitionConfiguration;
import org.openhab.binding.dscalarm.internal.config.DSCAlarmZoneConfiguration;
import org.openhab.binding.dscalarm.internal.handler.DSCAlarmBaseBridgeHandler;
import org.openhab.binding.dscalarm.internal.handler.DSCAlarmThingType;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for discovering DSC Alarm Things via the bridge.
 *
 * @author Russell Stephens - Initial Contribution
 *
 */
public class DSCAlarmDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(DSCAlarmDiscoveryService.class);

    /**
     * DSC Alarm Bridge handler.
     */
    DSCAlarmBaseBridgeHandler dscAlarmBridgeHandler;

    /**
     * Constructor.
     *
     * @param dscAlarmBridgeHandler
     */
    public DSCAlarmDiscoveryService(DSCAlarmBaseBridgeHandler dscAlarmBridgeHandler) {
        super(DSCAlarmBindingConstants.SUPPORTED_THING_TYPES_UIDS, 15, true);
        this.dscAlarmBridgeHandler = dscAlarmBridgeHandler;
    }

    /**
     * Activates the Discovery Service.
     */
    public void activate() {
        dscAlarmBridgeHandler.registerDiscoveryService(this);
    }

    /**
     * Deactivates the Discovery Service.
     */
    @Override
    public void deactivate() {
        dscAlarmBridgeHandler.unregisterDiscoveryService();
    }

    /**
     * Method to add a Thing to the Inbox.
     *
     * @param bridge
     * @param dscAlarmThingType
     * @param event
     */
    public void addThing(Bridge bridge, DSCAlarmThingType dscAlarmThingType, DSCAlarmEvent event) {
        logger.trace("addThing(): Adding new DSC Alarm {} to the inbox", dscAlarmThingType.getLabel());

        ThingUID thingUID = null;
        String thingID = "";
        String thingLabel = "";
        Map<String, Object> properties = null;

        int partitionNumber = Integer
                .parseInt(event.getDSCAlarmMessage().getMessageInfo(DSCAlarmMessageInfoType.PARTITION));
        int zoneNumber = Integer.parseInt(event.getDSCAlarmMessage().getMessageInfo(DSCAlarmMessageInfoType.ZONE));

        switch (dscAlarmThingType) {
            case PANEL:
                thingID = "panel";
                thingLabel = "Panel";
                thingUID = new ThingUID(DSCAlarmBindingConstants.PANEL_THING_TYPE, bridge.getUID(), thingID);
                break;
            case PARTITION:
                if (partitionNumber >= 1 && partitionNumber <= 8) {
                    thingID = "partition" + partitionNumber;
                    thingLabel = "Partition " + partitionNumber;
                    properties = new HashMap<>(0);
                    thingUID = new ThingUID(DSCAlarmBindingConstants.PARTITION_THING_TYPE, bridge.getUID(), thingID);
                    properties.put(DSCAlarmPartitionConfiguration.PARTITION_NUMBER, partitionNumber);
                }

                break;
            case ZONE:
                if (zoneNumber >= 1 && zoneNumber <= 64) {
                    thingID = "zone" + zoneNumber;
                    thingLabel = "Zone " + zoneNumber;
                    properties = new HashMap<>(0);
                    thingUID = new ThingUID(DSCAlarmBindingConstants.ZONE_THING_TYPE, bridge.getUID(), thingID);
                    properties.put(DSCAlarmZoneConfiguration.ZONE_NUMBER, zoneNumber);
                }
                break;
            case KEYPAD:
                thingID = "keypad";
                thingLabel = "Keypad";
                thingUID = new ThingUID(DSCAlarmBindingConstants.KEYPAD_THING_TYPE, bridge.getUID(), thingID);
                break;
        }

        if (thingUID != null) {
            DiscoveryResult discoveryResult;

            if (properties != null) {
                discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                        .withBridge(bridge.getUID()).withLabel(thingLabel).build();
            } else {
                discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridge.getUID())
                        .withLabel(thingLabel).build();
            }

            thingDiscovered(discoveryResult);
        } else {
            logger.debug("addThing(): Unable to Add DSC Alarm Thing to Inbox!");
        }
    }

    @Override
    protected void startScan() {
        // Can be ignored here as discovery is via the bridge
    }
}
