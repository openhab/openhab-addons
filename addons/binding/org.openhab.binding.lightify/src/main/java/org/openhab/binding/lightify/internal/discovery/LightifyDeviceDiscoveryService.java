/*
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lightify.internal.discovery;

import com.noctarius.lightify.LightifyLink;
import com.noctarius.lightify.model.Device;
import com.noctarius.lightify.model.Zone;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.lightify.handler.DeviceHandler;
import org.openhab.binding.lightify.handler.GatewayHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.noctarius.lightify.protocol.LightifyUtils.exceptional;
import static org.openhab.binding.lightify.LightifyConstants.PROPERTY_DEVICE_ADDRESS;
import static org.openhab.binding.lightify.LightifyConstants.PROPERTY_DEVICE_NAME;
import static org.openhab.binding.lightify.LightifyConstants.PROPERTY_ID;
import static org.openhab.binding.lightify.LightifyConstants.PROPERTY_ZONE_ID;
import static org.openhab.binding.lightify.internal.LightifyHandlerFactory.getThingTypeUID;

/**
 * The {@link org.eclipse.smarthome.config.discovery.DiscoveryService} implementation used by the
 * {@link GatewayHandler} to auto-discover devices and zones configured with the corresponding
 * Lightify gateway.
 *
 * @author Christoph Engelbert (@noctarius2k) - Initial contribution
 */
public class LightifyDeviceDiscoveryService extends AbstractDiscoveryService implements Consumer<Device> {

    private final Logger logger = LoggerFactory.getLogger(LightifyDeviceDiscoveryService.class);

    private final static int SEARCH_TIME = 60;

    private final GatewayHandler gatewayHandler;

    public LightifyDeviceDiscoveryService(GatewayHandler gatewayHandler) throws IllegalArgumentException {
        super(DeviceHandler.SUPPORTED_TYPES, SEARCH_TIME, false);
        this.gatewayHandler = gatewayHandler;
    }

    @Override
    protected void startScan() {
        exceptional(() -> {
            logger.info("Start scanning for paired devices");
            LightifyLink lightifyLink = gatewayHandler.getLightifyLink();
            if (lightifyLink != null) {
                lightifyLink.performSearch(this);
            }
        });
    }

    @Override
    public void accept(Device device) {
        try {
            logger.debug("Found device: {}", device);
            DiscoveryResult discoveryResult = discoveryResult(device);
            logger.debug("Thing discovered: {}", discoveryResult);
            thingDiscovered(discoveryResult);
        } catch (Exception e) {
            logger.error("Error while discovering", e);
        }
    }

    public void activate() {
    }

    public void deactivate() {
    }

    /**
     * Returns a {@link DiscoveryResult} based on the type of the {@link com.noctarius.lightify.model.Luminary}
     * passed. The discovery result instance contains properties like the device name as
     * known to the Lightify app, the internal address of the bulb or zone and the unique
     * device id generated as hex representation of the internal address.
     *
     * @param device the discovered device
     * @return the DiscoveryResult instance to represent the Lightify device
     */
    private DiscoveryResult discoveryResult(Device device) {
        ThingTypeUID thingTypeUID = getThingTypeUID(device);
        String deviceName = getDeviceName(device);

        String deviceAddress = getDeviceAddress(device);
        ThingUID bridgeUID = gatewayHandler.getThing().getUID();
        ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, deviceAddress);

        Map<String, Object> properties = new HashMap<>();
        properties.put(PROPERTY_ID, thingUID.getId());
        properties.put(PROPERTY_DEVICE_NAME, deviceName);
        properties.put(PROPERTY_DEVICE_ADDRESS, device.getAddress());

        if (device instanceof Zone) {
            properties.put(PROPERTY_ZONE_ID, ((Zone) device).getZoneId());
        }

        return DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID).withLabel(deviceName)
                                     .withThingType(thingTypeUID).withRepresentationProperty(PROPERTY_DEVICE_NAME)
                                     .withProperties(properties).build();
    }

    private String getDeviceName(Device device) {
        return device.getName();
    }

    private String getDeviceAddress(Device device) {
        return device.getAddress().toAddressCode();
    }
}
