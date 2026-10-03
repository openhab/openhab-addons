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
package org.openhab.binding.keba.internal.handler.modbus;

import static org.openhab.binding.keba.internal.KebaBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link KebaModbusReadRegister} enumerates the holding registers of the KEBA KeContact P30/P40 Modbus TCP
 * interface that are read cyclically by this binding, together with the channel they are mapped to and how their
 * raw {@code UINT32} register value has to be interpreted.
 *
 * The KEBA Modbus TCP interface only allows reading a single register (2 words / {@code UINT32}) per request, so
 * every entry below is polled with its own request.
 *
 * @author MikeTheTux - Initial contribution
 */
@NonNullByDefault
public enum KebaModbusReadRegister {

    STATE(1000, CHANNEL_STATE, Kind.NUMBER, true),
    CABLE_STATE(1004, CHANNEL_CABLE_STATE, Kind.NUMBER, true),
    ERROR_CODE(1006, CHANNEL_ERROR_CODE, Kind.NUMBER, true),
    CURRENT_L1(1008, CHANNEL_I1, Kind.CURRENT_MA, true),
    CURRENT_L2(1010, CHANNEL_I2, Kind.CURRENT_MA, true),
    CURRENT_L3(1012, CHANNEL_I3, Kind.CURRENT_MA, true),
    SERIAL(1014, CHANNEL_SERIAL, Kind.DECIMAL_STRING, false),
    PRODUCT_INFO(1016, PROPERTY_PRODUCT_TYPE, Kind.NUMBER, false),
    SOFTWARE_VERSION(1018, PROPERTY_FIRMWARE, Kind.HEX_STRING, false, true),
    ACTIVE_POWER(1020, CHANNEL_POWER, Kind.POWER_MW, true),
    TOTAL_ENERGY(1036, CHANNEL_TOTAL_CONSUMPTION, Kind.ENERGY_01WH, false),
    VOLTAGE_L1(1040, CHANNEL_U1, Kind.VOLTAGE_V, true),
    VOLTAGE_L2(1042, CHANNEL_U2, Kind.VOLTAGE_V, true),
    VOLTAGE_L3(1044, CHANNEL_U3, Kind.VOLTAGE_V, true),
    POWER_FACTOR(1046, CHANNEL_POWER_FACTOR, Kind.PERMILLE_PERCENT, true),
    MAX_CHARGING_CURRENT(1100, CHANNEL_MAX_CHARGING_CURRENT, Kind.CURRENT_MA, true),
    MAX_SUPPORTED_CURRENT(1110, CHANNEL_MAX_SUPPORTED_CURRENT, Kind.CURRENT_MA, false),
    FAST_CHARGING_STATUS(1200, CHANNEL_FAST_CHARGING_STATUS, Kind.NUMBER, false, true, true),
    RFID_TAG(1500, CHANNEL_SESSION_RFID_TAG, Kind.HEX_STRING, false),
    SESSION_ENERGY(1502, CHANNEL_SESSION_CONSUMPTION, Kind.ENERGY_01WH, false),
    PHASE_SWITCH_SOURCE(1550, CHANNEL_PHASE_SWITCH_SOURCE, Kind.NUMBER, false),
    PHASE_SWITCH_STATE(1552, CHANNEL_PHASE_SWITCH_STATE, Kind.NUMBER, false),
    FAILSAFE_CURRENT_SETTING(1600, CHANNEL_FAILSAFE_CURRENT_SETTING, Kind.CURRENT_MA, false),
    FAILSAFE_TIMEOUT_SETTING(1602, CHANNEL_FAILSAFE_TIMEOUT_SETTING, Kind.TIME_S, false),
    HARDWARE_REVISION_DEVICE(1700, "hardwareRevisionDevice", Kind.NUMBER, false, true, true),
    HARDWARE_REVISION_KC_MS10(1702, "hardwareRevisionKcMs10", Kind.NUMBER, false, true, true);

    /**
     * How the raw {@code UINT32} register value has to be converted into a channel state.
     */
    public enum Kind {
        /** Plain, unitless number. */
        NUMBER,
        /** Milliamperes, converted to {@code Number:ElectricCurrent} in Ampere. */
        CURRENT_MA,
        /** Volts, converted to {@code Number:ElectricPotential}. */
        VOLTAGE_V,
        /** Milliwatts, converted to {@code Number:Power} in Watt. */
        POWER_MW,
        /** Tenths of a Watt-hour, converted to {@code Number:Energy} in Watt-hours. */
        ENERGY_01WH,
        /** Tenths of a percent, converted to {@code Number:Dimensionless} in percent. */
        PERMILLE_PERCENT,
        /** Seconds, converted to {@code Number:Time}. */
        TIME_S,
        /** Decimal value, converted to its decimal string representation. */
        DECIMAL_STRING,
        /** Decimal value, converted to its upper case hexadecimal string representation. */
        HEX_STRING
    }

    private final int address;
    private final String channelId;
    private final Kind kind;
    private final boolean fast;
    private final boolean optional;
    private final boolean p40Only;

    KebaModbusReadRegister(int address, String channelId, Kind kind, boolean fast) {
        this(address, channelId, kind, fast, false, false);
    }

    KebaModbusReadRegister(int address, String channelId, Kind kind, boolean fast, boolean optional) {
        this(address, channelId, kind, fast, optional, false);
    }

    KebaModbusReadRegister(int address, String channelId, Kind kind, boolean fast, boolean optional, boolean p40Only) {
        this.address = address;
        this.channelId = channelId;
        this.kind = kind;
        this.fast = fast;
        this.optional = optional;
        this.p40Only = p40Only;
    }

    public int getAddress() {
        return address;
    }

    public String getChannelId() {
        return channelId;
    }

    public Kind getKind() {
        return kind;
    }

    public boolean isFast() {
        return fast;
    }

    public boolean isOptional() {
        return optional;
    }

    public boolean isP40Only() {
        return p40Only;
    }
}
