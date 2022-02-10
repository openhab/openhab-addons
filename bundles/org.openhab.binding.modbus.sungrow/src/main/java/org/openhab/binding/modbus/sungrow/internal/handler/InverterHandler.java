/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.modbus.sungrow.internal.handler;

import static org.openhab.binding.modbus.sungrow.internal.SungrowConstants.*;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;
import static org.openhab.core.library.unit.Units.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.sungrow.internal.SungrowDeviceType;
import org.openhab.binding.modbus.sungrow.internal.SungrowOutputType;
import org.openhab.binding.modbus.sungrow.internal.SungrowSystemState;
import org.openhab.binding.modbus.sungrow.internal.dto.InverterModelBlock13k;
import org.openhab.binding.modbus.sungrow.internal.dto.InverterModelBlock5k;
import org.openhab.binding.modbus.sungrow.internal.parser.InverterModel13kParser;
import org.openhab.binding.modbus.sungrow.internal.parser.InverterModel5kParser;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link InverterHandler} is responsible for handling commands, which are
 * sent to an inverter and publishing the received values to OpenHAB.
 *
 * @author Nagy Attila Gabor - Initial contribution
 * @author Ferdinand Schwenk - reused for sungrow bundle
 */
@NonNullByDefault
public class InverterHandler extends AbstractSungrowHandler {

    /**
     * Parser used to convert incoming raw messages into model blocks
     */
    private final InverterModel5kParser parser5k = new InverterModel5kParser();
    private final InverterModel13kParser parser13k = new InverterModel13kParser();

    /**
     * Logger instance
     */
    private final Logger logger = LoggerFactory.getLogger(InverterHandler.class);

    public InverterHandler(Thing thing) {
        super(thing);
    }

    /**
     * Scale Factors
     */
    Short scaleBy1 = 0;
    Short scaleBy10 = -1;
    Short scaleBy100 = -2;
    Short scaleTimes10 = +1;
    Short scaleTimes100 = +2;

    /**
     * This method is called each time new data has been polled from the modbus slave
     * The register array is first parsed, then each of the channels are updated
     * to the new values
     *
     * @param registers byte array read from the modbus slave
     */
    @Override
    protected void handlePolled5kData(ModbusRegisterArray registers) {
        logger.trace("Model block received, size: {}", registers.size());

        InverterModelBlock5k block = parser5k.parse(registers);
        logger.trace("InverterModelBlock5k:\n{}", block.toString());

        // Device information group
        SungrowDeviceType deviceType = SungrowDeviceType.getByCode(block.deviceType);
        updateState(channelUID(GROUP_DEVICE_INFO, CHANNEL_DEVICE_TYPE),
                deviceType == null ? UnDefType.UNDEF : new StringType(deviceType.name()));

        updateState(channelUID(GROUP_DEVICE_INFO, CHANNEL_INSIDE_TEMPERATURE),
                getScaled(block.insideTemperature, scaleBy10, CELSIUS));

        updateState(channelUID(GROUP_DEVICE_INFO, CHANNEL_NOMINAL_OUTPUT_POWER),
                getScaled(block.nominalOutputPower, scaleTimes100, WATT));

        SungrowOutputType outputType = SungrowOutputType.getByCode(block.outputType);
        updateState(channelUID(GROUP_DEVICE_INFO, CHANNEL_OUTPUT_TYPE),
                outputType == null ? UnDefType.UNDEF : new StringType(outputType.name()));

        // AC General group
        updateState(channelUID(GROUP_AC_GENERAL, CHANNEL_AC_FREQUENCY), getScaled(block.acFrequency, scaleBy10, HERTZ));

        updateState(channelUID(GROUP_AC_GENERAL, CHANNEL_AC_REACTIVE_POWER),
                getScaled(block.acReactivePower, scaleBy1, VAR));

        updateState(channelUID(GROUP_AC_GENERAL, CHANNEL_AC_POWER_FACTOR),
                getScaled(block.acPowerFactor, scaleBy10, PERCENT));

        // Power Info group
        updateState(channelUID(GROUP_POWER_INFO, CHANNEL_DC_TOTAL_POWER),
                getScaled(block.totalDCPower, scaleBy1, WATT));

        // MPPT groups
        updateState(channelUID(GROUP_MPPT1, CHANNEL_DC_CURRENT), getScaled(block.mppt1Current, scaleBy10, AMPERE));
        updateState(channelUID(GROUP_MPPT1, CHANNEL_DC_VOLTAGE), getScaled(block.mppt1Voltage, scaleBy10, VOLT));
        updateState(channelUID(GROUP_MPPT1, CHANNEL_DC_POWER), getScaled(block.mppt1Power, scaleBy100, WATT));
        updateState(channelUID(GROUP_MPPT2, CHANNEL_DC_CURRENT), getScaled(block.mppt2Current, scaleBy10, AMPERE));
        updateState(channelUID(GROUP_MPPT2, CHANNEL_DC_VOLTAGE), getScaled(block.mppt2Voltage, scaleBy10, VOLT));
        updateState(channelUID(GROUP_MPPT2, CHANNEL_DC_POWER), getScaled(block.mppt2Power, scaleBy100, WATT));

        // AC Phase specific groups
        // All types of inverters
        updateState(channelUID(GROUP_AC_PHASE_A, CHANNEL_AC_VOLTAGE_TO_NEXT),
                getScaled(block.acVoltageAB, scaleBy10, VOLT));
        updateState(channelUID(GROUP_AC_PHASE_A, CHANNEL_AC_VOLTAGE_TO_N),
                getScaled(block.acVoltageAtoN, scaleBy10, VOLT));
        updateState(channelUID(GROUP_AC_PHASE_B, CHANNEL_AC_VOLTAGE_TO_NEXT),
                getScaled(block.acVoltageBC, scaleBy10, VOLT));
        updateState(channelUID(GROUP_AC_PHASE_B, CHANNEL_AC_VOLTAGE_TO_N),
                getScaled(block.acVoltageBtoN, scaleBy10, VOLT));
        updateState(channelUID(GROUP_AC_PHASE_C, CHANNEL_AC_VOLTAGE_TO_NEXT),
                getScaled(block.acVoltageCA, scaleBy10, VOLT));
        updateState(channelUID(GROUP_AC_PHASE_C, CHANNEL_AC_VOLTAGE_TO_N),
                getScaled(block.acVoltageCtoN, scaleBy10, VOLT));

        resetCommunicationError();
    }

    /**
     * This method is called each time new data has been polled from the modbus slave
     * The register array is first parsed, then each of the channels are updated
     * to the new values
     *
     * @param registers byte array read from the modbus slave
     */
    @Override
    protected void handlePolled13kData(ModbusRegisterArray registers) {
        logger.trace("Model block received, size: {}", registers.size());

        InverterModelBlock13k block = parser13k.parse(registers);
        logger.trace("InverterModelBlock13k:\n{}", block.toString());

        // Device information group
        SungrowSystemState systemState = SungrowSystemState.getByCode(block.systemState);
        updateState(channelUID(GROUP_DEVICE_INFO, CHANNEL_SYSTEM_STATE),
                systemState == null ? UnDefType.UNDEF : new StringType(systemState.name()));

        updateState(channelUID(GROUP_DEVICE_INFO, CHANNEL_RUNNING_STATE),
                new StringType(String.format("0x%02X", block.runningState)));

        // AC General group
        StringType gridState = new StringType(String.format("0x%02X", block.gridState));
        updateState(channelUID(GROUP_AC_GENERAL, CHANNEL_GRID_STATE), gridState);

        updateState(channelUID(GROUP_AC_GENERAL, CHANNEL_AC_POWER), getScaled(block.totalActivePower, scaleBy1, WATT));

        // AC Phase specific groups
        // All types of inverters
        updateState(channelUID(GROUP_AC_PHASE_A, CHANNEL_AC_PHASE_CURRENT),
                getScaled(block.acCurrentPhaseA, scaleBy10, AMPERE));
        updateState(channelUID(GROUP_AC_PHASE_B, CHANNEL_AC_PHASE_CURRENT),
                getScaled(block.acCurrentPhaseB, scaleBy10, AMPERE));
        updateState(channelUID(GROUP_AC_PHASE_C, CHANNEL_AC_PHASE_CURRENT),
                getScaled(block.acCurrentPhaseC, scaleBy10, AMPERE));

        // Power Info group
        updateState(channelUID(GROUP_POWER_INFO, CHANNEL_LOAD_POWER), getScaled(block.loadPower, scaleBy1, WATT));
        updateState(channelUID(GROUP_POWER_INFO, CHANNEL_GRID_POWER),
                getScaled(block.exportPower * -1, scaleBy1, WATT));

        // Battery group
        updateState(channelUID(GROUP_BATTERY, CHANNEL_BATTERY_VOLTAGE),
                getScaled(block.batteryVoltage, scaleBy10, VOLT));
        updateState(channelUID(GROUP_BATTERY, CHANNEL_BATTERY_CURRENT),
                getScaled(block.batteryCurrent, scaleBy10, AMPERE));
        Integer batteryPowerSign = ((block.runningState & RS_BATTERY_CHARGING) > 0) ? -1 : +1;
        updateState(channelUID(GROUP_BATTERY, CHANNEL_BATTERY_POWER),
                getScaled(batteryPowerSign * block.batteryPower, scaleBy1, WATT));
        updateState(channelUID(GROUP_BATTERY, CHANNEL_BATTERY_LEVEL),
                getScaled(block.batteryLevel, scaleBy10, PERCENT));
        updateState(channelUID(GROUP_BATTERY, CHANNEL_BATTERY_HEALTH),
                getScaled(block.batteryHealth, scaleBy10, PERCENT));
        updateState(channelUID(GROUP_BATTERY, CHANNEL_BATTERY_TEMPERATURE),
                getScaled(block.batteryTemperature, scaleBy10, CELSIUS));

        resetCommunicationError();
    }
}
