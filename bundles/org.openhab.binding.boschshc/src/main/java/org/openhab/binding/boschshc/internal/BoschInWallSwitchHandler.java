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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
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
 * Represents Bosch in-wall switches.
 *
 * @author Stefan Kästle
 */
@NonNullByDefault
public class BoschInWallSwitchHandler extends BoschSHCHandler {

    private final Logger logger = LoggerFactory.getLogger(BoschSHCHandler.class);

    public BoschInWallSwitchHandler(Thing thing) {
        super(thing);
        logger.warn("Creating in-wall: {}", thing.getLabel());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        BoschSHCConfiguration config = super.getBoschConfig();
        Bridge bridge = this.getBridge();

        if (bridge != null && config != null) {

            logger.info("Handle command for: {} - {}", config.id, command);
            BoschSHCBridgeHandler bridgeHandler = (BoschSHCBridgeHandler) bridge.getHandler();

            if (bridgeHandler != null) {

                if (command instanceof RefreshType) {
                    switch (channelUID.getId()) {
                        case CHANNEL_POWER_SWITCH: {
                            PowerSwitchState state = bridgeHandler.refreshState(getThing(), "PowerSwitch",
                                    PowerSwitchState.class);
                            if (state != null) {
                                updatePowerSwitchState(state);
                            }
                            break;
                        }
                        case CHANNEL_POWER_CONSUMPTION: {
                            PowerMeterState state = bridgeHandler.refreshState(getThing(), "PowerMeter",
                                    PowerMeterState.class);
                            if (state != null) {
                                updatePowerMeterState(state);
                            }
                            break;
                        }
                        case CHANNEL_ENERGY_CONSUMPTION:
                            // Nothing to do here, since the same update is received from POWER_CONSUMPTION
                            break;
                        default:
                            logger.warn("Received refresh request for unsupported channel: {}", channelUID);
                    }
                } else {
                    bridgeHandler.updateSwitchState(getThing(), command.toFullString());
                }
            }
        } else {

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Bridge or config is NUL");
        }
    }

    void updatePowerMeterState(PowerMeterState state) {
        logger.debug("Parsed power meter state of {}: energy {} - power {}", this.getBoschID(), state.energyConsumption,
                state.energyConsumption);

        updateState(CHANNEL_POWER_CONSUMPTION, new DecimalType(state.powerConsumption));
        updateState(CHANNEL_ENERGY_CONSUMPTION, new DecimalType(state.energyConsumption));
    }

    void updatePowerSwitchState(PowerSwitchState state) {

        // Update power switch
        logger.debug("Parsed switch state of {}: {}", this.getBoschID(), state.switchState);
        State powerState = OnOffType.from(state.switchState);
        updateState(CHANNEL_POWER_SWITCH, powerState);
    }

    @Override
    public void processUpdate(String id, JsonElement state) {
        logger.debug("in-wall switch: received update: ID {} state {}", id, state);

        Gson gson = new Gson();

        try {

            if (id.equals("PowerMeter")) {
                updatePowerMeterState(gson.fromJson(state, PowerMeterState.class));

            } else {
                updatePowerSwitchState(gson.fromJson(state, PowerSwitchState.class));

            }

        } catch (JsonSyntaxException e) {
            logger.warn("Received unknown update in in-wall switch: {}", state);
        }
    }
}
