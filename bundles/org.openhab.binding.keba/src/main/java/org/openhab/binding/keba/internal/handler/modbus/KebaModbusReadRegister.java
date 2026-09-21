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
 * @author Karel Goderis - Initial contribution
 */
@NonNullByDefault
public enum KebaModbusReadRegister {

    STATE(1000, CHANNEL_STATE, Kind.NUMBER),
    CABLE_STATE(1004, CHANNEL_CABLE_STATE, Kind.NUMBER),
    ERROR_CODE(1006, CHANNEL_ERROR_CODE, Kind.NUMBER),
    CURRENT_L1(1008, CHANNEL_I1, Kind.CURRENT_MA),
    CURRENT_L2(1010, CHANNEL_I2, Kind.CURRENT_MA),
    CURRENT_L3(1012, CHANNEL_I3, Kind.CURRENT_MA),
    SERIAL(1014, CHANNEL_SERIAL, Kind.DECIMAL_STRING),
    ACTIVE_POWER(1020, CHANNEL_POWER, Kind.POWER_MW),
    TOTAL_ENERGY(1036, CHANNEL_TOTAL_CONSUMPTION, Kind.ENERGY_01WH),
    VOLTAGE_L1(1040, CHANNEL_U1, Kind.VOLTAGE_V),
    VOLTAGE_L2(1042, CHANNEL_U2, Kind.VOLTAGE_V),
    VOLTAGE_L3(1044, CHANNEL_U3, Kind.VOLTAGE_V),
    POWER_FACTOR(1046, CHANNEL_POWER_FACTOR, Kind.PERMILLE_PERCENT),
    MAX_CHARGING_CURRENT(1100, CHANNEL_MAX_CHARGING_CURRENT, Kind.CURRENT_MA),
    MAX_SUPPORTED_CURRENT(1110, CHANNEL_MAX_SUPPORTED_CURRENT, Kind.CURRENT_MA),
    RFID_TAG(1500, CHANNEL_SESSION_RFID_TAG, Kind.HEX_STRING),
    SESSION_ENERGY(1502, CHANNEL_SESSION_CONSUMPTION, Kind.ENERGY_01WH),
    PHASE_SWITCH_SOURCE(1550, CHANNEL_PHASE_SWITCH_SOURCE, Kind.NUMBER),
    PHASE_SWITCH_STATE(1552, CHANNEL_PHASE_SWITCH_STATE, Kind.NUMBER),
    FAILSAFE_CURRENT_SETTING(1600, CHANNEL_FAILSAFE_CURRENT_SETTING, Kind.CURRENT_MA),
    FAILSAFE_TIMEOUT_SETTING(1602, CHANNEL_FAILSAFE_TIMEOUT_SETTING, Kind.TIME_S);

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

    KebaModbusReadRegister(int address, String channelId, Kind kind) {
        this.address = address;
        this.channelId = channelId;
        this.kind = kind;
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
}
