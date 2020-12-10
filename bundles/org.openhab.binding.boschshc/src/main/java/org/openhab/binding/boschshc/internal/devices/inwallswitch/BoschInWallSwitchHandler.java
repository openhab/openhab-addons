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
package org.openhab.binding.boschshc.internal.devices.inwallswitch;

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.*;

import java.util.List;

import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.devices.BoschSHCHandler;
import org.openhab.binding.boschshc.internal.devices.inwallswitch.dto.PowerMeterState;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.powerswitch.PowerSwitchService;
import org.openhab.binding.boschshc.internal.services.powerswitch.PowerSwitchState;
import org.openhab.binding.boschshc.internal.services.powerswitch.dto.PowerSwitchServiceState;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SmartHomeUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

import com.google.gson.JsonElement;

/**
 * Represents Bosch in-wall switches.
 *
 * @author Stefan Kästle - Initial contribution
 */
@NonNullByDefault
public class BoschInWallSwitchHandler extends BoschSHCHandler {

    private final PowerSwitchService powerSwitchService;

    public BoschInWallSwitchHandler(Thing thing) {
        super(thing);
        this.powerSwitchService = new PowerSwitchService();
    }

    @Override
    protected void initializeServices() throws BoschSHCException {
        super.initializeServices();

        this.registerService(this.powerSwitchService, this::updateChannels, List.of(CHANNEL_POWER_SWITCH));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        logger.debug("Handle command for: {} - {}", channelUID.getThingUID(), command);

        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case CHANNEL_POWER_CONSUMPTION: {
                    PowerMeterState state = this.getState("PowerMeter", PowerMeterState.class);
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
            switch (channelUID.getId()) {
                case CHANNEL_POWER_SWITCH:
                    if (command instanceof OnOffType) {
                        updatePowerSwitchState((OnOffType) command);
                    }
                    break;
            }
        }
    }

    void updatePowerMeterState(PowerMeterState state) {
        logger.debug("Parsed power meter state of {}: energy {} - power {}", this.getBoschID(), state.energyConsumption,
                state.energyConsumption);

        updateState(CHANNEL_POWER_CONSUMPTION, new QuantityType<Power>(state.powerConsumption, SmartHomeUnits.WATT));
        updateState(CHANNEL_ENERGY_CONSUMPTION,
                new QuantityType<Energy>(state.energyConsumption, SmartHomeUnits.WATT_HOUR));
    }

    /**
     * Updates the channels which are linked to the {@link PowerSwitchService} of the device.
     * 
     * @param state Current state of {@link PowerSwitchService}.
     */
    private void updateChannels(PowerSwitchServiceState state) {
        State powerState = OnOffType.from(state.switchState.toString());
        super.updateState(CHANNEL_POWER_SWITCH, powerState);
    }

    private void updatePowerSwitchState(OnOffType command) {
        PowerSwitchServiceState state = new PowerSwitchServiceState();
        state.switchState = PowerSwitchState.valueOf(command.toFullString());
        this.powerSwitchService.setState(state);
    }

    @Override
    public void processUpdate(String id, JsonElement state) {
        super.processUpdate(id, state);

        logger.debug("in-wall switch: received update: ID {} state {}", id, state);

        if (id.equals("PowerMeter")) {
            PowerMeterState powerMeterState = gson.fromJson(state, PowerMeterState.class);
            if (powerMeterState == null) {
                logger.warn("Received unknown update in in-wall switch: {}", state);
            } else {
                updatePowerMeterState(powerMeterState);
            }
        }
    }
}
