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

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_ENERGY_CONSUMPTION;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_POWER_CONSUMPTION;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.powermeter.PowerMeterService;
import org.openhab.binding.boschshc.internal.services.powermeter.dto.PowerMeterServiceState;
import org.openhab.binding.boschshc.internal.services.powerswitch.PowerSwitchService;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;

/**
 * Abstract handler implementation for devices providing a {@link PowerSwitchService} and a {@link PowerMeterService}.
 * <p>
 * Examples for such devices are smart plugs and in-wall switches.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public abstract class AbstractPowerSwitchHandlerWithPowerMeter extends AbstractPowerSwitchHandler {

    protected AbstractPowerSwitchHandlerWithPowerMeter(Thing thing) {
        super(thing);
    }

    @Override
    protected void initializeServices() throws BoschSHCException {
        super.initializeServices();

        this.createService(PowerMeterService::new, this::updateChannels,
                List.of(CHANNEL_POWER_CONSUMPTION, CHANNEL_ENERGY_CONSUMPTION), true);
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
}
