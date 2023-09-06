/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.caddx.internal.discovery;

import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.caddx.internal.CaddxBindingConstants;
import org.openhab.binding.caddx.internal.CaddxEvent;
import org.openhab.binding.caddx.internal.config.CaddxKeypadConfiguration;
import org.openhab.binding.caddx.internal.config.CaddxPartitionConfiguration;
import org.openhab.binding.caddx.internal.config.CaddxZoneConfiguration;
import org.openhab.binding.caddx.internal.handler.CaddxBridgeHandler;
import org.openhab.binding.caddx.internal.handler.CaddxThingType;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for discovering the supported Things.
 *
 * @author Georgios Moutsos - Initial contribution
 */
@NonNullByDefault
public class CaddxDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService, DiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(CaddxDiscoveryService.class);

    private @Nullable CaddxBridgeHandler caddxBridgeHandler = null;

    public CaddxDiscoveryService() {
        super(CaddxBindingConstants.SUPPORTED_THING_TYPES_UIDS, 15, false);
    }

    @Override
    protected void startScan() {
        // Discovery is performed implicitly via the CadxBridgeHandler
    }

    /**
     * Method to add a Thing to the Inbox.
     *
     * @param bridge
     * @param caddxThingType
     * @param event
     */
    public void addThing(Bridge bridge, CaddxThingType caddxThingType, CaddxEvent event) {
        ThingUID thingUID = null;
        String thingID = "";
        String thingLabel = "";
        Map<String, Object> properties = null;

        Integer partition = event.getPartition();
        Integer zone = event.getZone();
        Integer keypad = event.getKeypad();
        String representationProperty = null;

        switch (caddxThingType) {
            case PANEL:
                thingID = "panel";
                thingLabel = "Panel";
                thingUID = new ThingUID(CaddxBindingConstants.PANEL_THING_TYPE, bridge.getUID(), thingID);
                break;
            case PARTITION:
                thingID = "partition" + partition;
                thingLabel = "Partition " + partition;
                thingUID = new ThingUID(CaddxBindingConstants.PARTITION_THING_TYPE, bridge.getUID(), thingID);

                if (partition != null) {
                    properties = Collections.singletonMap(CaddxPartitionConfiguration.PARTITION_NUMBER, partition);
                    representationProperty = CaddxPartitionConfiguration.PARTITION_NUMBER;
                }

                break;
            case ZONE:
                thingID = "zone" + zone;
                thingLabel = "Zone " + zone;
                thingUID = new ThingUID(CaddxBindingConstants.ZONE_THING_TYPE, bridge.getUID(), thingID);

                if (zone != null) {
                    properties = Collections.singletonMap(CaddxZoneConfiguration.ZONE_NUMBER, zone);
                    representationProperty = CaddxZoneConfiguration.ZONE_NUMBER;
                }
                break;
            case KEYPAD:
                thingID = "keypad";
                thingLabel = "Keypad";
                thingUID = new ThingUID(CaddxBindingConstants.KEYPAD_THING_TYPE, bridge.getUID(), thingID);

                if (keypad != null) {
                    properties = Collections.singletonMap(CaddxKeypadConfiguration.KEYPAD_ADDRESS, keypad);
                    representationProperty = CaddxKeypadConfiguration.KEYPAD_ADDRESS;
                }
                break;
        }

        if (thingUID != null) {
            DiscoveryResult discoveryResult;

            if (properties != null && representationProperty != null) {
                discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                        .withRepresentationProperty(representationProperty).withBridge(bridge.getUID())
                        .withLabel(thingLabel).build();
            } else {
                discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridge.getUID())
                        .withLabel(thingLabel).build();
            }

            thingDiscovered(discoveryResult);
        } else {
            logger.warn("addThing(): Unable to Add Caddx Alarm Thing to Inbox!");
        }
    }

    /**
     * Activates the Discovery Service.
     */
    @Override
    public void activate() {
        CaddxBridgeHandler handler = caddxBridgeHandler;
        if (handler != null) {
            handler.registerDiscoveryService(this);
        }
    }

    /**
     * Deactivates the Discovery Service.
     */
    @Override
    public void deactivate() {
        CaddxBridgeHandler handler = caddxBridgeHandler;
        if (handler != null) {
            handler.unregisterDiscoveryService();
        }
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof CaddxBridgeHandler) {
            caddxBridgeHandler = (CaddxBridgeHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return caddxBridgeHandler;
    }
}
