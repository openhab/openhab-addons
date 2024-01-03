/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import org.openhab.binding.tapocontrol.internal.device.TapoBridgeHandler;
import org.openhab.binding.tapocontrol.internal.structures.TapoBridgeConfiguration;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
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
@Component(scope = ServiceScope.PROTOTYPE, service = TapoDiscoveryService.class)
@NonNullByDefault
public class TapoDiscoveryService extends AbstractThingHandlerDiscoveryService<TapoBridgeHandler> {
    private final Logger logger = LoggerFactory.getLogger(TapoDiscoveryService.class);

    /***********************************
     *
     * INITIALIZATION
     *
     ************************************/

    /**
     * INIT CLASS
     */
    public TapoDiscoveryService() {
        super(TapoBridgeHandler.class, SUPPORTED_THING_TYPES_UIDS, TAPO_DISCOVERY_TIMEOUT_S, false);
    }

    @Override
    public void initialize() {
        thingHandler.setDiscoveryService(this);
        TapoBridgeConfiguration config = thingHandler.getBridgeConfig();
        modified(Map.of(DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY, config.cloudDiscovery));
        super.initialize();
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
        JsonArray jsonArray = thingHandler.getDeviceList();
        handleCloudDevices(jsonArray);
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
        ThingUID bridgeUID = thingHandler.getUID();
        ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, deviceMAC);
        return DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withRepresentationProperty(DEVICE_REPRESENTATION_PROPERTY).withBridge(bridgeUID).withLabel(label)
                .build();
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
            logger.debug("error handling CloudDevices", e);
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
