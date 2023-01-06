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
package org.openhab.binding.boschshc.internal.devices.wallthermostat;

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.devices.AbstractBatteryPoweredDeviceHandler;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.humiditylevel.HumidityLevelService;
import org.openhab.binding.boschshc.internal.services.humiditylevel.dto.HumidityLevelServiceState;
import org.openhab.binding.boschshc.internal.services.temperaturelevel.TemperatureLevelService;
import org.openhab.binding.boschshc.internal.services.temperaturelevel.dto.TemperatureLevelServiceState;
import org.openhab.core.thing.Thing;

/**
 * Handler for a wall thermostat device.
 *
 * @author Christian Oeing - Initial contribution
 */
@NonNullByDefault
public final class WallThermostatHandler extends AbstractBatteryPoweredDeviceHandler {

    public WallThermostatHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void initializeServices() throws BoschSHCException {
        super.initializeServices();

        this.createService(TemperatureLevelService::new, this::updateChannels, List.of(CHANNEL_TEMPERATURE));
        this.createService(HumidityLevelService::new, this::updateChannels, List.of(CHANNEL_HUMIDITY));
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
     * Updates the channels which are linked to the {@link HumidityLevelService} of the device.
     *
     * @param state Current state of {@link HumidityLevelService}.
     */
    private void updateChannels(HumidityLevelServiceState state) {
        super.updateState(CHANNEL_HUMIDITY, state.getHumidityState());
    }
}
