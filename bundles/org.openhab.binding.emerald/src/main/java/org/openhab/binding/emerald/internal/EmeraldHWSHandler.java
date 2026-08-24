/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.emerald.internal;

import static org.openhab.binding.emerald.internal.EmeraldBindingConstants.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.emerald.internal.api.List;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link EmeraldHWSHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author paul@smedley.id.au - Initial contribution
 */
@NonNullByDefault
public class EmeraldHWSHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EmeraldHWSHandler.class);

    @Nullable
    EmeraldAccountHandler bridgeHandler;
    private @Nullable EmeraldHWSConfiguration config;

    public EmeraldHWSHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            // Optional: You could call bridgeHandler.requestStatusUpdate(config.uuid) here
            return;
        }

        if (bridgeHandler == null || config == null || config.uuid == null) {
            logger.warn("Thing not fully configured or bridge offline. Cannot send command.");
            return;
        }

        JsonObject payload = new JsonObject();

        switch (channelUID.getId()) {
            case EmeraldBindingConstants.CHANNEL_POWER:
                if (command == OnOffType.ON) {
                    payload.addProperty("switch", 1);
                } else if (command == OnOffType.OFF) {
                    payload.addProperty("switch", 0);
                }
                break;

            case EmeraldBindingConstants.CHANNEL_MODE:
                try {
                    int modeValue = -1;

                    // If openHAB passes the command as a raw decimal
                    if (command instanceof org.openhab.core.library.types.DecimalType) {
                        modeValue = ((org.openhab.core.library.types.DecimalType) command).intValue();
                    }
                    // Fallback if the UI passes it as a numeric string (e.g., "1")
                    else {
                        modeValue = Integer.parseInt(command.toString());
                    }

                    // Only send if it matches our valid options (0, 1, or 2)
                    if (modeValue >= 0 && modeValue <= 2) {
                        payload.addProperty("mode", modeValue);
                        logger.debug("Successfully mapped numerical Mode command: {}", modeValue);
                    } else {
                        logger.warn("Received invalid mode command: {}", command);
                    }
                } catch (NumberFormatException e) {
                    logger.warn("Error parsing mode command to integer: {}", command, e);
                }
                break;
            case EmeraldBindingConstants.CHANNEL_SET_TEMPERATURE:
                if (command instanceof QuantityType) {
                    int temp = ((QuantityType<?>) command).intValue();
                    payload.addProperty("temp_set", temp);
                }
                // Fallback in case openHAB sends it as a raw decimal
                else if (command instanceof DecimalType) {
                    int temp = ((DecimalType) command).intValue();
                    payload.addProperty("temp_set", temp);
                }
                break;

            default:
                logger.debug("Command not supported or unhandled for channel: {}", channelUID.getId());
                return;
        }

        // Only send the MQTT packet if we successfully mapped a command payload
        if (payload.size() > 0) {
            bridgeHandler.sendControlMessage(config.uuid, payload);
        }
    }

    protected @Nullable List getApi() {
        EmeraldAccountHandler localBridge = bridgeHandler;
        if (localBridge == null) {
            return null;
        }
        try {
            return localBridge.getApi();
        } catch (IllegalStateException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, e.getMessage());
            return null;
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(EmeraldHWSConfiguration.class);
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "No Emerald Bridge thing selected");
            return;
        }
        bridgeHandler = (EmeraldAccountHandler) bridge.getHandler();
        updateStatus(ThingStatus.ONLINE);

        List api = getApi();
        int found = 0;
        if (api != null) {
            for (int i = 0; i < api.info.property.length; i++) {
                for (int j = 0; j < api.info.property[i].heatpump.length; j++) {
                    if (config.uuid.equals(api.info.property[i].heatpump[j].id)) {
                        logger.info("Found Heat Pump id = {}", api.info.property[i].heatpump[j].id);
                        found = 1;

                        Map<String, String> properties = editProperties();
                        if (api.info.property[i].heatpump[j].softVersion != null) {
                            properties.put(Thing.PROPERTY_FIRMWARE_VERSION,
                                    api.info.property[i].heatpump[j].softVersion);
                        }
                        if (api.info.property[i].heatpump[j].hwVersion != null) {
                            properties.put(Thing.PROPERTY_HARDWARE_VERSION, api.info.property[i].heatpump[j].hwVersion);
                        }
                        if (api.info.property[i].heatpump[j].macAddress != null) {
                            properties.put(Thing.PROPERTY_MAC_ADDRESS, api.info.property[i].heatpump[j].macAddress);
                        }
                        if (api.info.property[i].heatpump[j].brand != null) {
                            properties.put(Thing.PROPERTY_VENDOR, api.info.property[i].heatpump[j].brand);
                        }
                        if (api.info.property[i].heatpump[j].model != null) {
                            properties.put(Thing.PROPERTY_MODEL_ID, api.info.property[i].heatpump[j].model);
                        }
                        if (api.info.property[i].heatpump[j].serialNumber != null) {
                            properties.put(Thing.PROPERTY_SERIAL_NUMBER, api.info.property[i].heatpump[j].serialNumber);
                        }
                        if (api.info.property[i].heatpump[j].wifiName != null) {
                            properties.put(PROPERTY_WIFI_NAME, api.info.property[i].heatpump[j].wifiName);
                        }
                        updateProperties(properties);
                    }
                }
            }
        }

        if (found == 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "UUID is not found in Emerald API - check value");
        } else {
            // If we found the unit and pulled properties, it's fully online
            updateStatus(ThingStatus.ONLINE);
        }
    }

    public void updateChannels() {
        config = getConfigAs(EmeraldHWSConfiguration.class);
        logger.debug("Updating channels");
        List api = getApi();
        if (api != null) {
            for (int i = 0; i < api.info.property.length; i++) {
                for (int j = 0; j < api.info.property[i].heatpump.length; j++) {
                    if (config.uuid.equals(api.info.property[i].heatpump[j].id)) {
                        updateState(EmeraldBindingConstants.CHANNEL_POWER,
                                OnOffType.from(api.info.property[i].heatpump[j].lastState.switchOn));
                        if (api.info.property[i].heatpump[j].lastState.mode == 0) {
                            updateState(EmeraldBindingConstants.CHANNEL_MODE, new StringType("Boost"));
                        } else if (api.info.property[i].heatpump[j].lastState.mode == 1) {
                            updateState(EmeraldBindingConstants.CHANNEL_MODE, new StringType("Normal"));
                        } else if (api.info.property[i].heatpump[j].lastState.mode == 2) {
                            updateState(EmeraldBindingConstants.CHANNEL_MODE, new StringType("Quiet"));
                        }
                        updateState(EmeraldBindingConstants.CHANNEL_CURRENT_TEMPERATURE, new QuantityType<>(
                                api.info.property[i].heatpump[j].lastState.tempCurrent, SIUnits.CELSIUS));
                        updateState(EmeraldBindingConstants.CHANNEL_SET_TEMPERATURE, new QuantityType<>(
                                api.info.property[i].heatpump[j].lastState.tempSet, SIUnits.CELSIUS));
                    }
                }
            }
        }
    }

    /**
     * Called by the Bridge when a real-time MQTT packet arrives for this specific Thing's UUID
     */
    public void updateFromMqtt(String jsonPayload) {
        logger.debug("Raw MQTT Payload received for {}: {}", thing.getUID().getId(), jsonPayload);

        try {
            JsonElement element = JsonParser.parseString(jsonPayload);
            java.util.List<JsonObject> objectsToProcess = new java.util.ArrayList<>();

            // Emerald sends an array where index 0 is metadata, and index 1 contains state updates
            if (element.isJsonArray()) {
                for (JsonElement arrElement : element.getAsJsonArray()) {
                    if (arrElement.isJsonObject()) {
                        objectsToProcess.add(arrElement.getAsJsonObject());
                    }
                }
            }
            // Fallback just in case they ever send a raw object instead of an array
            else if (element.isJsonObject()) {
                objectsToProcess.add(element.getAsJsonObject());
            }

            boolean dataFound = false;

            // Check every object in the payload for our target keys
            for (JsonObject payload : objectsToProcess) {

                // 1. Current Temperature
                if (payload.has("temp_current")) {
                    int currentTemp = payload.get("temp_current").getAsInt();
                    updateState(EmeraldBindingConstants.CHANNEL_CURRENT_TEMPERATURE,
                            new QuantityType<>(currentTemp, SIUnits.CELSIUS));
                    dataFound = true;
                }

                // 2. Set Temperature
                if (payload.has("temp_set")) {
                    int setTemp = payload.get("temp_set").getAsInt();
                    updateState(EmeraldBindingConstants.CHANNEL_SET_TEMPERATURE,
                            new QuantityType<>(setTemp, SIUnits.CELSIUS));
                    dataFound = true;
                }

                // 3. Power State
                if (payload.has("switch")) {
                    JsonElement switchElement = payload.get("switch");
                    OnOffType powerState = OnOffType.OFF; // Default to OFF

                    try {
                        // Check if the payload sent a raw number (1 or 0)
                        if (switchElement.getAsJsonPrimitive().isNumber()) {
                            powerState = (switchElement.getAsInt() == 1) ? OnOffType.ON : OnOffType.OFF;
                        }
                        // Check if the payload sent a string ("on", "off", "1", "0")
                        else {
                            String switchStr = switchElement.getAsString();
                            if ("on".equalsIgnoreCase(switchStr) || "1".equals(switchStr)) {
                                powerState = OnOffType.ON;
                            }
                        }
                    } catch (Exception e) {
                        logger.warn("Could not parse switch state from MQTT: {}", switchElement);
                    }

                    updateState(EmeraldBindingConstants.CHANNEL_POWER, powerState);
                    dataFound = true;
                }

                // 4. Operating Mode
                if (payload.has("mode")) {
                    int mode = payload.get("mode").getAsInt();
                    updateState(EmeraldBindingConstants.CHANNEL_MODE, new DecimalType(mode));
                    dataFound = true;
                }

                // 5. Fault Code
                if (payload.has("fault")) {
                    int faultCode = payload.get("fault").getAsInt();
                    // 0 usually indicates no fault.
                    updateState(EmeraldBindingConstants.CHANNEL_FAULT, new DecimalType(faultCode));
                    dataFound = true;
                }
                // 6. Defrost Status
                if (payload.has("defrost")) {
                    String defrostState = payload.get("defrost").getAsString();
                    OnOffType isDefrosting = "1".equals(defrostState) ? OnOffType.ON : OnOffType.OFF;
                    updateState(EmeraldBindingConstants.CHANNEL_DEFROST, isDefrosting);
                    dataFound = true;
                }
                // 7. Work State
                if (payload.has("work_state")) {
                    int workState = payload.get("work_state").getAsInt();
                    updateState(EmeraldBindingConstants.CHANNEL_WORK_STATE, new DecimalType(workState));
                    dataFound = true;
                }
            }

            if (dataFound) {
                logger.debug("Successfully mapped MQTT data to openHAB channels.");
            } else {
                logger.trace("Parsed MQTT message did not contain channel state data (Metadata only).");
            }

        } catch (Exception e) {
            logger.warn("Error parsing incoming MQTT message for Thing {}: {}", thing.getUID().getId(), e.getMessage());
        }
    }
}
