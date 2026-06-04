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
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for the Anker SOLIX V1 Smart EV Charger profile.
 *
 * @author Thorben Grove - Initial contribution
 */
@NonNullByDefault
public class AnkerSolixWallboxHandler extends AbstractAnkerSolixHandler {

    private static final int MIN_TIMEOUT_SECONDS = 6;

    private static final List<PollRange> POLL_RANGES = Arrays.asList(
            new PollRange(ModbusReadFunctionCode.READ_INPUT_REGISTERS, 20000, 101),
            new PollRange(ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, 21000, 6));

    private static final Map<Integer, String> ENABLED_STATUS_MAP = Map.of(0, "disabled", 1, "enabled");
    private static final Map<Integer, String> SINGLE_THREE_PHASE_MODE_MAP = Map.of(1, "single_phase", 3, "three_phase");
    private static final Map<Integer, String> CHARGING_MODE_MAP = Map.of(0, "solar_plus_grid", 1, "only_solar");
    private static final Map<Integer, String> CP_SIGNAL_STATUS_MAP = Map.ofEntries(Map.entry(0, "a_12v"),
            Map.entry(3, "b1_9v"), Map.entry(4, "b2_9v"), Map.entry(5, "c1_6v"), Map.entry(6, "c2_6v"),
            Map.entry(7, "error"), Map.entry(8, "d1_3v"), Map.entry(9, "d2_3v"), Map.entry(10, "e_0v"),
            Map.entry(11, "f_minus_12v"));
    private static final Map<Integer, String> CHARGING_STATUS_MAP = Map.ofEntries(Map.entry(0, "idle"),
            Map.entry(1, "preparing"), Map.entry(2, "charging"), Map.entry(3, "charger_paused"),
            Map.entry(4, "vehicle_paused"), Map.entry(5, "charging_completed"), Map.entry(6, "reserving"),
            Map.entry(7, "disabled"), Map.entry(8, "error"));
    private static final Map<Integer, String> OCPP_CONNECTION_STATUS_MAP = Map.of(0, "not_connected", 1, "connecting",
            2, "connected");
    private static final Map<Integer, String> MQTT_CONNECTION_STATUS_MAP = Map.of(0, "not_connected", 1, "connected");
    private static final Map<Integer, String> CHARGING_COMMAND_MAP = Map.of(1, "start_charging", 2, "stop_charging");
    private static final Map<Integer, String> PHASE_SETTING_MAP = Map.of(0, "default", 1, "fixed_single_phase", 2,
            "fixed_three_phase");

    private final Logger logger = LoggerFactory.getLogger(AnkerSolixWallboxHandler.class);

    public AnkerSolixWallboxHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected List<PollRange> getPollRanges() {
        return POLL_RANGES;
    }

    @Override
    protected void handleDeviceCommand(String channelId, Command command) {
        if (CHANNEL_CHARGING_COMMAND.equals(channelId)) {
            Integer value = parseChargingCommand(command);
            if (value != null) {
                writeInt16Holding(21000, value);
                setShadowState(CHANNEL_CHARGING_COMMAND,
                        new StringType(CHARGING_COMMAND_MAP.getOrDefault(value, String.valueOf(value))));
            } else {
                logger.warn("Unsupported charging command: {}", command);
            }
            return;
        }

        if (CHANNEL_MAXIMUM_CURRENT_SETTING.equals(channelId)) {
            Integer currentInTenthAmpere = parseCurrentInTenthAmpere(command);
            if (currentInTenthAmpere != null && currentInTenthAmpere >= 0 && currentInTenthAmpere <= 320) {
                writeInt16Holding(21001, currentInTenthAmpere);
                setShadowState(CHANNEL_MAXIMUM_CURRENT_SETTING, new QuantityType<>(
                        BigDecimal.valueOf(currentInTenthAmpere).divide(BigDecimal.TEN), Units.AMPERE));
            } else {
                logger.warn("Unsupported maximum current command: {}", command);
            }
            return;
        }

        if (CHANNEL_BOOST_MODE_COMMAND.equals(channelId)) {
            Integer value = parseEnabledDisabled(command);
            if (value != null) {
                writeInt16Holding(21002, value);
                setShadowState(CHANNEL_BOOST_MODE_COMMAND, value == 1 ? OnOffType.ON : OnOffType.OFF);
            } else {
                logger.warn("Unsupported boost mode command: {}", command);
            }
            return;
        }

        if (CHANNEL_SET_TIMEOUT.equals(channelId)) {
            Integer timeoutInSeconds = parseInteger(command);
            if (timeoutInSeconds != null && timeoutInSeconds >= MIN_TIMEOUT_SECONDS) {
                writeInt16Holding(21003, timeoutInSeconds);
                setShadowState(CHANNEL_SET_TIMEOUT,
                        new QuantityType<>(BigDecimal.valueOf(timeoutInSeconds), Units.SECOND));
            } else {
                logger.warn("Unsupported timeout command: {}", command);
            }
            return;
        }

        if (CHANNEL_SET_NUMBER_OF_CHARGING_PHASES.equals(channelId)) {
            Integer value = parsePhaseSetting(command);
            if (value != null) {
                writeInt16Holding(21005, value);
                setShadowState(CHANNEL_SET_NUMBER_OF_CHARGING_PHASES,
                        new StringType(PHASE_SETTING_MAP.getOrDefault(value, String.valueOf(value))));
            } else {
                logger.warn("Unsupported charging phase command: {}", command);
            }
        }
    }

    @Override
    protected void applyStateFromCache() {
        String modelName = readString(20001, 10);
        String serialNumber = readString(20011, 12);
        updateStringChannel(CHANNEL_DEVICE_MODEL, resolveModelName(modelName, serialNumber));
        updateStringChannel(CHANNEL_DEVICE_SERIAL_NUMBER, serialNumber);
        updateStringChannel(CHANNEL_DEVICE_SW_VERSION, readString(20023, 6));
        updateStringChannel(CHANNEL_DEVICE_HW_VERSION, readString(20029, 6));

        Integer productNumber = readUInt16(20000);
        if (productNumber != null) {
            updateChannelState(CHANNEL_PRODUCT_NUMBER, new DecimalType(productNumber));
        }

        updatePowerChannel(CHANNEL_RATED_POWER, readInt32(20035));
        updateIntCurrentChannel(CHANNEL_MINIMUM_OUTPUT_CURRENT, readInt32(20037));
        updateIntCurrentChannel(CHANNEL_MAXIMUM_OUTPUT_CURRENT, readInt32(20039));

        updateAlarmWord(CHANNEL_ALARM_INFORMATION_1, 20041);
        updateAlarmWord(CHANNEL_ALARM_INFORMATION_2, 20042);
        updateAlarmWord(CHANNEL_ALARM_INFORMATION_3, 20043);
        updateAlarmWord(CHANNEL_ALARM_INFORMATION_4, 20044);
        updateAlarmWord(CHANNEL_ALARM_INFORMATION_5, 20045);
        updateAlarmWord(CHANNEL_ALARM_INFORMATION_6, 20046);
        updateAlarmWord(CHANNEL_ALARM_INFORMATION_7, 20047);
        updateAlarmWord(CHANNEL_ALARM_INFORMATION_8, 20048);
        updateAlarmWord(CHANNEL_ALARM_INFORMATION_9, 20049);
        updateAlarmWord(CHANNEL_ALARM_INFORMATION_10, 20050);
        updateAlarmWord(CHANNEL_ALARM_INFORMATION_11, 20051);
        updateAlarmWord(CHANNEL_ALARM_INFORMATION_12, 20052);

        updateVoltageChannel(CHANNEL_L1_N_VOLTAGE, readScaledUInt16(20053, 10));
        updateVoltageChannel(CHANNEL_L2_N_VOLTAGE, readScaledUInt16(20054, 10));
        updateVoltageChannel(CHANNEL_L3_N_VOLTAGE, readScaledUInt16(20055, 10));
        updateVoltageChannel(CHANNEL_L1_L2_VOLTAGE, readScaledUInt16(20056, 10));
        updateVoltageChannel(CHANNEL_L2_L3_VOLTAGE, readScaledUInt16(20057, 10));
        updateVoltageChannel(CHANNEL_L3_L1_VOLTAGE, readScaledUInt16(20058, 10));

        updateCurrentChannel(CHANNEL_L1_CURRENT, readScaledUInt16(20059, 100));
        updateCurrentChannel(CHANNEL_L2_CURRENT, readScaledUInt16(20060, 100));
        updateCurrentChannel(CHANNEL_L3_CURRENT, readScaledUInt16(20061, 100));

        updateUInt32PowerChannel(CHANNEL_L1_ACTIVE_POWER, 20062);
        updateUInt32PowerChannel(CHANNEL_L2_ACTIVE_POWER, 20064);
        updateUInt32PowerChannel(CHANNEL_L3_ACTIVE_POWER, 20066);
        updateUInt32PowerChannel(CHANNEL_TOTAL_CHARGING_ACTIVE_POWER, 20068);
        updateUInt32PowerChannel(CHANNEL_L1_REACTIVE_POWER, 20070);
        updateUInt32PowerChannel(CHANNEL_L2_REACTIVE_POWER, 20072);
        updateUInt32PowerChannel(CHANNEL_L3_REACTIVE_POWER, 20074);
        updateUInt32PowerChannel(CHANNEL_L1_APPARENT_POWER, 20076);
        updateUInt32PowerChannel(CHANNEL_L2_APPARENT_POWER, 20078);
        updateUInt32PowerChannel(CHANNEL_L3_APPARENT_POWER, 20080);

        Long currentSessionDuration = readUInt32(20082);
        if (currentSessionDuration != null) {
            updateChannelState(CHANNEL_CURRENT_CHARGING_SESSION_DURATION,
                    new QuantityType<>(BigDecimal.valueOf(currentSessionDuration), Units.SECOND));
        }

        Long currentChargingCapacity = readUInt32(20084);
        if (currentChargingCapacity != null) {
            updateChannelState(CHANNEL_CURRENT_CHARGING_CAPACITY,
                    new QuantityType<>(BigDecimal.valueOf(currentChargingCapacity), Units.WATT_HOUR));
        }

        updateEnumChannel(CHANNEL_PWM_ENABLED_STATUS, ENABLED_STATUS_MAP, readUInt16(20086));
        updateEnumChannel(CHANNEL_SINGLE_THREE_PHASE_OPERATING_MODE, SINGLE_THREE_PHASE_MODE_MAP, readUInt16(20087));
        updateEnumChannel(CHANNEL_CHARGING_MODE, CHARGING_MODE_MAP, readUInt16(20088));
        updateEnumChannel(CHANNEL_LOAD_BALANCING_ENABLED_STATUS, ENABLED_STATUS_MAP, readUInt16(20089));
        updateEnumChannel(CHANNEL_SOLAR_POWER_BALANCING_ENABLED_STATUS, ENABLED_STATUS_MAP, readUInt16(20090));

        Integer cpAcquisitionVoltage = readUInt16(20091);
        if (cpAcquisitionVoltage != null) {
            updateChannelState(CHANNEL_CP_ACQUISITION_VOLTAGE, new DecimalType(cpAcquisitionVoltage));
        }

        updateEnumChannel(CHANNEL_CP_SIGNAL_STATUS, CP_SIGNAL_STATUS_MAP, readUInt16(20092));

        updateTemperatureChannel(CHANNEL_RELAY_1_TEMPERATURE, readInt16(20093));
        updateTemperatureChannel(CHANNEL_RELAY_2_TEMPERATURE, readInt16(20094));

        updateEnumChannel(CHANNEL_BOOST_MODE, ENABLED_STATUS_MAP, readUInt16(20095));

        Integer ledBrightness = readUInt16(20096);
        if (ledBrightness != null) {
            updateChannelState(CHANNEL_LED_LIGHT_BRIGHTNESS,
                    new QuantityType<>(BigDecimal.valueOf(ledBrightness), Units.PERCENT));
        }

        updateEnumChannel(CHANNEL_CHARGING_STATUS, CHARGING_STATUS_MAP, readUInt16(20097));
        updateEnumChannel(CHANNEL_OCPP_CONNECTION_STATUS, OCPP_CONNECTION_STATUS_MAP, readUInt16(20099));
        updateEnumChannel(CHANNEL_MQTT_CONNECTION_STATUS, MQTT_CONNECTION_STATUS_MAP, readUInt16(20100));

        State chargingCommandShadow = getShadowState(CHANNEL_CHARGING_COMMAND);
        if (chargingCommandShadow != null) {
            updateChannelState(CHANNEL_CHARGING_COMMAND, chargingCommandShadow);
        } else {
            updateEnumChannel(CHANNEL_CHARGING_COMMAND, CHARGING_COMMAND_MAP, readUInt16(21000));
        }

        State currentSettingShadow = getShadowState(CHANNEL_MAXIMUM_CURRENT_SETTING);
        if (currentSettingShadow != null) {
            updateChannelState(CHANNEL_MAXIMUM_CURRENT_SETTING, currentSettingShadow);
        } else {
            updateCurrentChannel(CHANNEL_MAXIMUM_CURRENT_SETTING, readScaledUInt16(21001, 10));
        }

        State boostShadow = getShadowState(CHANNEL_BOOST_MODE_COMMAND);
        if (boostShadow != null) {
            updateChannelState(CHANNEL_BOOST_MODE_COMMAND, boostShadow);
        } else {
            Integer boostValue = readUInt16(21002);
            if (boostValue != null) {
                updateChannelState(CHANNEL_BOOST_MODE_COMMAND, boostValue == 1 ? OnOffType.ON : OnOffType.OFF);
            }
        }

        State timeoutShadow = getShadowState(CHANNEL_SET_TIMEOUT);
        if (timeoutShadow != null) {
            updateChannelState(CHANNEL_SET_TIMEOUT, timeoutShadow);
        } else {
            Integer timeoutValue = readUInt16(21003);
            if (timeoutValue != null) {
                updateChannelState(CHANNEL_SET_TIMEOUT,
                        new QuantityType<>(BigDecimal.valueOf(timeoutValue), Units.SECOND));
            }
        }

        State phaseShadow = getShadowState(CHANNEL_SET_NUMBER_OF_CHARGING_PHASES);
        if (phaseShadow != null) {
            updateChannelState(CHANNEL_SET_NUMBER_OF_CHARGING_PHASES, phaseShadow);
        } else {
            updateEnumChannel(CHANNEL_SET_NUMBER_OF_CHARGING_PHASES, PHASE_SETTING_MAP, readUInt16(21005));
        }
    }

    private void updateAlarmWord(String channelId, int registerAddress) {
        Integer value = readUInt16(registerAddress);
        if (value != null) {
            updateChannelState(channelId, new DecimalType(value));
        }
    }

    private void updateUInt32PowerChannel(String channelId, int registerAddress) {
        Long value = readUInt32(registerAddress);
        if (value != null) {
            updateChannelState(channelId, new QuantityType<>(BigDecimal.valueOf(value), Units.WATT));
        }
    }

    private void updateTemperatureChannel(String channelId, @Nullable Integer valueInCelsius) {
        if (valueInCelsius != null) {
            updateChannelState(channelId, new QuantityType<>(BigDecimal.valueOf(valueInCelsius), SIUnits.CELSIUS));
        }
    }

    private void updateIntCurrentChannel(String channelId, @Nullable Integer valueInAmpere) {
        if (valueInAmpere != null) {
            updateCurrentChannel(channelId, BigDecimal.valueOf(valueInAmpere));
        }
    }

    private void updateEnumChannel(String channelId, Map<Integer, String> mapping, @Nullable Integer rawValue) {
        if (rawValue == null) {
            return;
        }
        updateChannelState(channelId, new StringType(mapping.getOrDefault(rawValue, String.valueOf(rawValue))));
    }

    private @Nullable Integer parseInteger(Command command) {
        if (command instanceof QuantityType<?> quantityType) {
            return quantityType.toBigDecimal().intValue();
        }
        if (command instanceof DecimalType decimalType) {
            return decimalType.intValue();
        }
        return null;
    }

    private @Nullable Integer parseCurrentInTenthAmpere(Command command) {
        if (command instanceof QuantityType<?> quantityType) {
            QuantityType<?> current = quantityType.toUnit(Units.AMPERE);
            if (current != null) {
                return current.toBigDecimal().multiply(BigDecimal.TEN).intValue();
            }
            return quantityType.toBigDecimal().multiply(BigDecimal.TEN).intValue();
        }
        if (command instanceof DecimalType decimalType) {
            return decimalType.toBigDecimal().multiply(BigDecimal.TEN).intValue();
        }
        return null;
    }

    private @Nullable Integer parseChargingCommand(Command command) {
        if (command instanceof StringType stringType) {
            return switch (stringType.toString()) {
                case "start_charging" -> 1;
                case "stop_charging" -> 2;
                default -> null;
            };
        }
        if (command instanceof DecimalType decimalType) {
            int value = decimalType.intValue();
            return value == 1 || value == 2 ? value : null;
        }
        return null;
    }

    private @Nullable Integer parsePhaseSetting(Command command) {
        if (command instanceof StringType stringType) {
            return switch (stringType.toString()) {
                case "default" -> 0;
                case "fixed_single_phase" -> 1;
                case "fixed_three_phase" -> 2;
                default -> null;
            };
        }
        if (command instanceof DecimalType decimalType) {
            int value = decimalType.intValue();
            return value >= 0 && value <= 2 ? value : null;
        }
        return null;
    }

    private @Nullable Integer parseEnabledDisabled(Command command) {
        if (command instanceof OnOffType onOffType) {
            return onOffType == OnOffType.ON ? 1 : 0;
        }
        if (command instanceof StringType stringType) {
            return switch (stringType.toString()) {
                case "enabled" -> 1;
                case "disabled" -> 0;
                default -> null;
            };
        }
        if (command instanceof DecimalType decimalType) {
            int value = decimalType.intValue();
            return value == 0 || value == 1 ? value : null;
        }
        return null;
    }
}
