/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.evohome.discovery;

/**
 * The {@link EvohomeDiscoveryService} class is capable of discovering the available data from Evohome
 *
 * @author Neil Renaud - Initial contribution
 */

/*
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
                EvohomeApiClient client = evohomeBridgeHandler.getApiClient();
                // TODO Maybe client.update()
                if (client != null) {

                    for (ControlSystem gateway : client.getControlSystems()) {
                        discoverGateway(gateway);
                    }

                    DataModelResponse[] dataArray = client.getData();
                    for (DataModelResponse data : dataArray) {
                        discoverWeather(data.getWeather(), data.getName(), data.getLocationId());
                        discoverRadiatorValves(data.getDevices(), data.getName(), data.getLocationId());
                    }
                }
            } catch (Exception e) {
                logger.warn("{}", e.getMessage(), e);
            }
        }
        stopScan();
    }

    private void discoverGateway(ControlSystem controlSystem) {
        String name = controlSystem.getName();
        ThingUID thingUID = findThingUID(THING_TYPE_EVOHOME_DISPLAY.getId(), name);
        Map<String, Object> properties = new HashMap<>();
        properties.put(EvohomeBindingConstants.LOCATION_NAME, name);
        properties.put(EvohomeBindingConstants.LOCATION_ID, controlSystem.getId());
        addDiscoveredThing(thingUID, properties, name);
    }

    private void discoverWeather(Weather weather, String name, String locationId) throws IllegalArgumentException {
        ThingUID thingUID = findThingUID(THING_TYPE_EVOHOME_LOCATION.getId(), name);
        Map<String, Object> properties = new HashMap<>();
        properties.put(EvohomeBindingConstants.LOCATION_NAME, name);
        properties.put(EvohomeBindingConstants.LOCATION_ID, locationId);
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
*/