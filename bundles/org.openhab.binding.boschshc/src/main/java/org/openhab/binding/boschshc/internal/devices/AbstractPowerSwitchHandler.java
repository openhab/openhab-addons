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
package org.openhab.binding.boschshc.internal.devices;

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.powermeter.PowerMeterService;
import org.openhab.binding.boschshc.internal.services.powermeter.dto.PowerMeterServiceState;
import org.openhab.binding.boschshc.internal.services.powerswitch.PowerSwitchService;
import org.openhab.binding.boschshc.internal.services.powerswitch.PowerSwitchState;
import org.openhab.binding.boschshc.internal.services.powerswitch.dto.PowerSwitchServiceState;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * Abstract handler implementation for devices with power switches and energy monitoring.
 * <p>
 * This implementation provides the functionality to
 * <ul>
 * <li>Switch the device on and off using the <code>PowerSwitch</code> service</li>
 * <li>Measuring the current power consumption and the overall energy consumption using the <code>PowerMeter</code>
 * service</li>
 * </ul>
 *
 * @author David Pace - Initial contribution (extracted from LightControlHandler)
 */
@NonNullByDefault
public abstract class AbstractPowerSwitchHandler extends BoschSHCDeviceHandler {

    /**
     * Service for switching the device on and off
     */
    private final PowerSwitchService powerSwitchService;

    protected AbstractPowerSwitchHandler(Thing thing) {
        super(thing);
        this.powerSwitchService = new PowerSwitchService();
    }

    @Override
    protected void initializeServices() throws BoschSHCException {
        super.initializeServices();

        this.registerService(this.powerSwitchService, this::updateChannels, List.of(CHANNEL_POWER_SWITCH), true);
        this.createService(PowerMeterService::new, this::updateChannels,
                List.of(CHANNEL_POWER_CONSUMPTION, CHANNEL_ENERGY_CONSUMPTION), true);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        switch (channelUID.getId()) {
            case CHANNEL_POWER_SWITCH:
                if (command instanceof OnOffType onOffCommand) {
                    updatePowerSwitchState(onOffCommand);
                }
                break;
        }
    }

    /**
     * Updates the channels which are linked to the {@link PowerMeterService} of the device.
     *
     * @param state Current state of {@link PowerMeterService}.
     */
    private void updateChannels(PowerMeterServiceState state) {
        super.updateState(CHANNEL_POWER_CONSUMPTION, new QuantityType<>(state.powerConsumption, Units.WATT));
        super.updateState(CHANNEL_ENERGY_CONSUMPTION, new QuantityType<>(state.energyConsumption, Units.WATT_HOUR));
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
        this.updateServiceState(this.powerSwitchService, state);
    }
}
