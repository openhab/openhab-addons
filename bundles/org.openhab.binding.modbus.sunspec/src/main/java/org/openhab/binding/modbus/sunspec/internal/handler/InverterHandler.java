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
package org.openhab.binding.modbus.sunspec.internal.handler;

import static org.openhab.binding.modbus.sunspec.internal.SunSpecConstants.*;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;
import static org.openhab.core.library.unit.Units.*;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.sunspec.internal.InverterStatus;
import org.openhab.binding.modbus.sunspec.internal.dto.InverterModelBlock;
import org.openhab.binding.modbus.sunspec.internal.parser.InverterModelParser;
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
 */
@NonNullByDefault
public class InverterHandler extends AbstractSunSpecHandler {

    /**
     * Parser used to convert incoming raw messages into model blocks
     */
    private final InverterModelParser parser = new InverterModelParser();

    /**
     * Logger instance
     */
    private final Logger logger = LoggerFactory.getLogger(InverterHandler.class);

    public InverterHandler(Thing thing) {
        super(thing);
    }

    /**
     * This method is called each time new data has been polled from the modbus slave
     * The register array is first parsed, then each of the channels are updated
     * to the new values
     *
     * @param registers byte array read from the modbus slave
     */
    @Override
    protected void handlePolledData(ModbusRegisterArray registers) {
        logger.trace("Model block received, size: {}", registers.size());

        InverterModelBlock block = parser.parse(registers);

        // Device information group
        updateState(channelUID(GROUP_DEVICE_INFO, CHANNEL_CABINET_TEMPERATURE),
                getScaled(block.temperatureCabinet, block.temperatureSF, CELSIUS));

        updateState(channelUID(GROUP_DEVICE_INFO, CHANNEL_HEATSINK_TEMPERATURE),
                getScaled(block.temperatureHeatsink, Optional.of(block.temperatureSF), CELSIUS));

        updateState(channelUID(GROUP_DEVICE_INFO, CHANNEL_TRANSFORMER_TEMPERATURE),
                getScaled(block.temperatureTransformer, Optional.of(block.temperatureSF), CELSIUS));

        updateState(channelUID(GROUP_DEVICE_INFO, CHANNEL_OTHER_TEMPERATURE),
                getScaled(block.temperatureOther, Optional.of(block.temperatureSF), CELSIUS));

        InverterStatus status = InverterStatus.getByCode(block.status);
        updateState(channelUID(GROUP_DEVICE_INFO, CHANNEL_STATUS),
                status == null ? UnDefType.UNDEF : new StringType(status.name()));

        // AC General group
        updateState(channelUID(GROUP_AC_GENERAL, CHANNEL_AC_TOTAL_CURRENT),
                getScaled(block.acCurrentTotal, block.acCurrentSF, AMPERE));

        updateState(channelUID(GROUP_AC_GENERAL, CHANNEL_AC_POWER), getScaled(block.acPower, block.acPowerSF, WATT));

        updateState(channelUID(GROUP_AC_GENERAL, CHANNEL_AC_FREQUENCY),
                getScaled(block.acFrequency, block.acFrequencySF, HERTZ));

        updateState(channelUID(GROUP_AC_GENERAL, CHANNEL_AC_APPARENT_POWER),
                getScaled(block.acApparentPower, block.acApparentPowerSF, WATT)); // TODO: VA currently not supported,
                                                                                  // see:
                                                                                  // https://github.com/openhab/openhab-core/pull/1347

        updateState(channelUID(GROUP_AC_GENERAL, CHANNEL_AC_REACTIVE_POWER),
                getScaled(block.acReactivePower, block.acReactivePowerSF, WATT)); // TODO: var currently not supported,
                                                                                  // see:
                                                                                  // https://github.com/openhab/openhab-core/pull/1347

        updateState(channelUID(GROUP_AC_GENERAL, CHANNEL_AC_POWER_FACTOR),
                getScaled(block.acPowerFactor, block.acPowerFactorSF, PERCENT));

        updateState(channelUID(GROUP_AC_GENERAL, CHANNEL_AC_LIFETIME_ENERGY),
                getScaled(block.acEnergyLifetime, block.acEnergyLifetimeSF, WATT_HOUR));

        // DC General group
        updateState(channelUID(GROUP_DC_GENERAL, CHANNEL_DC_CURRENT),
                getScaled(block.dcCurrent, block.dcCurrentSF, AMPERE));
        updateState(channelUID(GROUP_DC_GENERAL, CHANNEL_DC_VOLTAGE),
                getScaled(block.dcVoltage, block.dcVoltageSF, VOLT));
        updateState(channelUID(GROUP_DC_GENERAL, CHANNEL_DC_POWER), getScaled(block.dcPower, block.dcPowerSF, WATT));

        // AC Phase specific groups
        // All types of inverters
        updateState(channelUID(GROUP_AC_PHASE_A, CHANNEL_AC_PHASE_CURRENT),
                getScaled(block.acCurrentPhaseA, block.acCurrentSF, AMPERE));
        updateState(channelUID(GROUP_AC_PHASE_A, CHANNEL_AC_VOLTAGE_TO_NEXT),
                getScaled(block.acVoltageAB, block.acVoltageSF, VOLT));
        updateState(channelUID(GROUP_AC_PHASE_A, CHANNEL_AC_VOLTAGE_TO_N),
                getScaled(block.acVoltageAtoN, block.acVoltageSF, VOLT));

        // Split phase and three phase
        if ((thing.getThingTypeUID().equals(THING_TYPE_INVERTER_SPLIT_PHASE)
                || thing.getThingTypeUID().equals(THING_TYPE_INVERTER_THREE_PHASE))
                && block.phaseConfiguration >= INVERTER_SPLIT_PHASE) {
            updateState(channelUID(GROUP_AC_PHASE_B, CHANNEL_AC_PHASE_CURRENT),
                    getScaled(block.acCurrentPhaseB, block.acCurrentSF, AMPERE));
            updateState(channelUID(GROUP_AC_PHASE_B, CHANNEL_AC_VOLTAGE_TO_NEXT),
                    getScaled(block.acVoltageBC, block.acVoltageSF, VOLT));
            updateState(channelUID(GROUP_AC_PHASE_B, CHANNEL_AC_VOLTAGE_TO_N),
                    getScaled(block.acVoltageBtoN, block.acVoltageSF, VOLT));
        }

        // Three phase only
        if (thing.getThingTypeUID().equals(THING_TYPE_INVERTER_THREE_PHASE)
                && block.phaseConfiguration >= INVERTER_THREE_PHASE) {
            updateState(channelUID(GROUP_AC_PHASE_C, CHANNEL_AC_PHASE_CURRENT),
                    getScaled(block.acCurrentPhaseC, block.acCurrentSF, AMPERE));
            updateState(channelUID(GROUP_AC_PHASE_C, CHANNEL_AC_VOLTAGE_TO_NEXT),
                    getScaled(block.acVoltageCA, block.acVoltageSF, VOLT));
            updateState(channelUID(GROUP_AC_PHASE_C, CHANNEL_AC_VOLTAGE_TO_N),
                    getScaled(block.acVoltageCtoN, block.acVoltageSF, VOLT));
        }

        resetCommunicationError();
    }
}
