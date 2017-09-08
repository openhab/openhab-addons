/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.osramlightify.internal.discovery;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.eclipse.smarthome.core.thing.Thing.PROPERTY_FIRMWARE_VERSION;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;

import static org.openhab.binding.osramlightify.LightifyBindingConstants.PROPERTY_IEEE_ADDRESS;
import static org.openhab.binding.osramlightify.LightifyBindingConstants.SUPPORTED_DEVICE_THING_TYPES_UIDS;

import org.openhab.binding.osramlightify.handler.LightifyBridgeHandler;
import org.openhab.binding.osramlightify.internal.LightifyDeviceState;
import org.openhab.binding.osramlightify.internal.messages.LightifyListPairedDevicesMessage;
import org.openhab.binding.osramlightify.internal.messages.LightifyListGroupsMessage;

/**
 * Auto-discovery support for devices paired with and groups configured on Lightify gateways.
 *
 * @author Mike Jagdis - Initial contribution
 */
public final class LightifyDeviceDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(LightifyDeviceDiscoveryService.class);

    // N.B. SCAN_INTERVAL is not really used as discovery scanning and state
    // polling are one and the same. In effect background discovery is always
    // enabled regardless of what the discovery service thinks.
    private final static int SCAN_INTERVAL = 300;

    private final LightifyBridgeHandler bridgeHandler;

    public LightifyDeviceDiscoveryService(LightifyBridgeHandler bridgeHandler) throws IllegalArgumentException {
        super(SUPPORTED_DEVICE_THING_TYPES_UIDS, SCAN_INTERVAL, false);
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    protected void startScan() {
        logger.trace("Start scanning for paired devices");
        bridgeHandler.sendMessage(new LightifyListGroupsMessage());
        bridgeHandler.sendMessage(new LightifyListPairedDevicesMessage());
    }

    public void scanComplete() {
        removeOlderResults(getTimestampOfLastScan());
    }

    public void removeResults() {
        removeOlderResults(new Date().getTime());
    }

    /**
     * Posts a discovery result for a Lightify device available via a gateway.
     *
     * @param device
     */
    public void discoveryResult(ThingUID thingUID, ThingTypeUID thingTypeUID, String deviceName, String deviceAddress, String firmwareVersion) {
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
            .withThingType(thingTypeUID)
            .withLabel(deviceName)
            .withBridge(bridgeUID)
//          .withRepresentationProperty(PROPERTY_DEVICE_NAME)
            .withProperty(PROPERTY_IEEE_ADDRESS, deviceAddress)
            .withProperty(PROPERTY_FIRMWARE_VERSION, firmwareVersion)
            .build();

        thingDiscovered(discoveryResult);
    }

    public void removeThing(ThingUID thingUID) {
        thingRemoved(thingUID);
    }
}
