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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for the Anker SOLIX Solarbank device profile.
 *
 * @author Thorben Grove - Initial contribution
 */
@NonNullByDefault
public class AnkerSolixSolarbankHandler extends AbstractAnkerSolixHandler {

    private static final List<PollRange> POLL_RANGES = Arrays.asList(
            new PollRange(ModbusReadFunctionCode.READ_INPUT_REGISTERS, 10000, 51),
            new PollRange(ModbusReadFunctionCode.READ_INPUT_REGISTERS, 10090, 67),
            new PollRange(ModbusReadFunctionCode.READ_INPUT_REGISTERS, 10208, 58),
            new PollRange(ModbusReadFunctionCode.READ_INPUT_REGISTERS, 32768, 7),
            new PollRange(ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, 10060, 13),
            new PollRange(ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, 10074, 8));

    private static final Map<String, Integer> OPERATING_MODE_VALUES = Map.ofEntries(Map.entry("self_consumption", 0),
            Map.entry("tou_mode", 1), Map.entry("third_party_control", 3), Map.entry("custom_mode", 4),
            Map.entry("socket_overlay_mode", 5), Map.entry("smart_mode", 6), Map.entry("dynamic_pricing", 7));

    private static final Map<Integer, String> OPERATING_MODE_NAMES = Map.ofEntries(Map.entry(0, "self_consumption"),
            Map.entry(1, "tou_mode"), Map.entry(3, "third_party_control"), Map.entry(4, "custom_mode"),
            Map.entry(5, "socket_overlay_mode"), Map.entry(6, "smart_mode"), Map.entry(7, "dynamic_pricing"));

    private static final int OPERATING_MODE_REGISTER = 10064;
    private static final int THIRD_PARTY_CONTROL_MODE = 3;

    private final Logger logger = LoggerFactory.getLogger(AnkerSolixSolarbankHandler.class);

    private volatile String directionSelection = "discharge";
    private volatile boolean autoModeApplied = false;

    public AnkerSolixSolarbankHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected List<PollRange> getPollRanges() {
        return POLL_RANGES;
    }

    @Override
    protected void handleDeviceCommand(String channelId, Command command) {
        if (CHANNEL_OPERATING_MODE.equals(channelId)) {
            if (command instanceof StringType stringType) {
                Integer modeValue = OPERATING_MODE_VALUES.get(stringType.toString());
                if (modeValue != null) {
                    writeInt16Holding(OPERATING_MODE_REGISTER, modeValue);
                    setShadowState(CHANNEL_OPERATING_MODE, stringType);
                } else {
                    logger.warn("Unsupported operating mode command: {}", command);
                }
            }
            return;
        }

        if (CHANNEL_BATTERY_POWER_DIRECTION.equals(channelId)) {
            if (command instanceof StringType stringType) {
                String value = stringType.toString();
                if ("charge".equals(value) || "discharge".equals(value)) {
                    directionSelection = value;
                    updateChannelState(CHANNEL_BATTERY_POWER_DIRECTION, new StringType(value));
                } else {
                    logger.warn("Unsupported battery direction command: {}", command);
                }
            }
            return;
        }

        if (CHANNEL_BATTERY_POWER_SETPOINT.equals(channelId)) {
            Integer setpointValue = parseSetpointCommand(command);
            if (setpointValue == null) {
                logger.warn("Unsupported setpoint command type: {}", command.getClass().getSimpleName());
                return;
            }

            int signedValue = toSignedSetpoint(setpointValue);
            writeInt32Holding(10071, signedValue);
            setShadowState(CHANNEL_BATTERY_POWER_SETPOINT,
                    new QuantityType<>(BigDecimal.valueOf(Math.abs(setpointValue)), Units.WATT));
        }
    }

    @Override
    protected void onConnected() {
        AnkerSolixConfiguration localConfig = config;
        if (localConfig == null || !localConfig.autoThirdPartyControl || autoModeApplied) {
            return;
        }

        autoModeApplied = true;
        Integer currentMode = readUInt16(OPERATING_MODE_REGISTER);
        if (currentMode != null && currentMode == THIRD_PARTY_CONTROL_MODE) {
            return;
        }

        logger.debug("Auto-enabling third party control mode on connect");
        writeInt16Holding(OPERATING_MODE_REGISTER, THIRD_PARTY_CONTROL_MODE);
        setShadowState(CHANNEL_OPERATING_MODE, new StringType("third_party_control"));
    }

    @Override
    protected void applyStateFromCache() {
        String serialNumber = readString(10100, 12);
        String rawModel = readString(32768, 5);
        updateStringChannel(CHANNEL_DEVICE_MODEL, resolveModelName(rawModel, serialNumber));
        updateStringChannel(CHANNEL_DEVICE_SERIAL_NUMBER, serialNumber);
        updateStringChannel(CHANNEL_DEVICE_SW_VERSION, readString(10112, 6));

        Integer batterySoc = readUInt16(10014);
        if (batterySoc != null) {
            updateChannelState(CHANNEL_BATTERY_SOC, new QuantityType<>(BigDecimal.valueOf(batterySoc), Units.PERCENT));
        }

        Integer pcsPvPower = readInt32(10002);
        Integer thirdPartyPvPower = readInt32(10004);
        if (pcsPvPower != null && thirdPartyPvPower != null) {
            updateChannelState(CHANNEL_PV_POWER,
                    new QuantityType<>(BigDecimal.valueOf(pcsPvPower + thirdPartyPvPower), Units.WATT));
        }

        Integer batteryPower = readInt32(10008);
        if (batteryPower != null) {
            int chargingPower = batteryPower < 0 ? Math.abs(batteryPower) : 0;
            int dischargingPower = batteryPower > 0 ? batteryPower : 0;
            updateChannelState(CHANNEL_BATTERY_CHARGING_POWER,
                    new QuantityType<>(BigDecimal.valueOf(chargingPower), Units.WATT));
            updateChannelState(CHANNEL_BATTERY_DISCHARGING_POWER,
                    new QuantityType<>(BigDecimal.valueOf(dischargingPower), Units.WATT));
        }

        Integer loadPower = readInt32(10010);
        if (loadPower != null) {
            updateChannelState(CHANNEL_LOAD_POWER, new QuantityType<>(BigDecimal.valueOf(loadPower), Units.WATT));
        }

        Integer gridPower = readInt32(10012);
        if (gridPower != null) {
            int importPower = gridPower > 0 ? gridPower : 0;
            int exportPower = gridPower < 0 ? Math.abs(gridPower) : 0;
            updateChannelState(CHANNEL_GRID_IMPORT_POWER,
                    new QuantityType<>(BigDecimal.valueOf(importPower), Units.WATT));
            updateChannelState(CHANNEL_GRID_EXPORT_POWER,
                    new QuantityType<>(BigDecimal.valueOf(exportPower), Units.WATT));
        }

        Integer acGridOutputPower = readInt32(10208);
        if (acGridOutputPower != null) {
            updateChannelState(CHANNEL_AC_GRID_OUTPUT_POWER,
                    new QuantityType<>(BigDecimal.valueOf(acGridOutputPower), Units.WATT));
        }

        Long pvGenerationRaw = readUInt32(10018);
        if (pvGenerationRaw != null) {
            updateChannelState(CHANNEL_PV_TOTAL_GENERATION, new QuantityType<>(
                    BigDecimal.valueOf(pvGenerationRaw).divide(BigDecimal.TEN), Units.KILOWATT_HOUR));
        }

        Long cumulativeChargeRaw = readUInt32(10262);
        if (cumulativeChargeRaw != null) {
            updateChannelState(CHANNEL_CUMULATIVE_CHARGE_ENERGY, new QuantityType<>(
                    BigDecimal.valueOf(cumulativeChargeRaw).divide(BigDecimal.TEN), Units.KILOWATT_HOUR));
        }

        Long cumulativeDischargeRaw = readUInt32(10264);
        if (cumulativeDischargeRaw != null) {
            updateChannelState(CHANNEL_CUMULATIVE_DISCHARGE_ENERGY, new QuantityType<>(
                    BigDecimal.valueOf(cumulativeDischargeRaw).divide(BigDecimal.TEN), Units.KILOWATT_HOUR));
        }

        State modeShadow = getShadowState(CHANNEL_OPERATING_MODE);
        if (modeShadow != null) {
            updateChannelState(CHANNEL_OPERATING_MODE, modeShadow);
        } else {
            Integer modeValue = readUInt16(OPERATING_MODE_REGISTER);
            if (modeValue != null) {
                String mode = OPERATING_MODE_NAMES.getOrDefault(modeValue, "unknown");
                updateChannelState(CHANNEL_OPERATING_MODE, new StringType(mode));
            }
        }

        State setpointShadow = getShadowState(CHANNEL_BATTERY_POWER_SETPOINT);
        if (setpointShadow != null) {
            updateChannelState(CHANNEL_BATTERY_POWER_SETPOINT, setpointShadow);
        } else {
            Integer setpointRaw = readInt32(10071);
            if (setpointRaw != null) {
                int absoluteSetpoint = Math.abs(setpointRaw);
                directionSelection = setpointRaw < 0 ? "charge" : "discharge";
                updateChannelState(CHANNEL_BATTERY_POWER_SETPOINT,
                        new QuantityType<>(BigDecimal.valueOf(absoluteSetpoint), Units.WATT));
            }
        }

        updateChannelState(CHANNEL_BATTERY_POWER_DIRECTION, new StringType(directionSelection));
    }

    private int toSignedSetpoint(int setpointValue) {
        return "charge".equals(directionSelection) ? -Math.abs(setpointValue) : Math.abs(setpointValue);
    }

    private @Nullable Integer parseSetpointCommand(Command command) {
        if (command instanceof QuantityType<?> quantityType) {
            QuantityType<?> power = quantityType.toUnit(Units.WATT);
            if (power != null) {
                return power.toBigDecimal().intValue();
            }
            return quantityType.toBigDecimal().intValue();
        }
        if (command instanceof DecimalType decimalType) {
            return decimalType.intValue();
        }
        return null;
    }
}
