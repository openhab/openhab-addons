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
package org.openhab.binding.boschshc.internal.devices.climatecontrol;

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.devices.BoschSHCDeviceHandler;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.roomclimatecontrol.RoomClimateControlService;
import org.openhab.binding.boschshc.internal.services.roomclimatecontrol.dto.RoomClimateControlServiceState;
import org.openhab.binding.boschshc.internal.services.temperaturelevel.TemperatureLevelService;
import org.openhab.binding.boschshc.internal.services.temperaturelevel.dto.TemperatureLevelServiceState;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A virtual device which controls up to six Bosch Smart Home radiator thermostats in a room.
 *
 * @author Christian Oeing - Initial contribution
 */
@NonNullByDefault
public final class ClimateControlHandler extends BoschSHCDeviceHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private RoomClimateControlService roomClimateControlService;

    /**
     * Constructor.
     *
     * @param thing The Bosch Smart Home device that should be handled.
     */
    public ClimateControlHandler(Thing thing) {
        super(thing);
        this.roomClimateControlService = new RoomClimateControlService();
    }

    @Override
    protected void initializeServices() throws BoschSHCException {
        super.createService(TemperatureLevelService::new, this::updateChannels, List.of(CHANNEL_TEMPERATURE));
        super.registerService(this.roomClimateControlService, this::updateChannels,
                List.of(CHANNEL_SETPOINT_TEMPERATURE));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        switch (channelUID.getId()) {
            case CHANNEL_SETPOINT_TEMPERATURE:
                if (command instanceof QuantityType<?> temperature) {
                    updateSetpointTemperature(temperature);
                }
                break;
        }
    }

    /**
     * Updates the channels which are linked to the {@link TemperatureLevelService} of the device.
     *
     * @param state Current state of {@link TemperatureLevelService}.
     */
    private void updateChannels(TemperatureLevelServiceState state) {
        super.updateState(CHANNEL_TEMPERATURE, state.getTemperatureState());
    }

    /**
     * Updates the channels which are linked to the {@link RoomClimateControlService} of the device.
     *
     * @param state Current state of {@link RoomClimateControlService}.
     */
    private void updateChannels(RoomClimateControlServiceState state) {
        super.updateState(CHANNEL_SETPOINT_TEMPERATURE, state.getSetpointTemperatureState());
    }

    /**
     * Sets the desired temperature for the device.
     *
     * @param quantityType Command which contains the new desired temperature.
     */
    private void updateSetpointTemperature(QuantityType<?> quantityType) {
        QuantityType<?> celsiusType = quantityType.toUnit(SIUnits.CELSIUS);
        if (celsiusType == null) {
            logger.debug("Could not convert quantity command to celsius");
            return;
        }

        double setpointTemperature = celsiusType.doubleValue();
        this.updateServiceState(this.roomClimateControlService,
                new RoomClimateControlServiceState(setpointTemperature));
    }
}
