/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.devices.thermostat;

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.devices.AbstractBatteryPoweredDeviceHandler;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.childlock.ChildLockService;
import org.openhab.binding.boschshc.internal.services.childlock.dto.ChildLockServiceState;
import org.openhab.binding.boschshc.internal.services.temperaturelevel.TemperatureLevelService;
import org.openhab.binding.boschshc.internal.services.temperaturelevel.dto.TemperatureLevelServiceState;
import org.openhab.binding.boschshc.internal.services.valvetappet.ValveTappetService;
import org.openhab.binding.boschshc.internal.services.valvetappet.dto.ValveTappetServiceState;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * Handler for a thermostat device.
 *
 * @author Christian Oeing - Initial contribution
 */
@NonNullByDefault
public final class ThermostatHandler extends AbstractBatteryPoweredDeviceHandler {

    private ChildLockService childLockService;

    public ThermostatHandler(Thing thing) {
        super(thing);
        this.childLockService = new ChildLockService();
    }

    @Override
    protected void initializeServices() throws BoschSHCException {
        super.initializeServices();

        this.createService(TemperatureLevelService::new, this::updateChannels, List.of(CHANNEL_TEMPERATURE));
        this.createService(ValveTappetService::new, this::updateChannels, List.of(CHANNEL_VALVE_TAPPET_POSITION));
        this.registerService(this.childLockService, this::updateChannels, List.of(CHANNEL_CHILD_LOCK));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        switch (channelUID.getId()) {
            case CHANNEL_CHILD_LOCK:
                this.handleServiceCommand(this.childLockService, command);
                break;
        }
    }

    /**
     * Updates the channels which are linked to the {@link TemperatureLevelService}
     * of the device.
     *
     * @param state Current state of {@link TemperatureLevelService}.
     */
    private void updateChannels(TemperatureLevelServiceState state) {
        super.updateState(CHANNEL_TEMPERATURE, state.getTemperatureState());
    }

    /**
     * Updates the channels which are linked to the {@link ValveTappetService} of
     * the device.
     *
     * @param state Current state of {@link ValveTappetService}.
     */
    private void updateChannels(ValveTappetServiceState state) {
        super.updateState(CHANNEL_VALVE_TAPPET_POSITION, state.getPositionState());
    }

    /**
     * Updates the channels which are linked to the {@link ChildLockService} of the
     * device.
     *
     * @param state Current state of {@link ChildLockService}.
     */
    private void updateChannels(ChildLockServiceState state) {
        super.updateState(CHANNEL_CHILD_LOCK, state.getActiveState());
    }
}
