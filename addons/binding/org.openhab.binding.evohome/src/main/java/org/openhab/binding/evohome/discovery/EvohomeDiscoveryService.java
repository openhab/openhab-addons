/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.evohome.discovery;

import static org.openhab.binding.evohome.EvohomeBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.evohome.handler.EvohomeGatewayHandler;
import org.openhab.binding.evohome.internal.api.EvohomeApiClient;
import org.openhab.binding.evohome.internal.api.models.Gateway;
import org.openhab.binding.evohome.internal.api.models.v1.DataModelResponse;
import org.openhab.binding.evohome.internal.api.models.v1.Device;
import org.openhab.binding.evohome.internal.api.models.v1.Weather;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EvohomeDiscoveryService} class is capable of discovering the available data from Evohome
 *
 * @author Neil Renaud - Initial contribution
 */
public class EvohomeDiscoveryService extends AbstractDiscoveryService {
    private Logger logger = LoggerFactory.getLogger(EvohomeDiscoveryService.class);
    private static final int SEARCH_TIME = 2;
    private static final String LOCATION_NAME = "Location Name";
    private static final String LOCATION_ID = "Location Id";
    private static final String DEVICE_NAME = "Device Name";
    private static final String DEVICE_ID = "Device Id";

    private EvohomeGatewayHandler evohomeBridgeHandler;

    public EvohomeDiscoveryService(EvohomeGatewayHandler evohomeBridgeHandler) {
        super(SUPPORTED_THING_TYPES_UIDS, SEARCH_TIME);
        this.evohomeBridgeHandler = evohomeBridgeHandler;
    }

    @Override
    public void startScan() {
        logger.debug("Evohome start scan");
        if (evohomeBridgeHandler != null) {
            try {
                EvohomeApiClient client = evohomeBridgeHandler.getClient();
                for (Gateway gateway : client.getGateways()) {
                    discoverGateway(gateway);
                }

                DataModelResponse[] dataArray = evohomeBridgeHandler.getData();
                for (DataModelResponse data : dataArray) {
                    discoverWeather(data.getWeather(), data.getName(), data.getLocationId());
                    discoverRadiatorValves(data.getDevices(), data.getName(), data.getLocationId());
                }
            } catch (Exception e) {
                logger.warn("{}", e.getMessage(), e);
            }
        }
        stopScan();
    }

    private void discoverGateway(Gateway gateway) {
        String name = gateway.getName();
        ThingUID thingUID = findThingUID(THING_TYPE_EVOHOME_SYSTEM.getId(), name);
        Map<String, Object> properties = new HashMap<>();
        properties.put("LOCATION_NAME", name);
        properties.put("LOCATION_ID", gateway.getId());
        addDiscoveredThing(thingUID, properties, name);
    }

    private void discoverWeather(Weather weather, String name, String locationId) throws IllegalArgumentException {
        ThingUID thingUID = findThingUID(THING_TYPE_EVOHOME_LOCATION.getId(), name);
        Map<String, Object> properties = new HashMap<>();
        properties.put("LOCATION_NAME", name);
        properties.put("LOCATION_ID", locationId);
        addDiscoveredThing(thingUID, properties, name);
    }

    private void discoverRadiatorValves(Device[] devices, String locationName, String locationId)
            throws IllegalArgumentException {
        for (Device device : devices) {
            ThingUID thingUID = findThingUID(THING_TYPE_EVOHOME_RADIATOR_VALVE.getId(),
                    Integer.toString(device.getDeviceId()));
            String name = device.getName();
            logger.debug("found Valve device_name:{} device_id:{} location_name:{} location_id:{}", name,
                    device.getDeviceId(), locationName, locationId);

            Map<String, Object> properties = new HashMap<>();
            properties.put(DEVICE_ID, device.getDeviceId());
            properties.put(DEVICE_NAME, device.getName());
            properties.put(LOCATION_ID, locationId);
            properties.put(LOCATION_NAME, locationName);
            addDiscoveredThing(thingUID, properties, name);
        }
    }

    private void addDiscoveredThing(ThingUID thingUID, Map<String, Object> properties, String displayLabel) {
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withBridge(evohomeBridgeHandler.getThing().getUID()).withLabel(displayLabel).build();

        thingDiscovered(discoveryResult);
    }

    private ThingUID findThingUID(String thingType, String thingId) throws IllegalArgumentException {
        for (ThingTypeUID supportedThingTypeUID : getSupportedThingTypes()) {
            String uid = supportedThingTypeUID.getId();

            if (uid.equalsIgnoreCase(thingType)) {

                return new ThingUID(supportedThingTypeUID, evohomeBridgeHandler.getThing().getUID(),
                        thingId.replaceAll("[^a-zA-Z0-9_]", ""));
            }
        }

        throw new IllegalArgumentException("Unsupported device type discovered: " + thingType);
    }
}
