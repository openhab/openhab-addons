/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.myenergi.internal.handler;

import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.*;
import static org.openhab.core.library.unit.Units.*;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.myenergi.internal.MyenergiApiClient;
import org.openhab.binding.myenergi.internal.dto.EddiSummary;
import org.openhab.binding.myenergi.internal.exception.ApiException;
import org.openhab.binding.myenergi.internal.exception.RecordNotFoundException;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MyenergiEddiHandler} is responsible for handling things created to represent Eddis.
 *
 * @author Stephen Cook - Initial Contribution
 */
@NonNullByDefault
public class MyenergiEddiHandler extends MyenergiBaseDeviceHandler {

    private final Logger logger = LoggerFactory.getLogger(MyenergiEddiHandler.class);

    public MyenergiEddiHandler(Thing thing, MyenergiApiClient apiClient) {
        super(thing, apiClient);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(MyenergiEddiActions.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Eddi handleCommand({},{})", channelUID, command.toFullString());
        if (command instanceof RefreshType) {
            updateThingCache.getValue();
        }
    }

    @Override
    protected void updateThing() {
        EddiSummary summary;
        try {
            summary = apiClient.getData().getEddiBySerialNumber(Long.parseLong(thing.getUID().getId()));
            logger.debug("Updating all thing channels for device : {}", summary.serialNumber);

            updateDateTimeState(EDDI_CHANNEL_LAST_UPDATED_TIME, summary.getLastUpdateTime());
            updateStringState(EDDI_CHANNEL_BOOST_MODE, summary.boostMode);
            updateEnergyState(EDDI_CHANNEL_ENERGY_TRANSFERRED, summary.energyTransferred, KILOWATT_HOUR);
            updateIntegerState(EDDI_CHANNEL_DIVERTED_POWER, summary.divertedPower);
            updateIntegerState(EDDI_CHANNEL_GENERATED_POWER, summary.generatedPower);
            updateIntegerState(EDDI_CHANNEL_CLAMP_POWER_1, summary.clampPower1);
            updateIntegerState(EDDI_CHANNEL_CLAMP_POWER_2, summary.clampPower2);
            updateIntegerState(EDDI_CHANNEL_CLAMP_POWER_3, summary.clampPower3);
            updateStringState(EDDI_CHANNEL_CLAMP_NAME_1, summary.clampName1);
            updateStringState(EDDI_CHANNEL_CLAMP_NAME_2, summary.clampName2);
            updateStringState(EDDI_CHANNEL_CLAMP_NAME_3, summary.clampName3);
            updateFrequencyState(EDDI_CHANNEL_SUPPLY_FREQUENCY, summary.supplyFrequency, HERTZ);
            updateIntegerState(EDDI_CHANNEL_GRID_POWER, summary.gridPower);
            updateIntegerState(EDDI_CHANNEL_ACTIVE_HEATER, summary.activeHeater);
            updateStringState(EDDI_CHANNEL_HEATER_NAME_1, summary.heaterName1);
            updateStringState(EDDI_CHANNEL_HEATER_NAME_2, summary.heaterName2);
            updateIntegerState(EDDI_CHANNEL_PHASE, summary.phase);
            updateIntegerState(EDDI_CHANNEL_DIVERTER_PRIORITY, summary.diverterPriority);
            updateIntegerState(EDDI_CHANNEL_HEATER_PRIORITY, summary.heaterPriority);
            updateIntegerState(EDDI_CHANNEL_BOOST_REMAINING, summary.boostRemaining);
            updateStringState(EDDI_CHANNEL_STATUS, summary.status.toString());
            updateTemperatureState(EDDI_CHANNEL_TEMPERATURE_1, summary.temperature1, KELVIN);
            updateTemperatureState(EDDI_CHANNEL_TEMPERATURE_2, summary.temperature2, KELVIN);
            updateElectricPotentialState(EDDI_CHANNEL_SUPPLY_VOLTAGE, summary.supplyVoltageInTenthVolt / 10.0f, VOLT);
        } catch (RecordNotFoundException e) {
            logger.warn("Trying to update unknown device: {}", thing.getUID().getId());
        }
    }

    @Override
    protected void refreshMeasurements() throws ApiException {
        try {
            apiClient.updateEddiSummary(serialNumber);
        } catch (RecordNotFoundException e) {
            logger.warn("invalid serial number: {}", serialNumber, e);
        }
    }
}
