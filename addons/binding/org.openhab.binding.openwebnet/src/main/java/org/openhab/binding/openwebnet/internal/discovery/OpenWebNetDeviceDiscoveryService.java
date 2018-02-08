/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openwebnet.internal.discovery;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.openwebnet.OpenWebNetBindingConstants;
import org.openhab.binding.openwebnet.handler.OpenWebNetAutomationHandler;
import org.openhab.binding.openwebnet.handler.OpenWebNetBridgeHandler;
import org.openhab.binding.openwebnet.handler.OpenWebNetLightingHandler;
import org.openhab.binding.openwebnet.internal.listener.ScanListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWebNetDeviceDiscoveryService} class discovers ZigBee OpenWebNet Devices connected to the bridge
 *
 * @author Antoine Laydier
 *
 */
@NonNullByDefault
public class OpenWebNetDeviceDiscoveryService extends AbstractDiscoveryService implements ScanListener {

    private static final int TIMEOUT = 180;

    @SuppressWarnings("null")
    private Logger logger = LoggerFactory.getLogger(OpenWebNetDeviceDiscoveryService.class);

    OpenWebNetBridgeHandler bridgeHandler;

    static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<ThingTypeUID>() {
        private static final long serialVersionUID = 1L;
        {
            addAll(OpenWebNetLightingHandler.SUPPORTED_THING_TYPES);
            addAll(OpenWebNetAutomationHandler.SUPPORTED_THING_TYPES);
        }
    };

    public OpenWebNetDeviceDiscoveryService(OpenWebNetBridgeHandler bridgeHandler) throws IllegalArgumentException {
        super(SUPPORTED_THING_TYPES, TIMEOUT, false);
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    protected void startScan() {
        logger.debug("Active Scan Started");
        this.bridgeHandler.scanNetwork(this);
    }

    @Override
    public void onScanError() {
        logger.warn("OpenWebNetDeviceDiscoveryService scan failed on {}", bridgeHandler);
        stopScan();
    }

    @Override
    public void onScanCompleted() {
        logger.debug("OpenWebNetDeviceDiscoveryService scan completed on {}", bridgeHandler);
        stopScan();
    }

    @Override
    public void onDeviceFound(int macAddress, String firmware, String hardware,
            Map<Integer, OpenWebNetChannel> channels) {
        if (channels.isEmpty()) {
            logger.warn("Device found  MAC = {}, FW={}, HW={} without any channel.", macAddress, firmware, hardware);
        } else {
            Map<String, Object> properties = new HashMap<>(5);

            String textMacAdress = String.valueOf(macAddress);
            if (textMacAdress != null) {
                properties.put(Thing.PROPERTY_MAC_ADDRESS, textMacAdress);
            }
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, firmware);
            properties.put(Thing.PROPERTY_HARDWARE_VERSION, hardware);

            Iterator<Integer> index = channels.keySet().iterator();
            OpenWebNetChannelType typeFirstChannel = channels.get(index.next()).getType();
            boolean isDual = index.hasNext();
            OpenWebNetChannelType typeSecondChannel = OpenWebNetChannelType.UNKNOWN;
            String label = "";
            String channelName = String.valueOf(typeFirstChannel.getValue());
            if (channelName != null) {
                properties.put(OpenWebNetBindingConstants.CHANNEL1, channelName);
            }
            if (isDual) {
                typeSecondChannel = channels.get(index.next()).getType();
                channelName = String.valueOf(typeSecondChannel.getValue());
                if (channelName != null) {
                    properties.put(OpenWebNetBindingConstants.CHANNEL2, channelName);
                }
                if (typeSecondChannel.equals(typeFirstChannel)) {
                    label = "Dual " + typeFirstChannel.toString();
                } else {
                    label = typeFirstChannel.toString() + " / " + typeSecondChannel.toString();
                }
            } else {
                label = typeFirstChannel.toString();
            }

            ThingUID bridgeUID = bridgeHandler.getThing().getUID();

            if (typeFirstChannel.isLighting()) {
                if (isDual) {
                    // dual lighting
                    ThingUID uid = new ThingUID(OpenWebNetBindingConstants.THING_TYPE_DUAL_LIGHTING,
                            Integer.toString(macAddress));
                    thingDiscovered(DiscoveryResultBuilder.create(uid).withProperties(properties).withBridge(bridgeUID)
                            .withLabel(label).withRepresentationProperty(String.valueOf(macAddress)).build());
                } else {
                    // simple lighting
                    ThingUID uid = new ThingUID(OpenWebNetBindingConstants.THING_TYPE_LIGHTING,
                            Integer.toString(macAddress));
                    thingDiscovered(DiscoveryResultBuilder.create(uid).withProperties(properties).withBridge(bridgeUID)
                            .withLabel(label).withRepresentationProperty(String.valueOf(macAddress)).build());
                }

            } else if (typeFirstChannel.isAutomation()) {
                // simple lighting
                ThingUID uid = new ThingUID(OpenWebNetBindingConstants.THING_TYPE_AUTOMATION,
                        Integer.toString(macAddress));
                thingDiscovered(DiscoveryResultBuilder.create(uid).withProperties(properties).withBridge(bridgeUID)
                        .withLabel(label).withRepresentationProperty(String.valueOf(macAddress)).build());
            } else {
                logger.warn("Not managed. MAC = {}, FW={}, HW={}, type={} ", macAddress, firmware, hardware,
                        typeFirstChannel);
                // Device not managed
            }
        }
    }
}
