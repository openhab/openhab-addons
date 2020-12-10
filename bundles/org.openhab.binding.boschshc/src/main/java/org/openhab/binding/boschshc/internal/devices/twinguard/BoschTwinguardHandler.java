/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.devices.twinguard;

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.*;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschshc.internal.devices.BoschSHCHandler;
import org.openhab.binding.boschshc.internal.devices.twinguard.dto.AirQualityLevelState;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

import com.google.gson.JsonElement;

/**
 * The {@link BoschSHCHandler} is responsible for handling commands for the TwinGuard handler.
 *
 * @author Stefan Kästle - Initial contribution
 */
@NonNullByDefault
public class BoschTwinguardHandler extends BoschSHCHandler {

    public BoschTwinguardHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Bridge bridge = this.getBridge();

        if (bridge != null) {
            logger.debug("Handle command for: {} - {}", channelUID.getThingUID(), command);

            if (command instanceof RefreshType) {
                AirQualityLevelState state = this.getState("AirQualityLevel", AirQualityLevelState.class);
                if (state != null) {
                    updateAirQualityState(state);
                }
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Bridge is NUL");
        }
    }

    void updateAirQualityState(AirQualityLevelState state) {
        updateState(CHANNEL_TEMPERATURE, new QuantityType<Temperature>(state.temperature, SIUnits.CELSIUS));
        updateState(CHANNEL_TEMPERATURE_RATING, new StringType(state.temperatureRating));
        updateState(CHANNEL_HUMIDITY, new QuantityType<Dimensionless>(state.humidity, Units.ONE));
        updateState(CHANNEL_HUMIDITY_RATING, new StringType(state.humidityRating));
        updateState(CHANNEL_PURITY, new QuantityType<Dimensionless>(state.purity, Units.ONE));
        updateState(CHANNEL_AIR_DESCRIPTION, new StringType(state.description));
        updateState(CHANNEL_PURITY_RATING, new StringType(state.purityRating));
        updateState(CHANNEL_COMBINED_RATING, new StringType(state.combinedRating));
    }

    @Override
    public void processUpdate(String id, JsonElement state) {
        logger.debug("Twinguard: received update: {} {}", id, state);

        @Nullable
        AirQualityLevelState parsed = gson.fromJson(state, AirQualityLevelState.class);
        if (parsed == null) {
            logger.warn("Received unknown update in in-wall switch: {}", state);
            return;
        }

        logger.debug("Parsed switch state of {}: {}", this.getBoschID(), parsed);
        updateAirQualityState(parsed);
    }
}
