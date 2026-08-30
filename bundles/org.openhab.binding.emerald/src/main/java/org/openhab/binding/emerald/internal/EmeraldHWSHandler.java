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
import org.openhab.binding.emerald.internal.api.EmeraldList;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
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
 * @author Paul Smedley - Initial contribution
 */
@NonNullByDefault
public class EmeraldHWSHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EmeraldHWSHandler.class);

    @Nullable
    EmeraldAccountHandler bridgeHandler;
    private @Nullable EmeraldHWSConfiguration config;
    private int cachedCurrentTemp = -1;
    private int cachedSetTemp = -1;

    public EmeraldHWSHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateChannels();
            return;
        }

        EmeraldAccountHandler localBridgeHandler = bridgeHandler;
        EmeraldHWSConfiguration localConfig = config;

        if (localBridgeHandler == null || localConfig == null) {
            logger.warn("Thing not fully configured or bridge offline. Cannot send command.");
            return;
        }

        String uuid = localConfig.uuid;
        JsonObject payload = new JsonObject();

        switch (channelUID.getId()) {
            case EmeraldBindingConstants.CHANNEL_POWER -> {
                if (command == OnOffType.ON) {
                    payload.addProperty("switch", 1);
                } else if (command == OnOffType.OFF) {
                    payload.addProperty("switch", 0);
                }
            }
            case EmeraldBindingConstants.CHANNEL_MODE -> {
                try {
                    int modeValue = switch (command) {
                        case DecimalType dec -> dec.intValue();
                        default -> Integer.parseInt(command.toString());
                    };

                    if (modeValue >= 0 && modeValue <= 2) {
                        payload.addProperty("mode", modeValue);
                        logger.debug("Successfully mapped numerical Mode command: {}", modeValue);
                    } else {
                        logger.warn("Received invalid mode command: {}", command);
                    }
                } catch (NumberFormatException e) {
                    logger.warn("Error parsing mode command to integer: {}", command, e);
                }
            }
            case EmeraldBindingConstants.CHANNEL_SET_TEMPERATURE -> {
                if (command instanceof QuantityType<?> qty) {
                    @Nullable
                    QuantityType<?> celsiusQty = qty.toUnit(SIUnits.CELSIUS);

                    if (celsiusQty != null) {
                        payload.addProperty("temp_set", celsiusQty.intValue());
                    } else {
                        logger.warn("Failed to convert received command to Celsius: {}", command);
                    }
                } else if (command instanceof DecimalType dec) {
                    payload.addProperty("temp_set", dec.intValue());
                }
            }
            default -> {
                logger.debug("Command not supported or unhandled for channel: {}", channelUID.getId());
                return;
            }
        }

        if (payload.size() > 0) {
            localBridgeHandler.sendControlMessage(uuid, payload);
        }
    }

    protected @Nullable EmeraldList getApi() {
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
        EmeraldHWSConfiguration localConfig = getConfigAs(EmeraldHWSConfiguration.class);
        config = localConfig;

        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "No Emerald Bridge thing selected");
            return;
        }
        if (bridge.getHandler() instanceof EmeraldAccountHandler emeraldAccountHandler) {
            bridgeHandler = emeraldAccountHandler;
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    "Emerald bridge handler is not initialized");
            return;
        }

        updateStatus(ThingStatus.ONLINE);

        EmeraldList api = getApi();
        if (api != null) {
            EmeraldList.HeatpumpContext ctx = api.findHeatpump(localConfig.uuid);

            if (ctx != null) {
                EmeraldList.Heatpump hp = ctx.heatpump();
                Map<String, String> properties = editProperties();
                if (!hp.softVersion.isEmpty()) {
                    properties.put(Thing.PROPERTY_FIRMWARE_VERSION, hp.softVersion);
                }
                if (!hp.hwVersion.isEmpty()) {
                    properties.put(Thing.PROPERTY_HARDWARE_VERSION, hp.hwVersion);
                }
                if (!hp.macAddress.isEmpty()) {
                    properties.put(Thing.PROPERTY_MAC_ADDRESS, hp.macAddress);
                }
                if (!hp.brand.isEmpty()) {
                    properties.put(Thing.PROPERTY_VENDOR, hp.brand);
                }
                if (!hp.model.isEmpty()) {
                    properties.put(Thing.PROPERTY_MODEL_ID, hp.model);
                }
                if (!hp.serialNumber.isEmpty()) {
                    properties.put(Thing.PROPERTY_SERIAL_NUMBER, hp.serialNumber);
                }
                if (!hp.wifiName.isEmpty()) {
                    properties.put(PROPERTY_WIFI_NAME, hp.wifiName);
                }
                updateProperties(properties);

                updateStatus(ThingStatus.ONLINE);
                return;
            }
        }

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                "UUID is not found in Emerald API - check value");
    }

    public void updateChannels() {
        EmeraldHWSConfiguration localConfig = getConfigAs(EmeraldHWSConfiguration.class);
        config = localConfig;

        logger.debug("Updating channels");
        EmeraldList api = getApi();

        if (api != null) {
            EmeraldList.HeatpumpContext ctx = api.findHeatpump(localConfig.uuid);

            if (ctx != null) {
                EmeraldList.Heatpump hp = ctx.heatpump();

                cachedCurrentTemp = hp.lastState.tempCurrent;
                cachedSetTemp = hp.lastState.tempSet;

                updateState(EmeraldBindingConstants.CHANNEL_POWER, OnOffType
                        .from("1".equals(hp.lastState.switchOn) || "on".equalsIgnoreCase(hp.lastState.switchOn)));
                updateState(EmeraldBindingConstants.CHANNEL_MODE, new DecimalType(hp.lastState.mode));
                updateState(EmeraldBindingConstants.CHANNEL_CURRENT_TEMPERATURE,
                        new QuantityType<>(cachedCurrentTemp, SIUnits.CELSIUS));
                updateState(EmeraldBindingConstants.CHANNEL_SET_TEMPERATURE,
                        new QuantityType<>(cachedSetTemp, SIUnits.CELSIUS));
                calculateAndPublishCapacity();
            }
        }
    }

    private void calculateAndPublishCapacity() {
        if (cachedCurrentTemp >= 0 && cachedSetTemp >= 0) {
            double raw = 100.0 - 2.3 * (cachedSetTemp - cachedCurrentTemp);
            double clamped = Math.max(0.0, Math.min(100.0, raw));

            // Snap to the nearest 20% step just like the Emerald app
            int rounded = (int) (Math.round(clamped / 20.0) * 20);

            updateState(EmeraldBindingConstants.CHANNEL_TANK_CAPACITY,
                    new QuantityType<>(rounded, org.openhab.core.library.unit.Units.PERCENT));
        }
    }

    /**
     * Called by the Bridge when a real-time MQTT packet arrives for this specific Thing's UUID
     */
    public void updateFromMqtt(String jsonPayload) {
        logger.trace("Raw MQTT Payload received for {}: {}", thing.getUID().getId(), jsonPayload);

        try {
            JsonElement element = JsonParser.parseString(jsonPayload);
            java.util.List<JsonObject> objectsToProcess = new java.util.ArrayList<>();

            if (element.isJsonArray()) {
                for (JsonElement arrElement : element.getAsJsonArray()) {
                    if (arrElement.isJsonObject()) {
                        objectsToProcess.add(arrElement.getAsJsonObject());
                    }
                }
            } else if (element.isJsonObject()) {
                objectsToProcess.add(element.getAsJsonObject());
            }

            boolean dataFound = false;

            for (JsonObject payload : objectsToProcess) {
                if (payload.has("temp_current")) {
                    cachedCurrentTemp = payload.get("temp_current").getAsInt();
                    updateState(EmeraldBindingConstants.CHANNEL_CURRENT_TEMPERATURE,
                            new QuantityType<>(cachedCurrentTemp, SIUnits.CELSIUS));
                    dataFound = true;
                }

                if (payload.has("temp_set")) {
                    cachedSetTemp = payload.get("temp_set").getAsInt();
                    updateState(EmeraldBindingConstants.CHANNEL_SET_TEMPERATURE,
                            new QuantityType<>(cachedSetTemp, SIUnits.CELSIUS));
                    dataFound = true;
                }

                if (payload.has("switch")) {
                    JsonElement switchElement = payload.get("switch");
                    OnOffType powerState = OnOffType.OFF;

                    try {
                        if (switchElement.getAsJsonPrimitive().isNumber()) {
                            powerState = (switchElement.getAsInt() == 1) ? OnOffType.ON : OnOffType.OFF;
                        } else {
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

                if (payload.has("mode")) {
                    int mode = payload.get("mode").getAsInt();
                    updateState(EmeraldBindingConstants.CHANNEL_MODE, new DecimalType(mode));
                    dataFound = true;
                }

                if (payload.has("fault")) {
                    int faultCode = payload.get("fault").getAsInt();
                    updateState(EmeraldBindingConstants.CHANNEL_FAULT, new DecimalType(faultCode));
                    dataFound = true;
                }

                if (payload.has("defrost")) {
                    String defrostState = payload.get("defrost").getAsString();
                    OnOffType isDefrosting = "1".equals(defrostState) ? OnOffType.ON : OnOffType.OFF;
                    updateState(EmeraldBindingConstants.CHANNEL_DEFROST, isDefrosting);
                    dataFound = true;
                }

                if (payload.has("work_state")) {
                    int workState = payload.get("work_state").getAsInt();
                    updateState(EmeraldBindingConstants.CHANNEL_WORK_STATE, new DecimalType(workState));
                    dataFound = true;
                }
            }

            if (dataFound) {
                calculateAndPublishCapacity();
                logger.debug("Successfully mapped MQTT data to openHAB channels.");
            } else {
                logger.debug("Parsed MQTT message did not contain channel state data (Metadata only).");
            }

        } catch (Exception e) {
            logger.warn("Error parsing incoming MQTT message for Thing {}: {}", thing.getUID().getId(), e.getMessage());
        }
    }
}
