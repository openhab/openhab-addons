/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal;

import static org.openhab.binding.boschshc.internal.BoschSHCBindingConstants.*;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link BoschSHCHandler} is responsible for handling commands for the TwinGuard handler.
 *
 * @author Stefan Kästle - Initial contribution
 */
@NonNullByDefault
public class BoschTwinguardHandler extends BoschSHCHandler {

    private final Logger logger = LoggerFactory.getLogger(BoschSHCHandler.class);

    public BoschTwinguardHandler(Thing thing) {
        super(thing);
        logger.warn("Creating Twinguard: {}", thing.getLabel());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        BoschSHCConfiguration config = super.getBoschConfig();
        Bridge bridge = this.getBridge();

        if (bridge != null && config != null) {

            logger.info("Handle command for: {} - {}", config.id, command);
            BoschSHCBridgeHandler bridgeHandler = (BoschSHCBridgeHandler) bridge.getHandler();

            if (bridgeHandler != null) {

                if (command instanceof RefreshType && CHANNEL_TEMPERATURE.equals(channelUID.getId())) {

                    // Only refresh the state for CHANNEL_TEMPERATURE, the rest will be filled in too.
                    TwinguardState state = bridgeHandler.refreshState(getThing(), "AirQualityLevel",
                            TwinguardState.class);
                    if (state != null) {
                        updateAirQualityState(state);
                    }
                }
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Bridge or config is NUL");
        }
    }

    void updateAirQualityState(TwinguardState state) {

        updateState(CHANNEL_TEMPERATURE, new DecimalType(state.temperature));
        updateState(CHANNEL_TEMPERATURE_RATING, new StringType(state.temperatureRating));
        updateState(CHANNEL_HUMIDITY, new DecimalType(state.humidity));
        updateState(CHANNEL_HUMIDITY_RATING, new StringType(state.humidityRating));
        updateState(CHANNEL_PURITY, new DecimalType(state.purity));
        updateState(CHANNEL_AIR_DESCRIPTION, new StringType(state.description));
        updateState(CHANNEL_PURITY_RATING, new StringType(state.purityRating));
        updateState(CHANNEL_COMBINED_RATING, new StringType(state.combinedRating));
    }

    @Override
    public void processUpdate(String id, @NonNull JsonElement state) {
        logger.warn("Twinguard: received update: {} {}", id, state);

        Gson gson = new Gson();

        try {
            TwinguardState parsed = gson.fromJson(state, TwinguardState.class);

            logger.warn("Parsed switch state of {}: {}", this.getBoschID(), parsed);
            updateAirQualityState(parsed);

        } catch (JsonSyntaxException e) {
            logger.warn("Received unknown update in in-wall switch: {}", state);
        }
    }

}
