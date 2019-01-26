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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.modbus.sunspec.internal.block.InverterModelBlock;
import org.openhab.binding.modbus.sunspec.internal.parser.InverterModelParser;
import org.openhab.io.transport.modbus.ModbusManager;
import org.openhab.io.transport.modbus.ModbusRegisterArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link InverterHandler} is responsible for handling commands, which are
 * sent to an inverter.
 *
 * @author Nagy Attila Gabor - Initial contribution
 */
@NonNullByDefault
public class InverterHandler extends AbstractSunSpecHandler {

    /**
     * Parser used to convert incoming raw messages into model blocks
     */
    private InverterModelParser parser = new InverterModelParser();
    /**
     * Logger instance
     */
    private final Logger logger = LoggerFactory.getLogger(InverterHandler.class);

    public InverterHandler(Thing thing, Supplier<@NonNull ModbusManager> managerRef) {
        super(thing, managerRef);
    }

    /**
     *
     */
    @Override
    protected void handlePolledData(@NonNull ModbusRegisterArray registers) {
        logger.trace("Model block received, size: {}", registers.size());

        InverterModelBlock block = parser.parse(registers);

        // Device information group
        updateState(new ChannelUID(getThing().getUID(), GROUP_DEVICE_INFO, CHANNEL_CABINET_TEMPERATURE),
                getScaled(block.getTemperatureCabinet(), block.getTemperatureSF()));

        updateState(new ChannelUID(getThing().getUID(), GROUP_DEVICE_INFO, CHANNEL_HEATSINK_TEMPERATURE),
                getScaled(block.getTemperatureHeatsink(), block.getTemperatureSF()));

        updateState(new ChannelUID(getThing().getUID(), GROUP_DEVICE_INFO, CHANNEL_TRANSFORMER_TEMPERATURE),
                getScaled(block.getTemperatureTransformer(), block.getTemperatureSF()));

        updateState(new ChannelUID(getThing().getUID(), GROUP_DEVICE_INFO, CHANNEL_OTHER_TEMPERATURE),
                getScaled(block.getTemperatureOther(), block.getTemperatureSF()));

        Integer status = block.getStatus();
        updateState(new ChannelUID(getThing().getUID(), GROUP_DEVICE_INFO, CHANNEL_STATUS),
                status == null ? UnDefType.UNDEF : new DecimalType(status));

        // AC General group
        updateState(new ChannelUID(getThing().getUID(), GROUP_AC_GENERAL, CHANNEL_AC_TOTAL_CURRENT),
                getScaled(block.getAcCurrentTotal(), block.getAcCurrentSF()));

        updateState(new ChannelUID(getThing().getUID(), GROUP_AC_GENERAL, CHANNEL_AC_POWER),
                getScaled(block.getAcPower(), block.getAcPowerSF()));

        updateState(new ChannelUID(getThing().getUID(), GROUP_AC_GENERAL, CHANNEL_AC_FREQUENCY),
                getScaled(block.getAcFrequency(), block.getAcFrequencySF()));

        updateState(new ChannelUID(getThing().getUID(), GROUP_AC_GENERAL, CHANNEL_AC_APPARENT_POWER),
                getScaled(block.getAcApparentPower(), block.getAcApparentPowerSF()));

        updateState(new ChannelUID(getThing().getUID(), GROUP_AC_GENERAL, CHANNEL_AC_REACTIVE_POWER),
                getScaled(block.getAcReactivePower(), block.getAcReactivePowerSF()));

        updateState(new ChannelUID(getThing().getUID(), GROUP_AC_GENERAL, CHANNEL_AC_POWER_FACTOR),
                getScaled(block.getAcPowerFactor(), block.getAcPowerFactorSF()));

        updateState(new ChannelUID(getThing().getUID(), GROUP_AC_GENERAL, CHANNEL_AC_LIFETIME_ENERGY),
                getScaled(block.getAcEnergyLifetime(), block.getAcEnergyLifetimeSF()));

        // DC General group
        updateState(new ChannelUID(getThing().getUID(), GROUP_DC_GENERAL, CHANNEL_DC_CURRENT),
                getScaled(block.getDcCurrent(), block.getDcCurrentSF()));
        updateState(new ChannelUID(getThing().getUID(), GROUP_DC_GENERAL, CHANNEL_DC_VOLTAGE),
                getScaled(block.getDcVoltage(), block.getDcVoltageSF()));
        updateState(new ChannelUID(getThing().getUID(), GROUP_DC_GENERAL, CHANNEL_DC_POWER),
                getScaled(block.getDcPower(), block.getDcPowerSF()));

        // AC Phase specific groups
        // All types of inverters
        updateState(new ChannelUID(getThing().getUID(), GROUP_AC_PHASE_A, CHANNEL_AC_PHASE_CURRENT),
                getScaled(block.getAcCurrentPhaseA(), block.getAcCurrentSF()));
        updateState(new ChannelUID(getThing().getUID(), GROUP_AC_PHASE_A, CHANNEL_AC_VOLTAGE_TO_NEXT),
                getScaled(block.getAcVoltageAB(), block.getAcVoltageSF()));
        updateState(new ChannelUID(getThing().getUID(), GROUP_AC_PHASE_A, CHANNEL_AC_VOLTAGE_TO_N),
                getScaled(block.getAcVoltageAtoN(), block.getAcVoltageSF()));

        // Split phase and three phase
        if ((thing.getThingTypeUID().equals(THING_TYPE_INVERTER_SPLIT_PHASE)
                || thing.getThingTypeUID().equals(THING_TYPE_INVERTER_THREE_PHASE))
                && block.getPhaseConfiguration() >= INVERTER_SPLIT_PHASE) {
            updateState(new ChannelUID(getThing().getUID(), GROUP_AC_PHASE_B, CHANNEL_AC_PHASE_CURRENT),
                    getScaled(block.getAcCurrentPhaseB(), block.getAcCurrentSF()));
            updateState(new ChannelUID(getThing().getUID(), GROUP_AC_PHASE_B, CHANNEL_AC_VOLTAGE_TO_NEXT),
                    getScaled(block.getAcVoltageBC(), block.getAcVoltageSF()));
            updateState(new ChannelUID(getThing().getUID(), GROUP_AC_PHASE_B, CHANNEL_AC_VOLTAGE_TO_N),
                    getScaled(block.getAcVoltageBtoN(), block.getAcVoltageSF()));
        }

        // Three phase only
        if (thing.getThingTypeUID().equals(THING_TYPE_INVERTER_THREE_PHASE)
                && block.getPhaseConfiguration() >= INVERTER_THREE_PHASE) {
            updateState(new ChannelUID(getThing().getUID(), GROUP_AC_PHASE_C, CHANNEL_AC_PHASE_CURRENT),
                    getScaled(block.getAcCurrentPhaseC(), block.getAcCurrentSF()));
            updateState(new ChannelUID(getThing().getUID(), GROUP_AC_PHASE_C, CHANNEL_AC_VOLTAGE_TO_NEXT),
                    getScaled(block.getAcVoltageCA(), block.getAcVoltageSF()));
            updateState(new ChannelUID(getThing().getUID(), GROUP_AC_PHASE_C, CHANNEL_AC_VOLTAGE_TO_N),
                    getScaled(block.getAcVoltageCtoN(), block.getAcVoltageSF()));
        }

        resetCommunicationError();
    }

}
