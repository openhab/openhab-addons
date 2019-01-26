/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import static org.openhab.binding.modbus.sunspec.internal.SunSpecBindingConstants.*;

import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.modbus.sunspec.internal.block.MeterModelBlock;
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
public class MeterHandler extends AbstractSunSpecHandler {

    /**
     * Parser used to convert incoming raw messages into model blocks
     */
    private MeterModelParser parser = new MeterModelParser();

    /**
     * Logger instance
     */
    private final Logger logger = LoggerFactory.getLogger(MeterHandler.class);

    public MeterHandler(Thing thing, Supplier<@NonNull ModbusManager> managerRef) {
        super(thing, managerRef);
    }

    /**
     * Receive polled data, parse then update states
     */
    @Override
    protected void handlePolledData(@NonNull ModbusRegisterArray registers) {
        logger.trace("Model block received, size: {}", registers.size());

        MeterModelBlock block = parser.parse(registers);

        // AC General group
        updateTotalValues(block);

        updatePhaseAValues(block);

        // Split phase, wye/delta phase
        if (block.getSunspecDID() >= METER_SPLIT_PHASE && (thing.getThingTypeUID().equals(THING_TYPE_METER_SPLIT_PHASE)
                || thing.getThingTypeUID().equals(THING_TYPE_METER_WYE_PHASE)
                || thing.getThingTypeUID().equals(THING_TYPE_METER_DELTA_PHASE))) {
            updatePhaseBValues(block);
        }

        // Three phase (wye/delta) only
        if (block.getSunspecDID() >= INVERTER_THREE_PHASE && (thing.getThingTypeUID().equals(THING_TYPE_METER_WYE_PHASE)
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
        updateState(new ChannelUID(getThing().getUID(), GROUP_AC_GENERAL, CHANNEL_AC_TOTAL_CURRENT),
                getScaled(block.getAcCurrentTotal(), block.getAcCurrentSF()));

        updateState(new ChannelUID(getThing().getUID(), GROUP_AC_GENERAL, CHANNEL_AC_AVERAGE_VOLTAGE_TO_N),
                getScaled(block.getAcVoltageLinetoNAverage(), block.getAcVoltageSF()));

        updateState(new ChannelUID(getThing().getUID(), GROUP_AC_GENERAL, CHANNEL_AC_AVERAGE_VOLTAGE_TO_NEXT),
                getScaled(block.getAcVoltageLineToLineAverage(), block.getAcVoltageSF()));

        updateState(new ChannelUID(getThing().getUID(), GROUP_AC_GENERAL, CHANNEL_AC_FREQUENCY),
                getScaled(block.getAcFrequency(), block.getAcFrequencySF()));

        updateState(new ChannelUID(getThing().getUID(), GROUP_AC_GENERAL, CHANNEL_AC_TOTAL_REAL_POWER),
                getScaled(block.getAcRealPowerTotal(), block.getAcRealPowerSF()));

        updateState(new ChannelUID(getThing().getUID(), GROUP_AC_GENERAL, CHANNEL_AC_TOTAL_APPARENT_POWER),
                getScaled(block.getAcApparentPowerTotal(), block.getAcApparentPowerSF()));

        updateState(new ChannelUID(getThing().getUID(), GROUP_AC_GENERAL, CHANNEL_AC_TOTAL_REACTIVE_POWER),
                getScaled(block.getAcReactivePowerTotal(), block.getAcReactivePowerSF()));

        updateState(new ChannelUID(getThing().getUID(), GROUP_AC_GENERAL, CHANNEL_AC_AVERAGE_POWER_FACTOR),
                getScaled(block.getAcPowerFactor(), block.getAcPowerFactorSF()));

        updateState(new ChannelUID(getThing().getUID(), GROUP_AC_GENERAL, CHANNEL_AC_TOTAL_EXPORTED_REAL_ENERGY),
                getScaled(block.getAcExportedRealEnergyTotal(), block.getAcRealEnergySF()));

        updateState(new ChannelUID(getThing().getUID(), GROUP_AC_GENERAL, CHANNEL_AC_TOTAL_IMPORTED_REAL_ENERGY),
                getScaled(block.getAcImportedRealEnergyTotal(), block.getAcRealEnergySF()));

        updateState(new ChannelUID(getThing().getUID(), GROUP_AC_GENERAL, CHANNEL_AC_TOTAL_EXPORTED_APPARENT_ENERGY),
                getScaled(block.getAcExportedApparentEnergyTotal(), block.getAcApparentEnergySF()));

        updateState(new ChannelUID(getThing().getUID(), GROUP_AC_GENERAL, CHANNEL_AC_TOTAL_IMPORTED_APPARENT_ENERGY),
                getScaled(block.getAcImportedApparentEnergyTotal(), block.getAcApparentEnergySF()));

        updateState(new ChannelUID(getThing().getUID(), GROUP_AC_GENERAL, CHANNEL_AC_TOTAL_IMPORTED_REACTIVE_ENERGY_Q1),
                getScaled(block.getAcImportedReactiveEnergyQ1Total(), block.getAcReactiveEnergySF()));

        updateState(new ChannelUID(getThing().getUID(), GROUP_AC_GENERAL, CHANNEL_AC_TOTAL_IMPORTED_REACTIVE_ENERGY_Q2),
                getScaled(block.getAcImportedReactiveEnergyQ2Total(), block.getAcReactiveEnergySF()));

        updateState(new ChannelUID(getThing().getUID(), GROUP_AC_GENERAL, CHANNEL_AC_TOTAL_EXPORTED_REACTIVE_ENERGY_Q3),
                getScaled(block.getAcExportedReactiveEnergyQ3Total(), block.getAcReactiveEnergySF()));

        updateState(new ChannelUID(getThing().getUID(), GROUP_AC_GENERAL, CHANNEL_AC_TOTAL_EXPORTED_REACTIVE_ENERGY_Q4),
                getScaled(block.getAcExportedReactiveEnergyQ4Total(), block.getAcReactiveEnergySF()));
    }

    /**
     * Update the phase A states from the received block
     *
     * @param block
     */
    private void updatePhaseAValues(MeterModelBlock block) {
        String group = GROUP_AC_PHASE_A;
        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_PHASE_CURRENT),
                getScaled(block.getAcCurrentPhaseA(), block.getAcCurrentSF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_VOLTAGE_TO_N),
                getScaled(block.getAcVoltageAtoN(), block.getAcVoltageSF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_VOLTAGE_TO_NEXT),
                getScaled(block.getAcVoltageAB(), block.getAcVoltageSF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_REAL_POWER),
                getScaled(block.getAcRealPowerPhaseA(), block.getAcRealPowerSF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_APPARENT_POWER),
                getScaled(block.getAcApparentPowerPhaseA(), block.getAcApparentPowerSF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_REACTIVE_POWER),
                getScaled(block.getAcReactivePowerPhaseA(), block.getAcReactivePowerSF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_POWER_FACTOR),
                getScaled(block.getAcPowerFactorPhaseA(), block.getAcPowerFactorSF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_EXPORTED_REAL_ENERGY),
                getScaled(block.getAcExportedRealEnergyPhaseA(), block.getAcRealEnergySF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_IMPORTED_REAL_ENERGY),
                getScaled(block.getAcImportedRealEnergyPhaseA(), block.getAcRealEnergySF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_EXPORTED_APPARENT_ENERGY),
                getScaled(block.getAcExportedApparentEnergyPhaseA(), block.getAcApparentEnergySF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_IMPORTED_APPARENT_ENERGY),
                getScaled(block.getAcImportedApparentEnergyPhaseA(), block.getAcApparentEnergySF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_IMPORTED_REACTIVE_ENERGY_Q1),
                getScaled(block.getAcImportedReactiveEnergyQ1PhaseA(), block.getAcReactiveEnergySF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_IMPORTED_REACTIVE_ENERGY_Q2),
                getScaled(block.getAcImportedReactiveEnergyQ2PhaseA(), block.getAcReactiveEnergySF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_EXPORTED_REACTIVE_ENERGY_Q3),
                getScaled(block.getAcExportedReactiveEnergyQ3PhaseA(), block.getAcReactiveEnergySF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_EXPORTED_REACTIVE_ENERGY_Q4),
                getScaled(block.getAcExportedReactiveEnergyQ4PhaseA(), block.getAcReactiveEnergySF()));
    }

    /**
     * Update the phase A states from the received block
     *
     * @param block
     */
    private void updatePhaseBValues(MeterModelBlock block) {
        String group = GROUP_AC_PHASE_B;
        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_PHASE_CURRENT),
                getScaled(block.getAcCurrentPhaseB(), block.getAcCurrentSF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_VOLTAGE_TO_N),
                getScaled(block.getAcVoltageBtoN(), block.getAcVoltageSF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_VOLTAGE_TO_NEXT),
                getScaled(block.getAcVoltageBC(), block.getAcVoltageSF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_REAL_POWER),
                getScaled(block.getAcRealPowerPhaseB(), block.getAcRealPowerSF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_APPARENT_POWER),
                getScaled(block.getAcApparentPowerPhaseB(), block.getAcApparentPowerSF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_REACTIVE_POWER),
                getScaled(block.getAcReactivePowerPhaseB(), block.getAcReactivePowerSF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_POWER_FACTOR),
                getScaled(block.getAcPowerFactorPhaseB(), block.getAcPowerFactorSF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_EXPORTED_REAL_ENERGY),
                getScaled(block.getAcExportedRealEnergyPhaseB(), block.getAcRealEnergySF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_IMPORTED_REAL_ENERGY),
                getScaled(block.getAcImportedRealEnergyPhaseB(), block.getAcRealEnergySF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_EXPORTED_APPARENT_ENERGY),
                getScaled(block.getAcExportedApparentEnergyPhaseB(), block.getAcApparentEnergySF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_IMPORTED_APPARENT_ENERGY),
                getScaled(block.getAcImportedApparentEnergyPhaseB(), block.getAcApparentEnergySF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_IMPORTED_REACTIVE_ENERGY_Q1),
                getScaled(block.getAcImportedReactiveEnergyQ1PhaseB(), block.getAcReactiveEnergySF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_IMPORTED_REACTIVE_ENERGY_Q2),
                getScaled(block.getAcImportedReactiveEnergyQ2PhaseB(), block.getAcReactiveEnergySF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_EXPORTED_REACTIVE_ENERGY_Q3),
                getScaled(block.getAcExportedReactiveEnergyQ3PhaseB(), block.getAcReactiveEnergySF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_EXPORTED_REACTIVE_ENERGY_Q4),
                getScaled(block.getAcExportedReactiveEnergyQ4PhaseB(), block.getAcReactiveEnergySF()));
    }

    /**
     * Update the phase C states from the received block
     *
     * @param block
     */
    private void updatePhaseCValues(MeterModelBlock block) {
        String group = GROUP_AC_PHASE_C;
        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_PHASE_CURRENT),
                getScaled(block.getAcCurrentPhaseC(), block.getAcCurrentSF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_VOLTAGE_TO_N),
                getScaled(block.getAcVoltageCtoN(), block.getAcVoltageSF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_VOLTAGE_TO_NEXT),
                getScaled(block.getAcVoltageCA(), block.getAcVoltageSF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_REAL_POWER),
                getScaled(block.getAcRealPowerPhaseC(), block.getAcRealPowerSF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_APPARENT_POWER),
                getScaled(block.getAcApparentPowerPhaseC(), block.getAcApparentPowerSF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_REACTIVE_POWER),
                getScaled(block.getAcReactivePowerPhaseC(), block.getAcReactivePowerSF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_POWER_FACTOR),
                getScaled(block.getAcPowerFactorPhaseC(), block.getAcPowerFactorSF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_EXPORTED_REAL_ENERGY),
                getScaled(block.getAcExportedRealEnergyPhaseC(), block.getAcRealEnergySF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_IMPORTED_REAL_ENERGY),
                getScaled(block.getAcImportedRealEnergyPhaseC(), block.getAcRealEnergySF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_EXPORTED_APPARENT_ENERGY),
                getScaled(block.getAcExportedApparentEnergyPhaseC(), block.getAcApparentEnergySF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_IMPORTED_APPARENT_ENERGY),
                getScaled(block.getAcImportedApparentEnergyPhaseC(), block.getAcApparentEnergySF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_IMPORTED_REACTIVE_ENERGY_Q1),
                getScaled(block.getAcImportedReactiveEnergyQ1PhaseC(), block.getAcReactiveEnergySF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_IMPORTED_REACTIVE_ENERGY_Q2),
                getScaled(block.getAcImportedReactiveEnergyQ2PhaseC(), block.getAcReactiveEnergySF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_EXPORTED_REACTIVE_ENERGY_Q3),
                getScaled(block.getAcExportedReactiveEnergyQ3PhaseC(), block.getAcReactiveEnergySF()));

        updateState(new ChannelUID(getThing().getUID(), group, CHANNEL_AC_EXPORTED_REACTIVE_ENERGY_Q4),
                getScaled(block.getAcExportedReactiveEnergyQ4PhaseC(), block.getAcReactiveEnergySF()));
    }

}
