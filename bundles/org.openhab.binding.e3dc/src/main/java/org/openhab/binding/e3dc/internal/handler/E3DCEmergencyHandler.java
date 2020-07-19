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
        // no commands possible for EMS block
    }

    @Override
    public void initialize() {
        super.initialize(DataType.EMERGENCY);
    }

    @Override
    public void dataAvailable(ModbusDataProvider provider) {
        EmergencyBlock block = (EmergencyBlock) provider.getData(DataType.EMERGENCY);
        if (block != null) {
            updateState(EMERGENCY_POWER_STATUS, block.epStatus);
            updateState(BATTERY_LOADING_LOCKED, block.batteryLoadingLocked);
            updateState(BATTERY_UNLOADING_LOCKED, block.batterUnLoadingLocked);
            updateState(EMERGENCY_POWER_POSSIBLE, block.epPossible);
            updateState(WEATHER_PREDICTION_LOADING, block.weatherPredictedLoading);
            updateState(REGULATION_STATUS, block.regulationStatus);
            updateState(LOADING_LOCK_TIME, block.loadingLockTime);
            updateState(UNLOADING_LOCKTIME, block.unloadingLockTime);
        } else {
            logger.debug("Unable to get {} from provider {}", DataType.EMERGENCY, provider.toString());
        }
    }
}
