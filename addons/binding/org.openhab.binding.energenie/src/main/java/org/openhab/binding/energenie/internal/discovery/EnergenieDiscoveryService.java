/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.energenie.internal.discovery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.energenie.EnergenieBindingConstants;
import org.openhab.binding.energenie.handler.EnergenieGatewayHandler;
import org.openhab.binding.energenie.handler.EnergenieSubdevicesHandler;
import org.openhab.binding.energenie.internal.api.constants.DeviceConstants;
import org.openhab.binding.energenie.internal.api.constants.JSONResponseConstants;
import org.openhab.binding.energenie.internal.api.manager.EnergenieApiManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * A discovery service for Energenie Mi|Home devices using the energenie Mi|Home REST API.
 * A {@link DiscoveryResult} is created for every registered/paired device in the user's profile
 * excluding those which already have a registered {@link Thing}
 *
 * @author Mihaela Memova
 *
 */
public class EnergenieDiscoveryService extends AbstractDiscoveryService {

    private static final int SEARCH_TIME = 10;

    private EnergenieApiManager apiManager;
    private ThingRegistry registry;
    private final Logger logger = LoggerFactory.getLogger(EnergenieDiscoveryService.class);

    public EnergenieDiscoveryService(EnergenieApiManager apiManager, ThingRegistry registry) {
        super(SEARCH_TIME);
        this.apiManager = apiManager;
        this.registry = registry;
    }

    public void activate() {
        logger.debug("Starting Mi|Home gateway discovery...");
    }

    @Override
    public void deactivate() {
        logger.debug("Stopping Mi|Home gateway discovery...");
        stopScan();
    }

    @Override
    protected void startScan() {
        if (apiManager.getConfiguration().getUserName() == null
                || apiManager.getConfiguration().getPassword() == null) {
            logger.warn("Your binding is not configured yet. Please set the credentials first.");
        } else {
            List<Thing> things = getAllEnergenieThings();
            JsonObject registeredGatewaysResponse = apiManager.listGateways();
            if (registeredGatewaysResponse != null) {
                JsonArray registeredGateways = registeredGatewaysResponse.get(JSONResponseConstants.DATA_KEY)
                        .getAsJsonArray();
                List<JsonElement> newDevices = searchNewGateways(registeredGateways, things);

                for (JsonElement device : newDevices) {
                    createGatewayDiscoveryResult((JsonObject) device);
                }
            } else {
                logger.error("Request for gateways information to Mi|Home server was not successful.");
            }

            JsonObject response = apiManager.listSubdevices();
            if (response != null) {
                JsonArray devices = response.get(JSONResponseConstants.DATA_KEY).getAsJsonArray();
                List<JsonElement> newDevices = searchNewDevices(devices, things);

                for (JsonElement device : newDevices) {
                    createSubdeviceDiscoveryResult((JsonObject) device);
                }
            } else {
                logger.error("Request for subdevices information to Mi|Home server wasn't successful.");
            }
        }
    }

    private List<Thing> getAllEnergenieThings() {
        List<Thing> allThings = new ArrayList<>();
        Collection<Thing> things = registry.getAll();
        for (Thing registeredThing : things) {

            String registeredThingBindingID = registeredThing.getThingTypeUID().getBindingId();
            if (registeredThingBindingID.equals(EnergenieBindingConstants.BINDING_ID)) {
                allThings.add(registeredThing);
            }
        }
        return allThings;
    }

    @Override
    protected synchronized void stopScan() {
        removeOlderResults(getTimestampOfLastScan());
        super.stopScan();
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        Set<ThingTypeUID> supported = new HashSet<>();
        supported.addAll(EnergenieSubdevicesHandler.SUPPORTED_THING_TYPES_UIDS);
        supported.add(EnergenieBindingConstants.THING_TYPE_GATEWAY);
        return supported;
    }

    /**
     * Searches for paired gateways that don't have things created yet
     *
     * @param gateways - all gateways registered in the Mi|Home REST API
     * @param things - all gateways things in the thing registry
     * @return list with the gateways that have no corresponding thing in the registry
     */
    private List<JsonElement> searchNewGateways(JsonArray gateways, List<Thing> things) {
        List<JsonElement> results = new LinkedList<>();

        for (JsonElement gateway : gateways) {
            if (!isThingCreatedForDevice(gateway, things)) {
                results.add(gateway);
            }
        }
        logger.debug("{} registered gateways without things found.", results.size());
        return results;
    }

    /**
     * Searches for paired subdevices that don't have things created yet
     *
     * @param subdevices - all subdevices registered in the Mi|Home REST API
     * @param things - all things attached to the current gateway
     * @return list with the subdevices that have no corresponding thing in the registry
     */
    private List<JsonElement> searchNewDevices(JsonArray subdevices, List<Thing> things) {
        List<JsonElement> results = new LinkedList<>();

        for (JsonElement device : subdevices) {
            if (!isThingCreatedForDevice(device, things)) {
                results.add(device);
            }
        }
        logger.debug("{} paired devices without things found.", results.size());
        return results;
    }

    private boolean isThingCreatedForDevice(JsonElement device, List<Thing> things) {
        JsonElement idProperty = device.getAsJsonObject().get(DeviceConstants.DEVICE_ID_KEY);
        if (idProperty != null) {
            int deviceID = idProperty.getAsInt();
            for (Thing registeredThing : things) {
                Map<String, String> props = registeredThing.getProperties();
                if (props.containsKey(EnergenieBindingConstants.PROPERTY_DEVICE_ID)) {
                    int id;
                    try {
                        id = Integer.parseInt(props.get(EnergenieBindingConstants.PROPERTY_DEVICE_ID));

                        if (deviceID == id) {
                            return true;
                        }
                    } catch (NumberFormatException exception) {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    private void createGatewayDiscoveryResult(JsonObject gateway) {
        String deviceType = gateway.get(DeviceConstants.DEVICE_TYPE_KEY).getAsString();
        int deviceID = gateway.get(DeviceConstants.DEVICE_ID_KEY).getAsInt();
        String label = gateway.get(DeviceConstants.DEVICE_LABEL_KEY).getAsString();
        String gatewayCode = gateway.get(DeviceConstants.GATEWAY_AUTH_CODE_KEY).getAsString();
        ThingTypeUID thingTypeUID = EnergenieBindingConstants.THING_TYPE_GATEWAY;

        logger.debug("Creating DiscoveryResult for gateway with id {}", deviceID);
        ThingUID thingUID = new ThingUID(thingTypeUID, Integer.toString(deviceID));

        Map<String, Object> properties = new HashMap<>();
        properties.put(EnergenieBindingConstants.CONFIG_USERNAME, this.apiManager.getConfiguration().getUserName());
        properties.put(EnergenieBindingConstants.CONFIG_PASSWORD, this.apiManager.getConfiguration().getPassword());
        properties.put(EnergenieBindingConstants.PROPERTY_DEVICE_ID, deviceID);
        properties.put(EnergenieBindingConstants.PROPERTY_TYPE, deviceType);
        properties.put(EnergenieBindingConstants.PROPERTY_AUTH_CODE, gatewayCode);
        properties.put(EnergenieBindingConstants.CONFIG_UPDATE_INTERVAL,
                EnergenieGatewayHandler.DEFAULT_UPDATE_INTERVAL);

        DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withProperties(properties).withLabel(label)
                .build();

        thingDiscovered(result);
    }

    private void createSubdeviceDiscoveryResult(JsonObject subdevice) {
        String deviceType = subdevice.get(DeviceConstants.DEVICE_TYPE_KEY).getAsString();
        int gatewayID = subdevice.get(DeviceConstants.SUBDEVICE_PARENT_ID_KEY).getAsInt();
        int deviceID = subdevice.get(DeviceConstants.DEVICE_ID_KEY).getAsInt();
        Thing bridge = getGatewayThingFromThingRegistry(gatewayID);
        ThingTypeUID thingTypeUID = EnergenieBindingConstants.DEVICE_TYPE_TO_THING_TYPE.get(deviceType);
        if (thingTypeUID != null) {
            logger.debug("Creating DiscoveryResult for device with type {} and id {}", deviceType, deviceID);
            ThingUID thingUID = new ThingUID(thingTypeUID, Integer.toString(deviceID));

            Map<String, Object> properties = new HashMap<>();
            properties.put(EnergenieBindingConstants.PROPERTY_DEVICE_ID, deviceID);
            properties.put(EnergenieBindingConstants.PROPERTY_TYPE, deviceType);
            properties.put(EnergenieBindingConstants.PROPERTY_GATEWAY_ID, gatewayID);
            properties.put(EnergenieBindingConstants.CONFIG_UPDATE_INTERVAL,
                    EnergenieSubdevicesHandler.DEFAULT_UPDATE_INTERVAL);
            DiscoveryResultBuilder resultBuilder = DiscoveryResultBuilder.create(thingUID).withProperties(properties);

            if (bridge != null) {
                resultBuilder = resultBuilder.withBridge(bridge.getUID());
            }
            thingDiscovered(resultBuilder.build());
        } else {
            logger.warn("Can't create DiscoveryResult for device with type {}, this type is not supported", deviceType);
        }
    }

    private Bridge getGatewayThingFromThingRegistry(int id) {
        Collection<Thing> things = registry.getAll();
        for (Thing registeredThing : things) {
            ThingTypeUID registeredThingTypeUID = registeredThing.getThingTypeUID();
            if (registeredThingTypeUID != null
                    && registeredThingTypeUID.equals(EnergenieBindingConstants.THING_TYPE_GATEWAY)) {
                Map<String, String> props = registeredThing.getProperties();
                int idProperty = Integer.parseInt(props.get(EnergenieBindingConstants.PROPERTY_DEVICE_ID));
                if (idProperty == id) {
                    return (Bridge) registeredThing;
                }
            }
        }
        return null;
    }
}
