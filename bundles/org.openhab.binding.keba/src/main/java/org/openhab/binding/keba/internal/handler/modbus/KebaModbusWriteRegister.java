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

    SET_CHARGING_CURRENT(5004, CHANNEL_SET_CHARGING_CURRENT, Kind.CURRENT_MA, 63000),
    SET_ENERGY(5010, CHANNEL_SETENERGY, Kind.ENERGY_10WH, 65535),
    UNLOCK_PLUG(5012, CHANNEL_UNLOCK_PLUG, Kind.SWITCH_TRIGGER, 0),
    ENABLE_DISABLE(5014, CHANNEL_ENABLED_USER, Kind.SWITCH, 1),
    SET_PHASE_SWITCH_SOURCE(5050, CHANNEL_PHASE_SWITCH_SOURCE, Kind.NUMBER, 4),
    TRIGGER_PHASE_SWITCH(5052, CHANNEL_TRIGGER_PHASE_SWITCH, Kind.NUMBER, 1),
    SET_FAILSAFE_CURRENT(5016, CHANNEL_FAILSAFE_CURRENT_SETTING, Kind.CURRENT_MA, 63000),
    SET_FAILSAFE_TIMEOUT(5018, CHANNEL_FAILSAFE_TIMEOUT_SETTING, Kind.TIME_S, 65535);

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
    private final long maxRawValue;

    KebaModbusWriteRegister(int address, String channelId, Kind kind, long maxRawValue) {
        this.address = address;
        this.channelId = channelId;
        this.kind = kind;
        this.maxRawValue = maxRawValue;
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

    public long getMaxRawValue() {
        return maxRawValue;
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
