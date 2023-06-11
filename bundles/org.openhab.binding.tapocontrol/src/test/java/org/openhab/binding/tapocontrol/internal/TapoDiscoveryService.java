/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.tapocontrol.internal;

import static org.openhab.binding.tapocontrol.internal.constants.TapoBindingSettings.*;
import static org.openhab.binding.tapocontrol.internal.constants.TapoThingConstants.*;
import static org.openhab.binding.tapocontrol.internal.helpers.TapoUtils.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tapocontrol.internal.device.TapoBridgeHandler;
import org.openhab.binding.tapocontrol.internal.structures.TapoBridgeConfiguration;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Handler class for TAPO Smart Home thing discovery
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {
    private final Logger logger = LoggerFactory.getLogger(TapoDiscoveryService.class);
    protected @NonNullByDefault({}) TapoBridgeHandler bridge;

    /***********************************
     *
     * INITIALIZATION
     *
     ************************************/

    /**
     * INIT CLASS
     * 
     * @param bridgeHandler
     */
    public TapoDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, TAPO_DISCOVERY_TIMEOUT_S, false);
    }

    /**
     * activate
     */
    @Override
    public void activate() {
        TapoBridgeConfiguration config = bridge.getBridgeConfig();
        if (config.cloudDiscovery || config.udpDiscovery) {
            startBackgroundDiscovery();
        }
    }

    /**
     * deactivate
     */
    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof TapoBridgeHandler) {
            TapoBridgeHandler tapoBridge = (TapoBridgeHandler) handler;
            tapoBridge.setDiscoveryService(this);
            this.bridge = tapoBridge;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.bridge;
    }

    /***********************************
     *
     * SCAN HANDLING
     *
     ************************************/

    /**
     * Start scan manually
     */
    @Override
    public void startScan() {
        removeOlderResults(getTimestampOfLastScan());
        if (bridge != null) {
            JsonArray jsonArray = bridge.getDeviceList();
            handleCloudDevices(jsonArray);
        }
    }

    /***********************************
     *
     * handle Results
     *
     ************************************/

    /**
     * CREATE DISCOVERY RESULT
     * creates discoveryResult (Thing) from JsonObject got from Cloud
     * 
     * @param device JsonObject with device information
     * @return DiscoveryResult-Object
     */
    public DiscoveryResult createResult(JsonObject device) {
        TapoBridgeHandler tapoBridge = this.bridge;
        String deviceModel = getDeviceModel(device);
        String label = getDeviceLabel(device);
        String deviceMAC = device.get(CLOUD_JSON_KEY_MAC).getAsString();
        ThingTypeUID thingTypeUID = new ThingTypeUID(BINDING_ID, deviceModel);

        /* create properties */
        Map<String, Object> properties = new HashMap<>();
        properties.put(Thing.PROPERTY_VENDOR, DEVICE_VENDOR);
        properties.put(Thing.PROPERTY_MAC_ADDRESS, formatMac(deviceMAC, MAC_DIVISION_CHAR));
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, device.get(CLOUD_JSON_KEY_FW).getAsString());
        properties.put(Thing.PROPERTY_HARDWARE_VERSION, device.get(CLOUD_JSON_KEY_HW).getAsString());
        properties.put(Thing.PROPERTY_MODEL_ID, deviceModel);
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, device.get(CLOUD_JSON_KEY_ID).getAsString());

        logger.debug("device {} discovered", deviceModel);
        if (tapoBridge != null) {
            ThingUID bridgeUID = tapoBridge.getUID();
            ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, deviceMAC);
            return DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withRepresentationProperty(DEVICE_REPRESENTATION_PROPERTY).withBridge(bridgeUID).withLabel(label)
                    .build();
        } else {
            ThingUID thingUID = new ThingUID(BINDING_ID, deviceMAC);
            return DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withRepresentationProperty(DEVICE_REPRESENTATION_PROPERTY).withLabel(label).build();
        }
    }

    /**
     * work with result from get devices from cloud devices
     * 
     * @param deviceList
     */
    protected void handleCloudDevices(JsonArray deviceList) {
        try {
            for (JsonElement deviceElement : deviceList) {
                if (deviceElement.isJsonObject()) {
                    JsonObject device = deviceElement.getAsJsonObject();
                    String deviceModel = getDeviceModel(device);
                    ThingTypeUID thingTypeUID = new ThingTypeUID(BINDING_ID, deviceModel);

                    /* create thing */
                    if (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
                        DiscoveryResult discoveryResult = createResult(device);
                        thingDiscovered(discoveryResult);
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("error handlling CloudDevices", e);
        }
    }

    /**
     * GET DEVICEMODEL
     * 
     * @param device JsonObject with deviceData
     * @return String with DeviceModel
     */
    protected String getDeviceModel(JsonObject device) {
        try {
            String deviceModel = device.get(CLOUD_JSON_KEY_MODEL).getAsString();
            deviceModel = deviceModel.replaceAll("\\(.*\\)", ""); // replace (DE)
            deviceModel = deviceModel.replace("Tapo", "");
            deviceModel = deviceModel.replace("Series", "");
            deviceModel = deviceModel.trim();
            deviceModel = deviceModel.replace(" ", "_");
            return deviceModel;
        } catch (Exception e) {
            logger.debug("error getDeviceModel", e);
            return "";
        }
    }

    /**
     * GET DEVICE LABEL
     * 
     * @param device JsonObject with deviceData
     * @return String with DeviceLabel
     */
    protected String getDeviceLabel(JsonObject device) {
        try {
            String deviceLabel = "";
            String deviceModel = getDeviceModel(device);
            ThingTypeUID deviceUID = new ThingTypeUID(BINDING_ID, deviceModel);

            if (SUPPORTED_SMART_PLUG_UIDS.contains(deviceUID)) {
                deviceLabel = DEVICE_DESCRIPTION_SMART_PLUG;
            } else if (SUPPORTED_WHITE_BULB_UIDS.contains(deviceUID)) {
                deviceLabel = DEVICE_DESCRIPTION_WHITE_BULB;
            } else if (SUPPORTED_COLOR_BULB_UIDS.contains(deviceUID)) {
                deviceLabel = DEVICE_DESCRIPTION_COLOR_BULB;
            }
            return DEVICE_VENDOR + " " + deviceModel + " " + deviceLabel;
        } catch (Exception e) {
            logger.debug("error getDeviceLabel", e);
            return "";
        }
    }
}
