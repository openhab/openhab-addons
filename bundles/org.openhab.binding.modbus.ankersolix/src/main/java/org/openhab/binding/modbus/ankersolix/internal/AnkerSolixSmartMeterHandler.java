/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.modbus.ankersolix.internal;

import static org.openhab.binding.modbus.ankersolix.internal.AnkerSolixBindingConstants.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * Handler for the Anker SOLIX Smart Meter Gen 2 device profile.
 *
 * @author Thorben Grove - Initial contribution
 */
@NonNullByDefault
public class AnkerSolixSmartMeterHandler extends AbstractAnkerSolixHandler {

    private static final List<PollRange> POLL_RANGES = Arrays.asList(
            new PollRange(ModbusReadFunctionCode.READ_INPUT_REGISTERS, 10620, 27),
            new PollRange(ModbusReadFunctionCode.READ_INPUT_REGISTERS, 10666, 12),
            new PollRange(ModbusReadFunctionCode.READ_INPUT_REGISTERS, 10696, 17));

    private static final Map<Integer, String> METER_TYPE_NAMES = Map.of(1, "single_phase", 2, "three_phase");

    public AnkerSolixSmartMeterHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected List<PollRange> getPollRanges() {
        return POLL_RANGES;
    }

    @Override
    protected void handleDeviceCommand(String channelId, Command command) {
        // Smart Meter Gen 2 exposes read-only channels.
    }

    @Override
    protected void applyStateFromCache() {
        String serialNumber = readString(10702, 10);
        String rawModel = readString(10620, 10);
        updateStringChannel(CHANNEL_DEVICE_MODEL, resolveModelName(rawModel, serialNumber));
        updateStringChannel(CHANNEL_DEVICE_SERIAL_NUMBER, serialNumber);
        updateStringChannel(CHANNEL_DEVICE_SW_VERSION, readVersion(10696));

        Integer meterTypeRaw = readUInt16(10630);
        if (meterTypeRaw != null) {
            updateChannelState(CHANNEL_METER_TYPE,
                    new StringType(METER_TYPE_NAMES.getOrDefault(meterTypeRaw, String.valueOf(meterTypeRaw))));
        }

        updatePowerChannel(CHANNEL_PRIMARY_TOTAL_ACTIVE_POWER, readInt32(10644));
        updatePowerChannel(CHANNEL_PRIMARY_PHASE_1_ACTIVE_POWER, readInt32(10638));
        updatePowerChannel(CHANNEL_PRIMARY_PHASE_2_ACTIVE_POWER, readInt32(10640));
        updatePowerChannel(CHANNEL_PRIMARY_PHASE_3_ACTIVE_POWER, readInt32(10642));

        updateCurrentChannel(CHANNEL_PRIMARY_PHASE_1_CURRENT, readScaledInt16(10635, 100));
        updateCurrentChannel(CHANNEL_PRIMARY_PHASE_2_CURRENT, readScaledInt16(10636, 100));
        updateCurrentChannel(CHANNEL_PRIMARY_PHASE_3_CURRENT, readScaledInt16(10637, 100));

        updateVoltageChannel(CHANNEL_PRIMARY_PHASE_1_VOLTAGE, readScaledUInt16(10632, 10));
        updateVoltageChannel(CHANNEL_PRIMARY_PHASE_2_VOLTAGE, readScaledUInt16(10633, 10));
        updateVoltageChannel(CHANNEL_PRIMARY_PHASE_3_VOLTAGE, readScaledUInt16(10634, 10));

        updatePowerChannel(CHANNEL_SECONDARY_TOTAL_ACTIVE_POWER, readInt32(10675));
        updatePowerChannel(CHANNEL_SECONDARY_PHASE_1_ACTIVE_POWER, readInt32(10669));
        updatePowerChannel(CHANNEL_SECONDARY_PHASE_2_ACTIVE_POWER, readInt32(10671));
        updatePowerChannel(CHANNEL_SECONDARY_PHASE_3_ACTIVE_POWER, readInt32(10673));

        updateCurrentChannel(CHANNEL_SECONDARY_PHASE_1_CURRENT, readScaledInt16(10666, 100));
        updateCurrentChannel(CHANNEL_SECONDARY_PHASE_2_CURRENT, readScaledInt16(10667, 100));
        updateCurrentChannel(CHANNEL_SECONDARY_PHASE_3_CURRENT, readScaledInt16(10668, 100));

        // The secondary CT group shares the same mains, so it reuses the primary phase
        // voltage registers (10632-10634) on purpose. The meter exposes only one voltage
        // measurement per phase; this mirrors the upstream Home Assistant register map and
        // is not a defect.
        updateVoltageChannel(CHANNEL_SECONDARY_PHASE_1_VOLTAGE, readScaledUInt16(10632, 10));
        updateVoltageChannel(CHANNEL_SECONDARY_PHASE_2_VOLTAGE, readScaledUInt16(10633, 10));
        updateVoltageChannel(CHANNEL_SECONDARY_PHASE_3_VOLTAGE, readScaledUInt16(10634, 10));
    }
}
