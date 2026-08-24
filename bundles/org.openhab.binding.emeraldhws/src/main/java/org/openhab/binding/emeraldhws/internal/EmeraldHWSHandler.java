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
package org.openhab.binding.emeraldhws.internal;

import static org.openhab.binding.emeraldhws.internal.EmeraldHWSBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.emeraldhws.internal.api.List;
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
    EmeraldHWSAccountHandler bridgeHandler;
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
            case EmeraldHWSBindingConstants.CHANNEL_POWER:
                if (command == OnOffType.ON) {
                    payload.addProperty("switch", 1);
                } else if (command == OnOffType.OFF) {
                    payload.addProperty("switch", 0);
                }
                break;

            case EmeraldHWSBindingConstants.CHANNEL_MODE:
                String mode = command.toString();
                if ("Boost".equalsIgnoreCase(mode)) {
                    payload.addProperty("mode", 0);
                } else if ("Normal".equalsIgnoreCase(mode)) {
                    payload.addProperty("mode", 1);
                } else if ("Quiet".equalsIgnoreCase(mode)) {
                    payload.addProperty("mode", 2);
                }
                break;

            case EmeraldHWSBindingConstants.CHANNEL_SET_TEMPERATURE:
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
        EmeraldHWSAccountHandler localBridge = bridgeHandler;
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
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "No EmeraldHWS Bridge thing selected");
            return;
        }
        bridgeHandler = (EmeraldHWSAccountHandler) bridge.getHandler();
        updateStatus(ThingStatus.ONLINE);

        List api = getApi();
        int found = 0;
        if (api != null) {
            for (int i = 0; i < api.info.property.length; i++) {
                for (int j = 0; j < api.info.property[i].heatpump.length; j++) {
                    if (config.uuid.equals(api.info.property[i].heatpump[j].id)) {
                        logger.info("Found Heat Pump id = {}", api.info.property[i].heatpump[j].id);
                        found = 1;
                    }
                }
            }
        }
        if (found == 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "UUID is not found in Emerald API - check value");
        }

        updateStatus(ThingStatus.UNKNOWN);

        // Example for background initialization:
        scheduler.execute(() -> {
            boolean thingReachable = true; // <background task with long running initialization here>
            // when done do:
            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        });
    }

    public void updateChannels() {
        config = getConfigAs(EmeraldHWSConfiguration.class);
        logger.debug("Updating channels");
        List api = getApi();
        if (api != null) {
            for (int i = 0; i < api.info.property.length; i++) {
                for (int j = 0; j < api.info.property[i].heatpump.length; j++) {
                    if (config.uuid.equals(api.info.property[i].heatpump[j].id)) {
                        updateState(EmeraldHWSBindingConstants.CHANNEL_POWER,
                                OnOffType.from(api.info.property[i].heatpump[j].lastState.switchOn));
                        if (api.info.property[i].heatpump[j].lastState.mode == 0) {
                            updateState(EmeraldHWSBindingConstants.CHANNEL_MODE, new StringType("Boost"));
                        } else if (api.info.property[i].heatpump[j].lastState.mode == 1) {
                            updateState(EmeraldHWSBindingConstants.CHANNEL_MODE, new StringType("Normal"));
                        } else if (api.info.property[i].heatpump[j].lastState.mode == 2) {
                            updateState(EmeraldHWSBindingConstants.CHANNEL_MODE, new StringType("Quiet"));
                        }
                        updateState(EmeraldHWSBindingConstants.CHANNEL_CURRENT_TEMPERATURE, new QuantityType<>(
                                api.info.property[i].heatpump[j].lastState.tempCurrent, SIUnits.CELSIUS));
                        updateState(EmeraldHWSBindingConstants.CHANNEL_SET_TEMPERATURE, new QuantityType<>(
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
                    updateState(EmeraldHWSBindingConstants.CHANNEL_CURRENT_TEMPERATURE,
                            new QuantityType<>(currentTemp, SIUnits.CELSIUS));
                    dataFound = true;
                }

                // 2. Set Temperature
                if (payload.has("temp_set")) {
                    int setTemp = payload.get("temp_set").getAsInt();
                    updateState(EmeraldHWSBindingConstants.CHANNEL_SET_TEMPERATURE,
                            new QuantityType<>(setTemp, SIUnits.CELSIUS));
                    dataFound = true;
                }

                // 3. Power State
                if (payload.has("switch")) {
                    String switchState = payload.get("switch").getAsString();
                    OnOffType powerState = "on".equalsIgnoreCase(switchState) ? OnOffType.ON : OnOffType.OFF;
                    updateState(EmeraldHWSBindingConstants.CHANNEL_POWER, powerState);
                    dataFound = true;
                }

                // 4. Operating Mode
                if (payload.has("mode")) {
                    int mode = payload.get("mode").getAsInt();
                    if (mode == 0) {
                        updateState(EmeraldHWSBindingConstants.CHANNEL_MODE, new StringType("Boost"));
                    } else if (mode == 1) {
                        updateState(EmeraldHWSBindingConstants.CHANNEL_MODE, new StringType("Normal"));
                    } else if (mode == 2) {
                        updateState(EmeraldHWSBindingConstants.CHANNEL_MODE, new StringType("Quiet"));
                    }
                    dataFound = true;
                }

                // 5. Fault Code
                if (payload.has("fault")) {
                    int faultCode = payload.get("fault").getAsInt();
                    // 0 usually indicates no fault.
                    updateState(EmeraldHWSBindingConstants.CHANNEL_FAULT, new DecimalType(faultCode));
                    dataFound = true;
                }
                // 6. Defrost Status
                if (payload.has("defrost")) {
                    String defrostState = payload.get("defrost").getAsString();
                    OnOffType isDefrosting = "1".equals(defrostState) ? OnOffType.ON : OnOffType.OFF;
                    updateState(EmeraldHWSBindingConstants.CHANNEL_DEFROST, isDefrosting);
                    dataFound = true;
                }
                // 7. Work State
                if (payload.has("work_state")) {
                    int workState = payload.get("work_state").getAsInt();
                    updateState(EmeraldHWSBindingConstants.CHANNEL_WORK_STATE, new DecimalType(workState));
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
