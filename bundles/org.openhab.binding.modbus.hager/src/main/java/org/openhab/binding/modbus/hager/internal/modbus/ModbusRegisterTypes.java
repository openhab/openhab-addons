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
package org.openhab.binding.modbus.hager.internal.modbus;

import java.util.List;
import java.util.Map;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ModbusRegisterTypes} bundles the value types shared by all Hager Flow register maps.
 * The declarations mirror the columns of the Hager Flow XEM470 Modbus table (FR2_2024_03).
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public final class ModbusRegisterTypes {

    private ModbusRegisterTypes() {
        // constants-only holder, not instantiable
    }

    /**
     * Modbus wire data type of a register, as declared in the source document.
     */
    public enum DataType {
        U16,
        I16,
        U32,
        I32,
        F32,
        F64,
        STRING,
        BOOL,
        BITSTRING,
        BITFIELD
    }

    /**
     * Read/write access of a register, as declared in the source document.
     */
    public enum Access {
        READ_ONLY,
        READ_WRITE
    }

    /**
     * One independently meaningful boolean bit within a register.
     *
     * @param bit zero based bit position within the register
     * @param channelId the channel id this bit is published as
     * @param whenSet meaning of the bit when it is set, as given in the source document
     * @param whenReset meaning of the bit when it is not set, as given in the source document
     */
    public record BitFlag(int bit, String channelId, String whenSet, String whenReset) {
    }

    /**
     * One register of the Hager Flow Modbus table.
     *
     * @param channelId the channel id the register value is published as; unused for registers which only publish
     *            single bits or are not decoded at all
     * @param groupId the channel group the register belongs to
     * @param address zero based Modbus register address
     * @param length length in 16 bit words
     * @param dataType Modbus wire data type
     * @param access read/write access
     * @param unit physical unit of the value, {@code null} for values without a unit
     * @param enumValues maps the raw value of a register to the label of the selected meaning, empty if the register
     *            is not a whole value enumeration
     * @param bitFlags independently meaningful bits of the register, empty if it has none
     */
    public record Register(String channelId, String groupId, int address, int length, DataType dataType, Access access,
            @Nullable Unit<?> unit, Map<Integer, String> enumValues, List<BitFlag> bitFlags) {
    }
}
