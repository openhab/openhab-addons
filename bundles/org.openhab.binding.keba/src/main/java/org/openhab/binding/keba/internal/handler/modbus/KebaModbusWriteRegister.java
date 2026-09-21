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
 * The {@link KebaModbusWriteRegister} enumerates the writable ({@code UINT16}) holding registers of the KEBA
 * KeContact P30/P40 Modbus TCP interface that are exposed as commandable channels by this binding.
 *
 * @author Karel Goderis - Initial contribution
 */
@NonNullByDefault
public enum KebaModbusWriteRegister {

    SET_CHARGING_CURRENT(5004, CHANNEL_SET_CHARGING_CURRENT, Kind.CURRENT_MA),
    SET_ENERGY(5010, CHANNEL_SETENERGY, Kind.ENERGY_10WH),
    UNLOCK_PLUG(5012, CHANNEL_UNLOCK_PLUG, Kind.SWITCH_TRIGGER),
    ENABLE_DISABLE(5014, CHANNEL_ENABLED_USER, Kind.SWITCH),
    SET_PHASE_SWITCH_SOURCE(5050, CHANNEL_SET_PHASE_SWITCH_SOURCE, Kind.NUMBER),
    TRIGGER_PHASE_SWITCH(5052, CHANNEL_TRIGGER_PHASE_SWITCH, Kind.NUMBER),
    SET_FAILSAFE_CURRENT(5016, CHANNEL_SET_FAILSAFE_CURRENT, Kind.CURRENT_MA),
    SET_FAILSAFE_TIMEOUT(5018, CHANNEL_SET_FAILSAFE_TIMEOUT, Kind.TIME_S);

    /**
     * How a {@link org.openhab.core.types.Command} sent to the channel has to be converted into a raw
     * {@code UINT16} register value.
     */
    public enum Kind {
        /** Plain, unitless number (0-63000 range style values). */
        NUMBER,
        /** {@code Number:ElectricCurrent} command, converted to milliamperes. */
        CURRENT_MA,
        /** {@code Number:Energy} command, converted to units of 10 Wh. */
        ENERGY_10WH,
        /** {@code Number:Time} command, converted to seconds. */
        TIME_S,
        /** {@code Switch} command, ON writes 1 and OFF writes 0. */
        SWITCH,
        /** {@code Switch} command, ON writes the fixed trigger value 0; OFF is ignored. */
        SWITCH_TRIGGER
    }

    private final int address;
    private final String channelId;
    private final Kind kind;

    KebaModbusWriteRegister(int address, String channelId, Kind kind) {
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

    public static KebaModbusWriteRegister fromChannelId(String channelId) {
        for (KebaModbusWriteRegister register : values()) {
            if (register.channelId.equals(channelId)) {
                return register;
            }
        }
        throw new IllegalArgumentException("No writable Modbus register for channel '" + channelId + "'");
    }
}
