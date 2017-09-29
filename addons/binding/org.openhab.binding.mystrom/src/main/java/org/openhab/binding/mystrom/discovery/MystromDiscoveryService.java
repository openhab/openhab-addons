/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mystrom.discovery;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.mystrom.MystromBindingConstants;
import org.openhab.binding.mystrom.handler.MystromBridgeHandler;
import org.openhab.binding.mystrom.internal.Device;
import org.openhab.binding.mystrom.internal.MystromDeviceAddedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mystrom DiscoveryService.
 *
 * @author Stéphane Raemy - Initial contribution
 */
public class MystromDiscoveryService extends AbstractDiscoveryService implements MystromDeviceAddedListener {
    private final Logger logger = LoggerFactory.getLogger(MystromDiscoveryService.class);

    private MystromBridgeHandler bridge;

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections
            .singleton(MystromBindingConstants.THING_TYPE_WIFISWITCH);

    private static final int DISCOVERY_TIME_SECONDS = 30;

    public MystromDiscoveryService(MystromBridgeHandler bridge) throws IllegalArgumentException {
        super(SUPPORTED_THING_TYPES, DISCOVERY_TIME_SECONDS, true);
        this.bridge = bridge;
    }

    public void activate() {
        logger.debug("MystromDiscoveryService activate");
        bridge.addDeviceAddedListener(this);
    }

    @Override
    public void deactivate() {
        logger.debug("MystromDiscoveryService activate");
        bridge.removeDeviceAddedListener(this);
    }

    @Override
    protected void startScan() {
        logger.debug("MystromDiscoveryService startScan");
        this.bridge.startDiscoveryScan();
    }

    @Override
    public void onWifiSwitchAdded(Device device) {
        logger.debug("onWifiSwitchAdded called for device id:{}", device.id);
        ThingUID bridgeUID = bridge.getThing().getUID();
        ThingUID thingUID = new ThingUID(MystromBindingConstants.THING_TYPE_WIFISWITCH, bridgeUID, device.id);
        Map<String, Object> properties = new HashMap<>(1);
        properties.put("id", device.id);
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                .withProperties(properties).withLabel(device.name).build();
        thingDiscovered(discoveryResult);
        logger.debug("thingDiscovered called for device");
    }

}
