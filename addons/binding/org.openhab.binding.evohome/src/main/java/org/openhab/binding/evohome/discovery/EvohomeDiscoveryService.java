/**
  * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.evohome.discovery;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.evohome.EvohomeBindingConstants;
import org.openhab.binding.evohome.handler.EvohomeGatewayHandler;
import org.openhab.binding.evohome.internal.api.EvohomeApiClient;
import org.openhab.binding.evohome.internal.api.models.ControlSystem;
import org.openhab.binding.evohome.internal.api.models.v1.Weather;
import org.openhab.binding.evohome.internal.api.models.v2.response.HeatSetpointCapabilities;
import org.openhab.binding.evohome.internal.api.models.v2.response.ScheduleCapabilities;
import org.openhab.binding.evohome.internal.api.models.v2.response.TemperatureControlSystem;
import org.openhab.binding.evohome.internal.api.models.v2.response.Zone;
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

    private EvohomeGatewayHandler evohomeGatewayHandler;

    public EvohomeDiscoveryService(EvohomeGatewayHandler evohomeBridgeHandler) {
        super(EvohomeBindingConstants.SUPPORTED_THING_TYPES_UIDS, SEARCH_TIME);
        this.evohomeGatewayHandler = evohomeBridgeHandler;
    }

    @Override
    public void startScan() {
        logger.debug("Evohome start scan");

        if (evohomeGatewayHandler != null) {
            try {
                EvohomeApiClient client = evohomeGatewayHandler.getApiClient();
                if (client != null) {

                    for (ControlSystem controlSystem : client.getControlSystems()) {
                        discoverDisplay(controlSystem);
                        discoverHeatingZones(controlSystem.getId(), controlSystem.getHeatingZones());
                    }
//                    DataModelResponse[] dataArray = client.getData();
//                    for (DataModelResponse data : dataArray) {
//                        discoverWeather(data.getWeather(), data.getName(), data.getLocationId());
//                    }
                }
            } catch (Exception e) {
                logger.warn("{}", e.getMessage(), e);
            }
        }

        stopScan();
    }

    private void discoverHeatingZones(int locationId, TemperatureControlSystem heatingZones) {
        for(Zone zone : heatingZones.zones){
            String zoneName = zone.name;
            long zoneId = zone.zoneId;
            String modelType = zone.modelType;
            String zoneType = zone.zoneType;
            HeatSetpointCapabilities heatSetpointCapabilities = zone.heatSetpointCapabilities;
            ScheduleCapabilities scheduleCapabilities = zone.scheduleCapabilities;
            ThingUID thingUID = findThingUID(EvohomeBindingConstants.THING_TYPE_EVOHOME_HEATING_ZONE.getId(), zoneName);
            Map<String, Object> properties = new HashMap<>();
            properties.put(EvohomeBindingConstants.LOCATION_ID, locationId);
            properties.put(EvohomeBindingConstants.ZONE_ID, zoneId);
            properties.put(EvohomeBindingConstants.ZONE_NAME, zoneName);
            properties.put(EvohomeBindingConstants.ZONE_TYPE, zoneType);
            properties.put(EvohomeBindingConstants.ZONE_MODEL_TYPE, modelType);
            addDiscoveredThing(thingUID, properties, zoneName);
        }
    }

    private void discoverDisplay(ControlSystem controlSystem) {
        String name = controlSystem.getName();
        ThingUID thingUID = findThingUID(EvohomeBindingConstants.THING_TYPE_EVOHOME_DISPLAY.getId(), name);
        Map<String, Object> properties = new HashMap<>();
        properties.put(EvohomeBindingConstants.DEVICE_NAME, name);
        properties.put(EvohomeBindingConstants.DEVICE_ID, controlSystem.getId());
        addDiscoveredThing(thingUID, properties, name);
    }

    private void discoverWeather(Weather weather, String name, String locationId) throws IllegalArgumentException {
        ThingUID thingUID = findThingUID(EvohomeBindingConstants.THING_TYPE_EVOHOME_LOCATION.getId(), name);
        Map<String, Object> properties = new HashMap<>();
        properties.put(EvohomeBindingConstants.LOCATION_NAME, name);
        properties.put(EvohomeBindingConstants.LOCATION_ID, locationId);
        addDiscoveredThing(thingUID, properties, name);
    }
/*
    private void discoverRadiatorValves(Device[] devices, String locationName, String locationId)
            throws IllegalArgumentException {
        for (Device device : devices) {
            ThingUID thingUID = findThingUID(EvohomeBindingConstants.THING_TYPE_EVOHOME_RADIATOR_VALVE.getId(),
                    Integer.toString(device.getDeviceId()));
            String name = device.getName();
            logger.debug("found Valve device_name:{} device_id:{} location_name:{} location_id:{}", name,
                    device.getDeviceId(), locationName, locationId);

            Map<String, Object> properties = new HashMap<>();
            properties.put(EvohomeBindingConstants.DEVICE_ID, device.getDeviceId());
            properties.put(EvohomeBindingConstants.DEVICE_NAME, device.getName());
            properties.put(EvohomeBindingConstants.LOCATION_ID, locationId);
            properties.put(EvohomeBindingConstants.LOCATION_NAME, locationName);
            addDiscoveredThing(thingUID, properties, name);
        }
    }*/

    private void addDiscoveredThing(ThingUID thingUID, Map<String, Object> properties, String displayLabel) {
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withBridge(evohomeGatewayHandler.getThing().getUID()).withLabel(displayLabel).build();

        thingDiscovered(discoveryResult);
    }

    private ThingUID findThingUID(String thingType, String thingId) throws IllegalArgumentException {
        for (ThingTypeUID supportedThingTypeUID : getSupportedThingTypes()) {
            String uid = supportedThingTypeUID.getId();

            if (uid.equalsIgnoreCase(thingType)) {

                return new ThingUID(supportedThingTypeUID, evohomeGatewayHandler.getThing().getUID(),
                        thingId.replaceAll("[^a-zA-Z0-9_]", ""));
            }
        }

        throw new IllegalArgumentException("Unsupported device type discovered: " + thingType);
    }
}
