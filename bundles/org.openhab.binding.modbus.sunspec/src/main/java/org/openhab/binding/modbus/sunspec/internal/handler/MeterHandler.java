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
package org.openhab.binding.modbus.sunspec.internal.handler;

import static org.openhab.binding.modbus.sunspec.internal.SunSpecConstants.*;
import static org.openhab.core.library.unit.Units.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.sunspec.internal.dto.MeterModelBlock;
import org.openhab.binding.modbus.sunspec.internal.parser.MeterModelParser;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This handler is responsible for handling data recieved from a sunspec meter
 *
 * @author Nagy Attila Gabor - Initial contribution
 *
 */
@NonNullByDefault
public class MeterHandler extends AbstractSunSpecHandler {

    /**
     * Parser used to convert incoming raw messages into model blocks
     */
    private final MeterModelParser parser = new MeterModelParser();

    /**
     * Logger instance
     */
    private final Logger logger = LoggerFactory.getLogger(MeterHandler.class);

    public MeterHandler(Thing thing) {
        super(thing);
    }

    /**
     * Receive polled data, parse then update states
     */
    @Override
    protected void handlePolledData(ModbusRegisterArray registers) {
        logger.trace("Model block received, size: {}", registers.size());

        MeterModelBlock block = parser.parse(registers);

        // AC General group
        updateTotalValues(block);

        updatePhaseValues(block, block.phaseA, GROUP_AC_PHASE_A);

        // Split phase, wye/delta phase
        if (block.sunspecDID >= METER_SPLIT_PHASE && (thing.getThingTypeUID().equals(THING_TYPE_METER_SPLIT_PHASE)
                || thing.getThingTypeUID().equals(THING_TYPE_METER_WYE_PHASE)
                || thing.getThingTypeUID().equals(THING_TYPE_METER_DELTA_PHASE))) {
            updatePhaseValues(block, block.phaseB, GROUP_AC_PHASE_B);
        }

        // Three phase (wye/delta) only
        if (block.sunspecDID >= INVERTER_THREE_PHASE && (thing.getThingTypeUID().equals(THING_TYPE_METER_WYE_PHASE)
                || thing.getThingTypeUID().equals(THING_TYPE_METER_DELTA_PHASE))) {
            updatePhaseValues(block, block.phaseC, GROUP_AC_PHASE_C);
        }

        resetCommunicationError();
    }

    /**
     * Update the total states from the received block
     *
     * @param block
     */
    private void updateTotalValues(MeterModelBlock block) {
        updateState(channelUID(GROUP_AC_GENERAL, CHANNEL_AC_TOTAL_CURRENT),
                getScaled(block.acCurrentTotal, block.acCurrentSF, AMPERE));

        updateState(channelUID(GROUP_AC_GENERAL, CHANNEL_AC_AVERAGE_VOLTAGE_TO_N),
                getScaled(block.acVoltageLineToNAverage, block.acVoltageSF, VOLT));

        updateState(channelUID(GROUP_AC_GENERAL, CHANNEL_AC_AVERAGE_VOLTAGE_TO_NEXT),
                getScaled(block.acVoltageLineToLineAverage, block.acVoltageSF, VOLT));

        updateState(channelUID(GROUP_AC_GENERAL, CHANNEL_AC_FREQUENCY),
                getScaled(block.acFrequency, block.acFrequencySF.orElse((short) 1), HERTZ));

        updateState(channelUID(GROUP_AC_GENERAL, CHANNEL_AC_TOTAL_REAL_POWER),
                getScaled(block.acRealPowerTotal, block.acRealPowerSF, WATT));

        updateState(channelUID(GROUP_AC_GENERAL, CHANNEL_AC_TOTAL_APPARENT_POWER),
                getScaled(block.acApparentPowerTotal, block.acApparentPowerSF, WATT)); // TODO: this should be VA

        updateState(channelUID(GROUP_AC_GENERAL, CHANNEL_AC_TOTAL_REACTIVE_POWER),
                getScaled(block.acReactivePowerTotal, block.acReactivePowerSF, WATT)); // TODO: this should be VAR

        updateState(channelUID(GROUP_AC_GENERAL, CHANNEL_AC_AVERAGE_POWER_FACTOR),
                getScaled(block.acPowerFactor, block.acPowerFactorSF, PERCENT));

        updateState(channelUID(GROUP_AC_GENERAL, CHANNEL_AC_TOTAL_EXPORTED_REAL_ENERGY),
                getScaled(block.acExportedRealEnergyTotal, block.acRealEnergySF, WATT_HOUR));

        updateState(channelUID(GROUP_AC_GENERAL, CHANNEL_AC_TOTAL_IMPORTED_REAL_ENERGY),
                getScaled(block.acImportedRealEnergyTotal, block.acRealEnergySF, WATT_HOUR));

        updateState(channelUID(GROUP_AC_GENERAL, CHANNEL_AC_TOTAL_EXPORTED_APPARENT_ENERGY),
                getScaled(block.acExportedApparentEnergyTotal, block.acApparentEnergySF, WATT_HOUR)); // TODO: this
                                                                                                      // should be
                                                                                                      // VA_HOUR

        updateState(channelUID(GROUP_AC_GENERAL, CHANNEL_AC_TOTAL_IMPORTED_APPARENT_ENERGY),
                getScaled(block.acImportedApparentEnergyTotal, block.acApparentEnergySF, WATT_HOUR)); // TODO: this
                                                                                                      // should be
                                                                                                      // VA_HOUR

        updateState(channelUID(GROUP_AC_GENERAL, CHANNEL_AC_TOTAL_IMPORTED_REACTIVE_ENERGY_Q1),
                getScaled(block.acImportedReactiveEnergyQ1Total, block.acReactiveEnergySF, WATT_HOUR)); // TODO: this
                                                                                                        // should be
                                                                                                        // VAR_HOUR

        updateState(channelUID(GROUP_AC_GENERAL, CHANNEL_AC_TOTAL_IMPORTED_REACTIVE_ENERGY_Q2),
                getScaled(block.acImportedReactiveEnergyQ2Total, block.acReactiveEnergySF, WATT_HOUR)); // TODO: this
                                                                                                        // should be
                                                                                                        // VAR_HOUR

        updateState(channelUID(GROUP_AC_GENERAL, CHANNEL_AC_TOTAL_EXPORTED_REACTIVE_ENERGY_Q3),
                getScaled(block.acExportedReactiveEnergyQ3Total, block.acReactiveEnergySF, WATT_HOUR)); // TODO: this
                                                                                                        // should be
                                                                                                        // VAR_HOUR

        updateState(channelUID(GROUP_AC_GENERAL, CHANNEL_AC_TOTAL_EXPORTED_REACTIVE_ENERGY_Q4),
                getScaled(block.acExportedReactiveEnergyQ4Total, block.acReactiveEnergySF, WATT_HOUR)); // TODO: this
                                                                                                        // should be
                                                                                                        // VAR_HOUR
    }

    /**
     * Update phase related channels for the selected phase.
     *
     * @param block the main block for scale
     * @param phaseBlock the block containing the raw values for the selected phase
     * @param group channel group id for the output
     */
    private void updatePhaseValues(MeterModelBlock block, MeterModelBlock.PhaseBlock phaseBlock, String group) {
        updateState(channelUID(group, CHANNEL_AC_PHASE_CURRENT),
                getScaled(phaseBlock.acPhaseCurrent, block.acCurrentSF, AMPERE));

        updateState(channelUID(group, CHANNEL_AC_VOLTAGE_TO_N),
                getScaled(phaseBlock.acVoltageToN, block.acVoltageSF, VOLT));

        updateState(channelUID(group, CHANNEL_AC_VOLTAGE_TO_NEXT),
                getScaled(phaseBlock.acVoltageToNext, block.acVoltageSF, VOLT));

        updateState(channelUID(group, CHANNEL_AC_REAL_POWER),
                getScaled(phaseBlock.acRealPower, block.acRealPowerSF, WATT));

        updateState(channelUID(group, CHANNEL_AC_APPARENT_POWER),
                getScaled(phaseBlock.acApparentPower, block.acApparentPowerSF, WATT)); // TODO: this should be VA

        updateState(channelUID(group, CHANNEL_AC_REACTIVE_POWER),
                getScaled(phaseBlock.acReactivePower, block.acReactivePowerSF, WATT)); // TODO: this should be VAR

        updateState(channelUID(group, CHANNEL_AC_POWER_FACTOR),
                getScaled(phaseBlock.acPowerFactor, block.acPowerFactorSF, PERCENT));

        updateState(channelUID(group, CHANNEL_AC_EXPORTED_REAL_ENERGY),
                getScaled(phaseBlock.acExportedRealEnergy, block.acRealEnergySF, WATT_HOUR));

        updateState(channelUID(group, CHANNEL_AC_IMPORTED_REAL_ENERGY),
                getScaled(phaseBlock.acImportedRealEnergy, block.acRealEnergySF, WATT_HOUR));

        updateState(channelUID(group, CHANNEL_AC_EXPORTED_APPARENT_ENERGY),
                getScaled(phaseBlock.acExportedApparentEnergy, block.acApparentEnergySF, WATT_HOUR)); // TODO: this
                                                                                                      // should be
                                                                                                      // VA_HOUR

        updateState(channelUID(group, CHANNEL_AC_IMPORTED_APPARENT_ENERGY),
                getScaled(phaseBlock.acImportedApparentEnergy, block.acApparentEnergySF, WATT_HOUR)); // TODO: this
                                                                                                      // should be
                                                                                                      // VA_HOUR

        updateState(channelUID(group, CHANNEL_AC_IMPORTED_REACTIVE_ENERGY_Q1),
                getScaled(phaseBlock.acImportedReactiveEnergyQ1, block.acReactiveEnergySF, WATT_HOUR)); // TODO: this
                                                                                                        // should be
                                                                                                        // VAR_HOUR

        updateState(channelUID(group, CHANNEL_AC_IMPORTED_REACTIVE_ENERGY_Q2),
                getScaled(phaseBlock.acImportedReactiveEnergyQ2, block.acReactiveEnergySF, WATT_HOUR)); // TODO: this
                                                                                                        // should be
                                                                                                        // VAR_HOUR

        updateState(channelUID(group, CHANNEL_AC_EXPORTED_REACTIVE_ENERGY_Q3),
                getScaled(phaseBlock.acExportedReactiveEnergyQ3, block.acReactiveEnergySF, WATT_HOUR)); // TODO: this
                                                                                                        // should be
                                                                                                        // VAR_HOUR

        updateState(channelUID(group, CHANNEL_AC_EXPORTED_REACTIVE_ENERGY_Q4),
                getScaled(phaseBlock.acExportedReactiveEnergyQ4, block.acReactiveEnergySF, WATT_HOUR)); // TODO: this
                                                                                                        // should be
                                                                                                        // VAR_HOUR
    }
}
