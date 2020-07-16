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
package org.openhab.binding.e3dc.internal.handler;

import static org.openhab.binding.e3dc.internal.E3DCBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.e3dc.internal.dto.EmergencyBlock;
import org.openhab.binding.e3dc.internal.modbus.Data.DataType;
import org.openhab.binding.e3dc.internal.modbus.ModbusDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link E3DCEmergencyHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class E3DCEmergencyHandler extends BaseHandler {

    private final Logger logger = LoggerFactory.getLogger(E3DCEmergencyHandler.class);

    public E3DCEmergencyHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no commands possible for Info block
    }

    @Override
    public void dataAvailable(ModbusDataProvider provider) {
        EmergencyBlock block = (EmergencyBlock) provider.getData(DataType.EMERGENCY);
        updateState(PV_POWER_SUPPLY_CHANNEL, block.pvPowerSupply);
        updateState(BATTERY_POWER_SUPPLY_CHANNEL, block.batteryPowerSupply);
        updateState(BATTERY_POWER_CONSUMPTION, block.batteryPowerConsumption);
        updateState(HOUSEHOLD_POWER_CONSUMPTION_CHANNEL, block.householdPowerConsumption);
        updateState(GRID_POWER_CONSUMPTION_CHANNEL, block.gridPowerConsumpition);
        updateState(GRID_POWER_SUPPLY_CHANNEL, block.gridPowerSupply);
        updateState(EXTERNAL_POWER_SUPPLY_CHANNEL, block.externalPowerSupply);
        updateState(WALLBOX_POWER_CONSUMPTION_CHANNEL, block.wallboxPowerConsumption);
        updateState(WALLBOX_PV_POWER_CONSUMPTION_CHANNEL, block.wallboxPVPowerConsumption);
        updateState(AUTARKY, block.autarky);
        updateState(SELF_CONSUMPTION, block.selfConsumption);
        updateState(BATTERY_STATE_OF_CHARGE_CHANNEL, block.batterySOC);
    }
}
