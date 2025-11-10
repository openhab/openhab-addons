/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.modbus.foxinverter.internal;

import static org.openhab.core.io.transport.modbus.ModbusConstants.ValueType.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.transport.modbus.ModbusConstants.ValueType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;

/**
 * The {@link SolakonOneInverterRegisters} is responsible for defining Modbus registers and their units.
 *
 * @author Holger Friedrich - Initial contribution
 */
@NonNullByDefault
public enum SolakonOneInverterRegisters {
    METER_CONNECTED(38801, UINT16, BigDecimal.ONE, switchFactory(), "overview"),

    HIDDEN_STATUS1(39063, UINT16, BigDecimal.ONE, DecimalType::new, ""),
    HIDDEN_STATUS3(39065, UINT32, BigDecimal.ONE, DecimalType::new, ""),
    HIDDEN_ALARM1(39067, UINT16, BigDecimal.ONE, DecimalType::new, ""),
    HIDDEN_ALARM2(39068, UINT16, BigDecimal.ONE, DecimalType::new, ""),
    HIDDEN_ALARM3(39069, UINT16, BigDecimal.ONE, DecimalType::new, ""),

    // NOTE: this list needs to be sorted by register number!
    MPPT1_VOLTAGE(39070, UINT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.VOLT), "mppt-information"),
    MPPT1_CURRENT(39071, UINT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.AMPERE), "mppt-information"),
    MPPT2_VOLTAGE(39072, UINT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.VOLT), "mppt-information"),
    MPPT2_CURRENT(39073, UINT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.AMPERE), "mppt-information"),
    MPPT3_VOLTAGE(39074, UINT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.VOLT), "mppt-information"),
    MPPT3_CURRENT(39075, UINT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.AMPERE), "mppt-information"),
    MPPT4_VOLTAGE(39076, UINT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.VOLT), "mppt-information"),
    MPPT4_CURRENT(39077, UINT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.AMPERE), "mppt-information"),

    TOTAL_PV_POWER(39118, INT32_SWAP, ConversionConstants.DIV_BY_THOUSAND, quantityFactory(Units.WATT),
            "mppt-information"),

    GRID_FREQUENCY(39139, UINT16, new BigDecimal(BigInteger.ONE, 2), quantityFactory(Units.HERTZ), "overview"),

    PHASE_A_VOLTAGE(39123, INT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.VOLT), "grid-information"),
    PHASE_B_VOLTAGE(39124, INT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.VOLT), "grid-information"),
    PHASE_C_VOLTAGE(39125, INT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.VOLT), "grid-information"),

    ACTIVE_POWER(39134, INT32, ConversionConstants.DIV_BY_THOUSAND, quantityFactory(Units.WATT), "overview"),
    REACTIVE_POWER(39136, INT32_SWAP, ConversionConstants.DIV_BY_THOUSAND, quantityFactory(Units.VAR), "overview"),
    POWER_FACTOR(39138, INT16, ConversionConstants.DIV_BY_THOUSAND, DecimalType::new, "overview"),
    INTERNAL_TEMPERATURE(39141, INT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.KELVIN),
            ConversionConstants.CELSIUS_TO_KELVIN, "overview"),

    TOTAL_PV_GENERATION(39149, UINT32, ConversionConstants.DIV_BY_HUNDRED, quantityFactory(Units.KILOWATT_HOUR),
            "overview"),
    DAILY_PV_GENERATION(39151, UINT32, ConversionConstants.DIV_BY_HUNDRED, quantityFactory(Units.KILOWATT_HOUR),
            "overview"),

    CHARGING_POWER(39162, INT32, BigDecimal.ONE, quantityFactory(Units.WATT), "battery-information"),
    EXPORT_POWER(39168, INT32, BigDecimal.ONE, quantityFactory(Units.WATT), "grid-information"),

    BATTERY_VOLTAGE(39227, UINT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.VOLT), "battery-information"),
    BATTERY_CURRENT(39228, INT32_SWAP, ConversionConstants.DIV_BY_THOUSAND, quantityFactory(Units.AMPERE),
            "battery-information"),
    BATTERY_POWER(39237, INT32_SWAP, BigDecimal.ONE, quantityFactory(Units.WATT), "battery-information"), // prefer
                                                                                                          // combined
                                                                                                          // power over
                                                                                                          // bat1 power

    MPPT1_POWER(39279, INT32_SWAP, BigDecimal.ONE, quantityFactory(Units.WATT), "mppt-information"),
    MPPT2_POWER(39281, INT32_SWAP, BigDecimal.ONE, quantityFactory(Units.WATT), "mppt-information"),
    MPPT3_POWER(39283, INT32_SWAP, BigDecimal.ONE, quantityFactory(Units.WATT), "mppt-information"),
    MPPT4_POWER(39285, INT32_SWAP, BigDecimal.ONE, quantityFactory(Units.WATT), "mppt-information"),

    BATTERY_LEVEL(39424, UINT16, BigDecimal.ONE, DecimalType::new, "battery-information"),

    EPS_OUTPUT(46613, UINT16, BigDecimal.ONE, DecimalType::new, "emergency-power-supply"); // 0:off 2:on

    /*
     * Thing data protocolVersion "Protokoll Version" [ readStart="39000", readValueType="uint32" ] // always 0
     * Thing data inverterL1Current "Wechselrichter L1 Strom" [ readStart="39126", readValueType="int32_swap",
     * readTransform="JS(divide1000.js)" ]
     * Thing data inverterL2Current "Wechselrichter L2 Strom" [ readStart="39128", readValueType="int32_swap",
     * readTransform="JS(divide1000.js)" ]
     * Thing data inverterL3Current "Wechselrichter L3 Strom" [ readStart="39130", readValueType="int32_swap",
     * readTransform="JS(divide1000.js)" ]
     * 
     * Thing data remoteControlStatus "Fernsteuerung Status" [ readStart="46001", readValueType="uint16",
     * writeStart="46001", writeValueType="uint16", writeType="holding" ]
     * Thing data remoteTimeoutSet "Fernsteuerung Timeout" [ readStart="46002", readValueType="uint16",
     * writeStart="46002", writeValueType="uint16", writeType="holding" ]
     * Thing data remoteActivePower "Fernsteuerung Wirkleistung" [ readStart="46003", readValueType="int32_swap",
     * writeStart="46003", writeValueType="int32_swap", writeType="holding" ]
     * Thing data remoteReactivePower "Fernsteuerung Blindleistung" [ readStart="46005", readValueType="int32_swap",
     * writeStart="46005", writeValueType="int32_swap", writeType="holding" ]
     * Thing data remoteTimeoutCountdown "Fernsteuerung Countdown" [ readStart="46007", readValueType="uint16" ]
     * 
     * Thing data minimumSoc "Minimaler SOC" [ readStart="46609", readValueType="uint16", writeStart="46609",
     * writeValueType="uint16", writeType="holding" ]
     * Thing data maximumSoc "Maximaler SOC" [ readStart="46610", readValueType="uint16", writeStart="46610",
     * writeValueType="uint16", writeType="holding" ]
     * Thing data minimumSocOnGrid "Minimaler SOC OnGrid" [ readStart="46611", readValueType="uint16",
     * writeStart="46611", writeValueType="uint16", writeType="holding" ]
     * Thing data epsOutput "EPS Ausgabe" [ readStart="46613", readValueType="uint16", writeStart="46613",
     * writeValueType="uint16", writeType="holding" ]
     */
    private final BigDecimal multiplier;
    private final int registerNumber;
    private final ValueType type;

    private final Function<BigDecimal, BigDecimal> conversion;
    private final Function<BigDecimal, State> stateFactory;
    private final String channelGroup;

    SolakonOneInverterRegisters(int registerNumber, ValueType type, BigDecimal multiplier,
            Function<BigDecimal, State> stateFactory, Function<BigDecimal, BigDecimal> conversion,
            String channelGroup) {
        this.multiplier = multiplier;
        this.registerNumber = registerNumber;
        this.type = type;
        this.conversion = conversion;
        this.stateFactory = stateFactory;
        this.channelGroup = channelGroup;
    }

    SolakonOneInverterRegisters(int registerNumber, ValueType type, BigDecimal multiplier,
            Function<BigDecimal, State> stateFactory, String channelGroup) {
        this.multiplier = multiplier;
        this.registerNumber = registerNumber;
        this.type = type;
        this.conversion = Function.identity();
        this.stateFactory = stateFactory;
        this.channelGroup = channelGroup;
    }

    private static Function<BigDecimal, State> switchFactory() {
        return (BigDecimal value) -> OnOffType.from(value.toString());
    }

    /**
     * Creates a Function that creates {@link QuantityType} states with the given {@link Unit}.
     *
     * @param unit {@link Unit} to be used for the value.
     * @return Function for value creation.
     */
    private static Function<BigDecimal, State> quantityFactory(Unit<?> unit) {
        return (BigDecimal value) -> new QuantityType<>(value, unit);
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
        return this.name().toLowerCase().replace('_', '-');
    }

    /**
     * Creates the {@link State} for the given register value.
     *
     * @param registerValue the value for the channel.
     * @return {@link State} for the given value.
     */
    public State createState(DecimalType registerValue) {
        final BigDecimal scaledValue = registerValue.toBigDecimal().multiply(this.multiplier);

        final BigDecimal convertedValue = conversion.apply(scaledValue);
        return this.stateFactory.apply(convertedValue);
    }
}
