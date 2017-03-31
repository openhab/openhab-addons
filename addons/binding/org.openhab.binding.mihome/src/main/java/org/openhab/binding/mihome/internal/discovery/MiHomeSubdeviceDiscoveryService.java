/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.internal.discovery;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.mihome.MiHomeBindingConstants;
import org.openhab.binding.mihome.handler.MiHomeGatewayHandler;
import org.openhab.binding.mihome.handler.MiHomeSubdevicesHandler;
import org.openhab.binding.mihome.internal.api.constants.DeviceConstants;
import org.openhab.binding.mihome.internal.api.constants.JSONResponseConstants;
import org.openhab.binding.mihome.internal.api.manager.MiHomeApiManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * A discovery service for Mi|Home subdevices using the Mi|Home REST API.
 * A {@link DiscoveryResult} is created for each paired device in the Mi|Home REST API, for which there is no
 * {@link Thing} created
 *
 * @author Svilen Valkanov
 *
 */
public class MiHomeSubdeviceDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // Time in seconds before the discovery services stops
    private static final int SEARCH_TIME = 10;

    private MiHomeGatewayHandler gatewayHandler;

    public MiHomeSubdeviceDiscoveryService(MiHomeGatewayHandler gatewayHandler) {
        super(SEARCH_TIME);
        this.gatewayHandler = gatewayHandler;
    }

    @Override
    protected void startScan() {
        ThingStatus gatewayStatus = gatewayHandler.getThing().getStatus();
        if (gatewayStatus.equals(ThingStatus.ONLINE)) {
            int gatewayID = gatewayHandler.getGatewayId();
            MiHomeApiManager apiManager = gatewayHandler.getMiHomeApiManager();

            if (apiManager == null || gatewayID < 0) {
                throw new IllegalStateException("Gateway isn't initialized correctly.");
            }

            logger.info("Starting scan for Mi|Home Subdevices for gateway with id {}", gatewayID);

            Bridge bridge = gatewayHandler.getThing();
            ThingUID bridgeUID = bridge.getUID();
            List<Thing> things = bridge.getThings();

            JsonObject response = apiManager.listSubdevices();
            if (response != null) {
                JsonArray devices = response.get(JSONResponseConstants.DATA_KEY).getAsJsonArray();

                List<JsonElement> newDevices = searchNewDevices(devices, things, gatewayID);

                for (JsonElement device : newDevices) {
                    createDiscoveryResult((JsonObject) device, bridgeUID);
                }
            } else {
                logger.error("Request for subdevices information to Mi|Home server wasn't successful.");
            }
        } else {
            logger.warn("Discovery service for gateway {} can't be started, gateway is in status {}, expected {}",
                    gatewayHandler.getGatewayId(), gatewayStatus, ThingStatus.ONLINE);
        }
    }

    @Override
    protected synchronized void stopScan() {
        removeOlderResults(getTimestampOfLastScan());
        super.stopScan();
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return MiHomeSubdevicesHandler.SUPPORTED_THING_TYPES_UIDS;
    }

    /**
     * Returns the Mi|Home device IDs of all devices that have a Thing representation. The device ID is stored in the
     * Thing configuration after a successful pairing
     *
     * @param things - list of things
     * @return a Set of deviceIDs of the devices for which a Thing is already created
     */
    private Set<Integer> getDevicesWithThings(List<Thing> things) {
        Set<Integer> ids = new HashSet<>();

        for (Thing thing : things) {
            Configuration config = thing.getConfiguration();
            // Take only the successfully paired devices that have persisted the device ID
            if (config.containsKey(MiHomeBindingConstants.PROPERTY_DEVICE_ID)) {
                BigDecimal deviceID = (BigDecimal) config.get(MiHomeBindingConstants.PROPERTY_DEVICE_ID);
                ids.add(deviceID.intValue());
            }
        }

        return ids;
    }

    /**
     * Searches for paired device, that don't have things created yet
     *
     * @param subdevices - all subdevices registered in the Mi|Home REST API
     * @param things - all things attached to the current gateway
     * @param gatewayID - the ID of the current gateway
     * @return list with devices
     */
    private List<JsonElement> searchNewDevices(JsonArray subdevices, List<Thing> things, int gatewayID) {
        List<JsonElement> results = new LinkedList<>();

        Set<Integer> devicesWithThings = getDevicesWithThings(things);
        logger.debug(
                "Gateway with ID {} has already {} successfully added devices. They will be excluded from the search.",
                gatewayID, devicesWithThings.size());

        for (JsonElement device : subdevices) {
            if (!isThingCreatedForDevice(device, gatewayID, devicesWithThings)) {
                results.add(device);
            }
        }
        logger.debug("{} paired devices without things found.", results.size());
        return results;
    }

    private boolean isThingCreatedForDevice(JsonElement device, int gatewayID, Set<Integer> excludedDeviceIDs) {
        JsonElement deviceID = ((JsonObject) device).get(DeviceConstants.DEVICE_ID_KEY);
        JsonElement parentID = ((JsonObject) device).get(DeviceConstants.SUBDEVICE_PARENT_ID_KEY);

        int id = deviceID.getAsInt();
        int parent = parentID.getAsInt();

        return gatewayID == parent && excludedDeviceIDs.contains(id);
    }

    private void createDiscoveryResult(JsonObject device, ThingUID bridgeUID) {
        String deviceType = device.get(DeviceConstants.DEVICE_TYPE_KEY).getAsString();
        int gatewayID = device.get(DeviceConstants.SUBDEVICE_PARENT_ID_KEY).getAsInt();
        int deviceID = device.get(DeviceConstants.DEVICE_ID_KEY).getAsInt();
        String label = device.get(DeviceConstants.DEVICE_LABEL_KEY).getAsString();

        ThingTypeUID thingTypeUID = MiHomeBindingConstants.DEVICE_TYPE_TO_THING_TYPE.get(deviceType);
        if (thingTypeUID != null) {
            logger.info("Creating DiscoveryResult for device with type {} and id {}", deviceType, deviceID);
            ThingUID thingUID = new ThingUID(thingTypeUID, Integer.toString(deviceID));

            Map<String, Object> properties = new HashMap<>();
            properties.put(MiHomeBindingConstants.CONFIG_UPDATE_ITNERVAL,
                    MiHomeSubdevicesHandler.DEFAULT_UPDATE_INTERVAL);
            properties.put(MiHomeBindingConstants.PROPERTY_DEVICE_ID, deviceID);
            properties.put(MiHomeBindingConstants.PROPERTY_TYPE, deviceType);
            properties.put(MiHomeBindingConstants.PROPERTY_GATEWAY_ID, gatewayID);

            DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withProperties(properties).withLabel(label)
                    .withBridge(bridgeUID).build();
            thingDiscovered(result);
        } else {
            logger.warn("Can't create DiscoveryResult for device with type {}, this type is not supported", deviceType);
        }

    }
}
