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

import com.google.gson.Gson;
import com.google.gson.JsonObject;

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
        if (CHANNEL_CURRENT_TEMPERATURE.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                // TODO: handle data refresh
            }

            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
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
        logger.info("Updating channels");
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
        logger.debug("Updating Thing {} from MQTT real-time data", thing.getUID().getId());

        try {
            Gson gson = new Gson();
            JsonObject payload = gson.fromJson(jsonPayload, JsonObject.class);

            // Example extraction (adapt the keys based on the actual Emerald AWS IoT payload)
            if (payload.has("temp_current")) {
                int currentTemp = payload.get("temp_current").getAsInt();
                updateState(EmeraldHWSBindingConstants.CHANNEL_CURRENT_TEMPERATURE,
                        new QuantityType<>(currentTemp, SIUnits.CELSIUS));
            }

            if (payload.has("temp_set")) {
                int setTemp = payload.get("temp_set").getAsInt();
                updateState(EmeraldHWSBindingConstants.CHANNEL_SET_TEMPERATURE,
                        new QuantityType<>(setTemp, SIUnits.CELSIUS));
            }

            // Add additional channel updates as discovered in the MQTT payload...

        } catch (Exception e) {
            logger.warn("Error parsing incoming MQTT message for Thing {}: {}", thing.getUID().getId(), e.getMessage());
        }
    }
}
