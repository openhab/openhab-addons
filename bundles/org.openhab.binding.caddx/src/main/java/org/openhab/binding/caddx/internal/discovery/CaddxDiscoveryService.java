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
package org.openhab.binding.caddx.internal.discovery;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.openhab.binding.caddx.internal.CaddxBindingConstants;
import org.openhab.binding.caddx.internal.CaddxEvent;
import org.openhab.binding.caddx.internal.CaddxProtocol;
import org.openhab.binding.caddx.internal.config.CaddxBridgeConfiguration;
import org.openhab.binding.caddx.internal.config.CaddxKeypadConfiguration;
import org.openhab.binding.caddx.internal.config.CaddxPartitionConfiguration;
import org.openhab.binding.caddx.internal.config.CaddxZoneConfiguration;
import org.openhab.binding.caddx.internal.handler.CaddxBridgeHandler;
import org.openhab.binding.caddx.internal.handler.CaddxThingType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for discovering the supported Things.
 *
 * @author Georgios Moutsos - Initial contribution
 */
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.caddx")
@NonNullByDefault
public class CaddxDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(CaddxDiscoveryService.class);

    private @NonNullByDefault({}) SerialPortManager portManager;
    private CaddxBridgeDiscovery caddxBridgeDiscovery = new CaddxBridgeDiscovery(portManager, this);
    private @Nullable CaddxBridgeHandler caddxBridgeHandler;

    /**
     * Constructor.
     *
     * @param caddxBridgeHandler The Bridge handler
     */
    public CaddxDiscoveryService(CaddxBridgeHandler caddxBridgeHandler) {
        super(CaddxBindingConstants.SUPPORTED_THING_TYPES_UIDS, 15, true);
        this.caddxBridgeHandler = caddxBridgeHandler;
    }

    @Override
    protected void startScan() {
        logger.trace("Start Caddx Bridge discovery.");

        if (portManager != null) {
            caddxBridgeDiscovery = new CaddxBridgeDiscovery(portManager, this);
            caddxBridgeDiscovery.discoverBridge();
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
    }

    /**
     * Method to add a Caddx Bridge to the Smarthome Inbox.
     *
     * @param port
     */
    public void addCaddxBridge(CaddxProtocol protocol, String port, int baudrate) {
        logger.trace("addCaddxBridge(): Adding new Caddx Bridge on {} {} to Smarthome inbox", port, baudrate);

        String bridgeID = "";
        boolean containsChar = port.contains("/");

        if (containsChar) {
            String[] parts = port.split("/");
            String id = parts[parts.length - 1].toUpperCase();
            bridgeID = id.replaceAll("\\W", "_");
        } else {
            String id = port.toUpperCase();
            bridgeID = id.replaceAll("\\W", "_");
        }

        Map<String, Object> properties = new HashMap<>(3);
        properties.put(CaddxBridgeConfiguration.PROTOCOL, protocol);
        properties.put(CaddxBridgeConfiguration.SERIAL_PORT, port);
        properties.put(CaddxBridgeConfiguration.BAUD, baudrate);

        try {
            ThingUID thingUID = new ThingUID(CaddxBindingConstants.CADDXBRIDGE_THING_TYPE, bridgeID);

            thingDiscovered(DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withLabel("Caddx Bridge - " + port).build());
        } catch (Exception e) {
            logger.warn("addBridge(): ", e);
        }
    }

    /**
     * Method to add a Thing to the Smarthome Inbox.
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

        switch (caddxThingType) {
            case PANEL:
                thingID = "panel";
                thingLabel = "Panel";
                thingUID = new ThingUID(CaddxBindingConstants.PANEL_THING_TYPE, bridge.getUID(), thingID);
                break;
            case PARTITION:
                thingID = "partition" + String.valueOf(partition);
                thingLabel = "Partition " + String.valueOf(partition);
                thingUID = new ThingUID(CaddxBindingConstants.PARTITION_THING_TYPE, bridge.getUID(), thingID);

                properties = new HashMap<>(1);
                if (partition != null) {
                    properties.put(CaddxPartitionConfiguration.PARTITION_NUMBER, partition);
                }
                break;
            case ZONE:
                thingID = "zone" + String.valueOf(zone);
                thingLabel = "Zone " + String.valueOf(zone);
                thingUID = new ThingUID(CaddxBindingConstants.ZONE_THING_TYPE, bridge.getUID(), thingID);

                properties = new HashMap<>(1);
                if (zone != null) {
                    properties.put(CaddxZoneConfiguration.ZONE_NUMBER, zone);
                }
                break;
            case KEYPAD:
                thingID = "keypad";
                thingLabel = "Keypad";
                thingUID = new ThingUID(CaddxBindingConstants.KEYPAD_THING_TYPE, bridge.getUID(), thingID);

                properties = new HashMap<>(1);
                if (keypad != null) {
                    properties.put(CaddxKeypadConfiguration.KEYPAD_ADDRESS, keypad);
                }
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
            logger.warn("addThing(): Unable to Add Caddx Alarm Thing to Inbox!");
        }
    }

    /**
     * Activates the Discovery Service.
     */
    public void activate() {
        CaddxBridgeHandler h = caddxBridgeHandler;
        if (h != null) {
            h.registerDiscoveryService(this);
        }
    }

    /**
     * Deactivates the Discovery Service.
     */
    @Override
    public void deactivate() {
        CaddxBridgeHandler h = caddxBridgeHandler;
        if (h != null) {
            h.unregisterDiscoveryService();
        }
    }

    @Reference
    protected void setSerialPortManager(final SerialPortManager serialPortManager) {
        this.portManager = serialPortManager;
    }

    protected void unsetSerialPortManager(final SerialPortManager serialPortManager) {
        this.portManager = null;
    }
}
