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
package org.openhab.binding.modbus.sunspec.internal.handler;

import static org.eclipse.smarthome.core.library.unit.SmartHomeUnits.*;
import static org.openhab.binding.modbus.sunspec.internal.SunSpecConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.modbus.sunspec.internal.dto.MeterModelBlock;
import org.openhab.binding.modbus.sunspec.internal.parser.MeterModelParser;
import org.openhab.io.transport.modbus.ModbusManager;
import org.openhab.io.transport.modbus.ModbusRegisterArray;
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

    public MeterHandler(Thing thing, ModbusManager managerRef) {
        super(thing, managerRef);
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

        updatePhaseAValues(block);

        // Split phase, wye/delta phase
        if (block.sunspecDID >= METER_SPLIT_PHASE && (thing.getThingTypeUID().equals(THING_TYPE_METER_SPLIT_PHASE)
                || thing.getThingTypeUID().equals(THING_TYPE_METER_WYE_PHASE)
                || thing.getThingTypeUID().equals(THING_TYPE_METER_DELTA_PHASE))) {
            updatePhaseBValues(block);
        }

        // Three phase (wye/delta) only
        if (block.sunspecDID >= INVERTER_THREE_PHASE && (thing.getThingTypeUID().equals(THING_TYPE_METER_WYE_PHASE)
                || thing.getThingTypeUID().equals(THING_TYPE_METER_DELTA_PHASE))) {
            updatePhaseCValues(block);
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
     * Update the phase A states from the received block
     *
     * @param block
     */
    private void updatePhaseAValues(MeterModelBlock block) {
        String group = GROUP_AC_PHASE_A;
        updateState(channelUID(group, CHANNEL_AC_PHASE_CURRENT),
                getScaled(block.acCurrentPhaseA, block.acCurrentSF, AMPERE));

        updateState(channelUID(group, CHANNEL_AC_VOLTAGE_TO_N),
                getScaled(block.acVoltageAtoN, block.acVoltageSF, VOLT));

        updateState(channelUID(group, CHANNEL_AC_VOLTAGE_TO_NEXT),
                getScaled(block.acVoltageAB, block.acVoltageSF, VOLT));

        updateState(channelUID(group, CHANNEL_AC_REAL_POWER),
                getScaled(block.acRealPowerPhaseA, block.acRealPowerSF, WATT));

        updateState(channelUID(group, CHANNEL_AC_APPARENT_POWER),
                getScaled(block.acApparentPowerPhaseA, block.acApparentPowerSF, WATT)); // TODO: this should be VA

        updateState(channelUID(group, CHANNEL_AC_REACTIVE_POWER),
                getScaled(block.acReactivePowerPhaseA, block.acReactivePowerSF, WATT)); // TODO: this should be VAR

        updateState(channelUID(group, CHANNEL_AC_POWER_FACTOR),
                getScaled(block.acPowerFactorPhaseA, block.acPowerFactorSF, PERCENT));

        updateState(channelUID(group, CHANNEL_AC_EXPORTED_REAL_ENERGY),
                getScaled(block.acExportedRealEnergyPhaseA, block.acRealEnergySF, WATT_HOUR));

        updateState(channelUID(group, CHANNEL_AC_IMPORTED_REAL_ENERGY),
                getScaled(block.acImportedRealEnergyPhaseA, block.acRealEnergySF, WATT_HOUR));

        updateState(channelUID(group, CHANNEL_AC_EXPORTED_APPARENT_ENERGY),
                getScaled(block.acExportedApparentEnergyPhaseA, block.acApparentEnergySF, WATT_HOUR)); // TODO: this
                                                                                                       // should be
                                                                                                       // VA_HOUR

        updateState(channelUID(group, CHANNEL_AC_IMPORTED_APPARENT_ENERGY),
                getScaled(block.acImportedApparentEnergyPhaseA, block.acApparentEnergySF, WATT_HOUR)); // TODO: this
                                                                                                       // should be
                                                                                                       // VA_HOUR

        updateState(channelUID(group, CHANNEL_AC_IMPORTED_REACTIVE_ENERGY_Q1),
                getScaled(block.acImportedReactiveEnergyQ1PhaseA, block.acReactiveEnergySF, WATT_HOUR)); // TODO: this
                                                                                                         // should be
                                                                                                         // VAR_HOUR

        updateState(channelUID(group, CHANNEL_AC_IMPORTED_REACTIVE_ENERGY_Q2),
                getScaled(block.acImportedReactiveEnergyQ2PhaseA, block.acReactiveEnergySF, WATT_HOUR)); // TODO: this
                                                                                                         // should be
                                                                                                         // VAR_HOUR

        updateState(channelUID(group, CHANNEL_AC_EXPORTED_REACTIVE_ENERGY_Q3),
                getScaled(block.acExportedReactiveEnergyQ3PhaseA, block.acReactiveEnergySF, WATT_HOUR)); // TODO: this
                                                                                                         // should be
                                                                                                         // VAR_HOUR

        updateState(channelUID(group, CHANNEL_AC_EXPORTED_REACTIVE_ENERGY_Q4),
                getScaled(block.acExportedReactiveEnergyQ4PhaseA, block.acReactiveEnergySF, WATT_HOUR)); // TODO: this
                                                                                                         // should be
                                                                                                         // VAR_HOUR
    }

    /**
     * Update the phase A states from the received block
     *
     * @param block
     */
    private void updatePhaseBValues(MeterModelBlock block) {
        String group = GROUP_AC_PHASE_B;
        updateState(channelUID(group, CHANNEL_AC_PHASE_CURRENT),
                getScaled(block.acCurrentPhaseB, block.acCurrentSF, AMPERE));

        updateState(channelUID(group, CHANNEL_AC_VOLTAGE_TO_N),
                getScaled(block.acVoltageBtoN, block.acVoltageSF, VOLT));

        updateState(channelUID(group, CHANNEL_AC_VOLTAGE_TO_NEXT),
                getScaled(block.acVoltageBC, block.acVoltageSF, VOLT));

        updateState(channelUID(group, CHANNEL_AC_REAL_POWER),
                getScaled(block.acRealPowerPhaseB, block.acRealPowerSF, WATT));

        updateState(channelUID(group, CHANNEL_AC_APPARENT_POWER),
                getScaled(block.acApparentPowerPhaseB, block.acApparentPowerSF, WATT)); // TODO: this should be VA

        updateState(channelUID(group, CHANNEL_AC_REACTIVE_POWER),
                getScaled(block.acReactivePowerPhaseB, block.acReactivePowerSF, WATT)); // TODO: this should be VAR

        updateState(channelUID(group, CHANNEL_AC_POWER_FACTOR),
                getScaled(block.acPowerFactorPhaseB, block.acPowerFactorSF, PERCENT));

        updateState(channelUID(group, CHANNEL_AC_EXPORTED_REAL_ENERGY),
                getScaled(block.acExportedRealEnergyPhaseB, block.acRealEnergySF, WATT_HOUR));

        updateState(channelUID(group, CHANNEL_AC_IMPORTED_REAL_ENERGY),
                getScaled(block.acImportedRealEnergyPhaseB, block.acRealEnergySF, WATT_HOUR));

        updateState(channelUID(group, CHANNEL_AC_EXPORTED_APPARENT_ENERGY),
                getScaled(block.acExportedApparentEnergyPhaseB, block.acApparentEnergySF, WATT_HOUR)); // TODO: this
                                                                                                       // should be
                                                                                                       // VA_HOUR

        updateState(channelUID(group, CHANNEL_AC_IMPORTED_APPARENT_ENERGY),
                getScaled(block.acImportedApparentEnergyPhaseB, block.acApparentEnergySF, WATT_HOUR)); // TODO: this
                                                                                                       // should be
                                                                                                       // VA_HOUR

        updateState(channelUID(group, CHANNEL_AC_IMPORTED_REACTIVE_ENERGY_Q1),
                getScaled(block.acImportedReactiveEnergyQ1PhaseB, block.acReactiveEnergySF, WATT_HOUR)); // TODO: this
                                                                                                         // should be
                                                                                                         // VAR_HOUR

        updateState(channelUID(group, CHANNEL_AC_IMPORTED_REACTIVE_ENERGY_Q2),
                getScaled(block.acImportedReactiveEnergyQ2PhaseB, block.acReactiveEnergySF, WATT_HOUR)); // TODO: this
                                                                                                         // should be
                                                                                                         // VAR_HOUR

        updateState(channelUID(group, CHANNEL_AC_EXPORTED_REACTIVE_ENERGY_Q3),
                getScaled(block.acExportedReactiveEnergyQ3PhaseB, block.acReactiveEnergySF, WATT_HOUR)); // TODO: this
                                                                                                         // should be
                                                                                                         // VAR_HOUR

        updateState(channelUID(group, CHANNEL_AC_EXPORTED_REACTIVE_ENERGY_Q4),
                getScaled(block.acExportedReactiveEnergyQ4PhaseB, block.acReactiveEnergySF, WATT_HOUR)); // TODO: this
                                                                                                         // should be
                                                                                                         // VAR_HOUR
    }

    /**
     * Update the phase C states from the received block
     *
     * @param block
     */
    private void updatePhaseCValues(MeterModelBlock block) {
        String group = GROUP_AC_PHASE_C;
        updateState(channelUID(group, CHANNEL_AC_PHASE_CURRENT),
                getScaled(block.acCurrentPhaseC, block.acCurrentSF, AMPERE));

        updateState(channelUID(group, CHANNEL_AC_VOLTAGE_TO_N),
                getScaled(block.acVoltageCtoN, block.acVoltageSF, VOLT));

        updateState(channelUID(group, CHANNEL_AC_VOLTAGE_TO_NEXT),
                getScaled(block.acVoltageCA, block.acVoltageSF, VOLT));

        updateState(channelUID(group, CHANNEL_AC_REAL_POWER),
                getScaled(block.acRealPowerPhaseC, block.acRealPowerSF, WATT));

        updateState(channelUID(group, CHANNEL_AC_APPARENT_POWER),
                getScaled(block.acApparentPowerPhaseC, block.acApparentPowerSF, WATT)); // TODO: this should be VA

        updateState(channelUID(group, CHANNEL_AC_REACTIVE_POWER),
                getScaled(block.acReactivePowerPhaseC, block.acReactivePowerSF, WATT)); // TODO: this should be VAR

        updateState(channelUID(group, CHANNEL_AC_POWER_FACTOR),
                getScaled(block.acPowerFactorPhaseC, block.acPowerFactorSF, PERCENT));

        updateState(channelUID(group, CHANNEL_AC_EXPORTED_REAL_ENERGY),
                getScaled(block.acExportedRealEnergyPhaseC, block.acRealEnergySF, WATT_HOUR));

        updateState(channelUID(group, CHANNEL_AC_IMPORTED_REAL_ENERGY),
                getScaled(block.acImportedRealEnergyPhaseC, block.acRealEnergySF, WATT_HOUR));

        updateState(channelUID(group, CHANNEL_AC_EXPORTED_APPARENT_ENERGY),
                getScaled(block.acExportedApparentEnergyPhaseC, block.acApparentEnergySF, WATT_HOUR)); // TODO: this
                                                                                                       // should be
                                                                                                       // VA_HOUR

        updateState(channelUID(group, CHANNEL_AC_IMPORTED_APPARENT_ENERGY),
                getScaled(block.acImportedApparentEnergyPhaseC, block.acApparentEnergySF, WATT_HOUR)); // TODO: this
                                                                                                       // should be
                                                                                                       // VA_HOUR

        updateState(channelUID(group, CHANNEL_AC_IMPORTED_REACTIVE_ENERGY_Q1),
                getScaled(block.acImportedReactiveEnergyQ1PhaseC, block.acReactiveEnergySF, WATT_HOUR)); // TODO: this
                                                                                                         // should be
                                                                                                         // VAR_HOUR

        updateState(channelUID(group, CHANNEL_AC_IMPORTED_REACTIVE_ENERGY_Q2),
                getScaled(block.acImportedReactiveEnergyQ2PhaseC, block.acReactiveEnergySF, WATT_HOUR)); // TODO: this
                                                                                                         // should be
                                                                                                         // VAR_HOUR

        updateState(channelUID(group, CHANNEL_AC_EXPORTED_REACTIVE_ENERGY_Q3),
                getScaled(block.acExportedReactiveEnergyQ3PhaseC, block.acReactiveEnergySF, WATT_HOUR)); // TODO: this
                                                                                                         // should be
                                                                                                         // VAR_HOUR

        updateState(channelUID(group, CHANNEL_AC_EXPORTED_REACTIVE_ENERGY_Q4),
                getScaled(block.acExportedReactiveEnergyQ4PhaseC, block.acReactiveEnergySF, WATT_HOUR)); // TODO: this
                                                                                                         // should be
                                                                                                         // VAR_HOUR
    }

}
