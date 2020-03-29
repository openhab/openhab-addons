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

import static org.openhab.binding.boschshc.internal.BoschSHCBindingConstants.CHANNEL_TEMPERATURE;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
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

                if (CHANNEL_TEMPERATURE.equals(channelUID.getId())) {
                    if (command instanceof RefreshType) {

                        // Refresh the temperature from the Bosch Twinguard device.
                        // Might not be necessary, can just wait until we get one
                        logger.warn("Refreshing the temperature is not yet supported.");
                    }
                    // Otherwise: not action supported here.
                }
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Bridge or config is NUL");
        }
    }

    @Override
    public void processUpdate(@NonNull JsonElement state) {
        logger.warn("Twinguard: received update: {}", state);

        Gson gson = new Gson();

        try {
            TwinguardState parsed = gson.fromJson(state, TwinguardState.class);

            // Update power switch
            logger.warn("Parsed switch state of {}: {}", this.getBoschID(), parsed.temperature);
            State temperature = new DecimalType(parsed.temperature);
            updateState(CHANNEL_TEMPERATURE, temperature);

        } catch (JsonSyntaxException e) {
            logger.warn("Received unknown update in in-wall switch: {}", state);
        }
    }

}
