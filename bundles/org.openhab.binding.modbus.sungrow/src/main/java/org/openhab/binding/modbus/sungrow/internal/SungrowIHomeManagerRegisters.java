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
package org.openhab.binding.modbus.sungrow.internal;

import static org.openhab.core.io.transport.modbus.ModbusConstants.ValueType.*;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.function.Function;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.sungrow.internal.mapper.ToStringMapper;
import org.openhab.binding.modbus.sungrow.internal.mapper.impl.ChargerStatusMapper;
import org.openhab.binding.modbus.sungrow.internal.mapper.impl.OutputTypeMapper;
import org.openhab.core.io.transport.modbus.ModbusConstants.ValueType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;

/**
 * The {@link SungrowIHomeManagerRegisters} is responsible for defining Modbus registers and their units.
 *
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
public enum SungrowIHomeManagerRegisters {

    // The following register numbers are 1-based. They need to be converted before sending them on the wire.
    TOTAL_DEVICES_CONNECTED(8005, UINT16, ConversionConstants.ONE, decimalStateFactory(), "ihm-overview"),
    DEVICES_IN_FAULT(8006, UINT16, ConversionConstants.ONE, decimalStateFactory(), "ihm-overview"),

    TOTAL_NOMINAL_ACTIVE_POWER(8145, UINT32_SWAP, ConversionConstants.MULTI_BY_HUNDRED,
            quantityStateFactory(Units.WATT), "ihm-overview"),
    TOTAL_BATTERY_RATED_CAPACITY(8147, UINT32_SWAP, ConversionConstants.DIV_BY_TEN,
            quantityStateFactory(Units.KILOWATT_HOUR), "ihm-battery-information"),
    BATTERY_CHARGE_DISCHARGE_LIMIT(8149, UINT32_SWAP, ConversionConstants.MULTI_BY_HUNDRED,
            quantityStateFactory(Units.WATT), "ihm-battery-information"),
    BATTERY_MAX_CHARGE_POWER(8151, UINT16, ConversionConstants.MULTI_BY_HUNDRED, quantityStateFactory(Units.WATT),
            "ihm-battery-information"),
    BATTERY_MIN_CHARGE_POWER(8152, UINT16, ConversionConstants.MULTI_BY_HUNDRED, quantityStateFactory(Units.WATT),
            "ihm-battery-information"),
    BATTERY_MAX_DISCHARGE_POWER(8153, UINT16, ConversionConstants.MULTI_BY_HUNDRED, quantityStateFactory(Units.WATT),
            "ihm-battery-information"),
    BATTERY_MIN_DISCHARGE_POWER(8154, UINT16, ConversionConstants.MULTI_BY_HUNDRED, quantityStateFactory(Units.WATT),
            "ihm-battery-information"),
    TOTAL_ACTIVE_POWER(8155, INT32_SWAP, ConversionConstants.MULTI_BY_TEN, quantityStateFactory(Units.WATT),
            "ihm-overview"),
    METER_POWER(8157, INT32_SWAP, ConversionConstants.MULTI_BY_TEN, quantityStateFactory(Units.WATT),
            "ihm-meter-information"),
    LOAD_POWER(8159, INT32_SWAP, ConversionConstants.MULTI_BY_TEN, quantityStateFactory(Units.WATT),
            "ihm-load-information"),
    BATTERY_POWER(8161, INT32_SWAP, ConversionConstants.MULTI_BY_TEN, quantityStateFactory(Units.WATT),
            "ihm-battery-information"),
    BATTERY_LEVEL(8163, UINT16, ConversionConstants.DIV_BY_TEN, quantityStateFactory(Units.PERCENT),
            "ihm-battery-information"),
    GRID_IMPORT_ENERGY(8176, UINT32_SWAP, ConversionConstants.DIV_BY_TEN, quantityStateFactory(Units.KILOWATT_HOUR),
            "ihm-grid-information"),
    GRID_EXPORT_ENERGY(8178, UINT32_SWAP, ConversionConstants.DIV_BY_TEN, quantityStateFactory(Units.KILOWATT_HOUR),
            "ihm-grid-information"),

    CHARGER_STATUS(8552, UINT16, ConversionConstants.ONE, stringStateFactory(ChargerStatusMapper.instance()),
            "ihm-charger"),
    OUTPUT_TYPE(8554, UINT16, ConversionConstants.ONE,
            (BigDecimal value) -> new StringType(OutputTypeMapper.instance().map(value.intValue())), "ihm-charger"),
    PHASE_A_VOLTAGE(8555, UINT16, ConversionConstants.DIV_BY_TEN, quantityStateFactory(Units.VOLT),
            "ihm-meter-information"),
    PHASE_B_VOLTAGE(8556, UINT16, ConversionConstants.DIV_BY_TEN, quantityStateFactory(Units.VOLT),
            "ihm-meter-information"),
    PHASE_C_VOLTAGE(8557, UINT16, ConversionConstants.DIV_BY_TEN, quantityStateFactory(Units.VOLT),
            "ihm-meter-information"),
    GRID_FREQUENCY(8558, UINT16, ConversionConstants.DIV_BY_TEN, quantityStateFactory(Units.HERTZ),
            "ihm-meter-information"),
    PHASE_A_ACTIVE_POWER(8559, INT32_SWAP, ConversionConstants.ONE, quantityStateFactory(Units.WATT),
            "ihm-meter-information"),
    PHASE_B_ACTIVE_POWER(8561, INT32_SWAP, ConversionConstants.ONE, quantityStateFactory(Units.WATT),
            "ihm-meter-information"),
    PHASE_C_ACTIVE_POWER(8563, INT32_SWAP, ConversionConstants.ONE, quantityStateFactory(Units.WATT),
            "ihm-meter-information"),

    PHASE_A_VOLTAGE_CH2(8565, UINT16, ConversionConstants.DIV_BY_TEN, quantityStateFactory(Units.VOLT),
            "ihm-meter-information"),
    PHASE_B_VOLTAGE_CH2(8566, UINT16, ConversionConstants.DIV_BY_TEN, quantityStateFactory(Units.VOLT),
            "ihm-meter-information"),
    PHASE_C_VOLTAGE_CH2(8567, UINT16, ConversionConstants.DIV_BY_TEN, quantityStateFactory(Units.VOLT),
            "ihm-meter-information"),
    GRID_FREQUENCY_CH2(8568, UINT16, ConversionConstants.DIV_BY_TEN, quantityStateFactory(Units.HERTZ),
            "ihm-meter-information"),
    PHASE_A_ACTIVE_POWER_CH2(8569, INT32_SWAP, ConversionConstants.ONE, quantityStateFactory(Units.WATT),
            "ihm-meter-information"),
    PHASE_B_ACTIVE_POWER_CH2(8571, INT32_SWAP, ConversionConstants.ONE, quantityStateFactory(Units.WATT),
            "ihm-meter-information"),
    PHASE_C_ACTIVE_POWER_CH2(8573, INT32_SWAP, ConversionConstants.ONE, quantityStateFactory(Units.WATT),
            "ihm-meter-information");

    private final BigDecimal multiplier;
    private final int registerNumber;
    private final ValueType type;

    private final Function<BigDecimal, State> stateFactory;
    private final String channelGroup;

    SungrowIHomeManagerRegisters(int registerNumber, ValueType type, BigDecimal multiplier,
            Function<BigDecimal, State> stateFactory, String channelGroup) {
        this.multiplier = multiplier;
        this.registerNumber = registerNumber;
        this.type = type;
        this.stateFactory = stateFactory;
        this.channelGroup = channelGroup;
    }

    /**
     * Creates a Function that creates {@link QuantityType} states with the given {@link Unit}.
     *
     * @param unit {@link Unit} to be used for the value.
     * @return Function for value creation.
     */
    private static Function<BigDecimal, State> quantityStateFactory(Unit<?> unit) {
        return (BigDecimal value) -> new QuantityType<>(value, unit);
    }

    /**
     * Creates a Function that creates {@link DecimalType} states.
     *
     * @return Function for value creation.
     */
    private static Function<BigDecimal, State> decimalStateFactory() {
        return DecimalType::new;
    }

    /**
     * Creates a Function that creates {@link StringType} states with the help of the given {@link ToStringMapper}.
     *
     * @param mapper {@link ToStringMapper} to be used for the mapping of the {@link BigDecimal} to {@link String}.
     * @return Function for value creation.
     */
    private static Function<BigDecimal, State> stringStateFactory(ToStringMapper mapper) {
        return (BigDecimal value) -> new StringType(mapper.map(value));
    }

    /**
     * Returns the modbus register number.
     *
     * @return modbus register number.
     */
    public int getRegisterNumber() {
        return registerNumber;
    }

    /**
     * Returns the {@link ValueType} for the channel.
     *
     * @return {@link ValueType} for the channel.
     */
    public ValueType getType() {
        return type;
    }

    /**
     * Returns the count of registers read to return the value of this register.
     *
     * @return register count.
     */
    public int getRegisterCount() {
        return this.type.getBits() / 16;
    }

    /**
     * Returns the channel group.
     *
     * @return channel group id.
     */
    public String getChannelGroup() {
        return channelGroup;
    }

    /**
     * Returns the channel name.
     *
     * @return the channel name.
     */
    public String getChannelName() {
        return this.name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    /**
     * Creates the {@link State} for the given register value.
     *
     * @param registerValue the value for the channel.
     * @return {@link State] for the given value.
     */
    public State createState(DecimalType registerValue) {
        final BigDecimal scaledValue = registerValue.toBigDecimal().multiply(this.multiplier);
        return this.stateFactory.apply(scaledValue);
    }
}
