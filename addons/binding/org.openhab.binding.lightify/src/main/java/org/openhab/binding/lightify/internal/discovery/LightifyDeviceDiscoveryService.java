/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lightify.internal.discovery;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.lightify.handler.DeviceHandler;
import org.openhab.binding.lightify.handler.GatewayHandler;
import org.openhab.binding.lightify.internal.link.LightifyLight;
import org.openhab.binding.lightify.internal.link.LightifyLink;
import org.openhab.binding.lightify.internal.link.LightifyLuminary;
import org.openhab.binding.lightify.internal.link.LightifyZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.openhab.binding.lightify.internal.LightifyConstants.PROPERTY_DEVICE_ADDRESS;
import static org.openhab.binding.lightify.internal.LightifyConstants.PROPERTY_DEVICE_NAME;
import static org.openhab.binding.lightify.internal.LightifyConstants.PROPERTY_ID;
import static org.openhab.binding.lightify.internal.LightifyConstants.PROPERTY_ZONE_ID;
import static org.openhab.binding.lightify.internal.LightifyConstants.THING_TYPE_LIGHTIFY_BULB;
import static org.openhab.binding.lightify.internal.LightifyConstants.THING_TYPE_LIGHTIFY_ZONE;
import static org.openhab.binding.lightify.internal.LightifyUtils.exceptional;

/**
 * @author Christoph Engelbert (@noctarius2k) - Initial contribution
 */
public class LightifyDeviceDiscoveryService extends AbstractDiscoveryService implements Consumer<LightifyLuminary> {

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
            logger.debug("Start scanning for paired devices");
            LightifyLink lightifyLink = gatewayHandler.getLightifyLink();
            lightifyLink.performSearch(this);
        });
    }

    @Override
    public void accept(LightifyLuminary luminary) {
        exceptional(() -> {
            logger.debug("Found device: {}", luminary);
            DiscoveryResult discoveryResult = discoveryResult(luminary);
            thingDiscovered(discoveryResult);
        });
    }

    public void activate() {
    }

    public void deactivate() {
    }

    /**
     * Returns a {@link DiscoveryResult} based on the type of the {@link LightifyLuminary}
     * passed. The discovery result instance contains properties like the device name as
     * known to the Lightify app, the internal address of the bulb or zone and the unique
     * device id generated as hex representation of the internal address.
     *
     * @param luminary the discovered luminary device
     * @return the DiscoveryResult instance to represent the Lightify device to OpenHAB2
     */
    private DiscoveryResult discoveryResult(LightifyLuminary luminary) {
        ThingTypeUID thingTypeUID = getThingTypeUID(luminary);
        String deviceName = getDeviceName(luminary);

        String deviceAddress = getDeviceAddress(luminary);
        ThingUID bridgeUID = gatewayHandler.getThing().getUID();
        ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, deviceAddress);

        Map<String, Object> properties = new HashMap<>();
        properties.put(PROPERTY_ID, thingUID.getId());
        properties.put(PROPERTY_DEVICE_NAME, deviceName);
        properties.put(PROPERTY_DEVICE_ADDRESS, luminary.address());

        if (luminary instanceof LightifyZone) {
            properties.put(PROPERTY_ZONE_ID, ((LightifyZone) luminary).getZoneId());
        }

        return DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID).withLabel(luminary.getName())
                                     .withThingType(thingTypeUID).withRepresentationProperty(PROPERTY_DEVICE_NAME)
                                     .withProperties(properties).build();
    }

    private String getDeviceName(LightifyLuminary luminary) {
        return luminary.getName();
    }

    private String getDeviceAddress(LightifyLuminary luminary) {
        return DatatypeConverter.printHexBinary(luminary.address());
    }

    private ThingTypeUID getThingTypeUID(LightifyLuminary luminary) {
        return luminary instanceof LightifyLight ? THING_TYPE_LIGHTIFY_BULB : THING_TYPE_LIGHTIFY_ZONE;
    }
}
