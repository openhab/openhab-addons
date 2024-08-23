/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.modbus.e3dc.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.e3dc.internal.modbus.Data;
import org.openhab.core.io.transport.modbus.ValueBuffer;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.util.HexUtils;

/**
 * The {@link InfoBlock} Data object for E3DC Info Block
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class InfoBlock implements Data {
    private static final StringType EMPTY = new StringType("NULL");
    public StringType modbusId = EMPTY;
    public StringType modbusVersion = EMPTY;
    public DecimalType supportedRegisters = new DecimalType(-1);
    public StringType manufacturer = EMPTY;
    public StringType modelName = EMPTY;
    public StringType serialNumber = EMPTY;
    public StringType firmware = EMPTY;

    /**
     * For decoding see Modbus Register Mapping Chapter 3.1.1 page 14
     *
     * @param bArray - Modbus Registers as bytes from 40001 to 40067
     */
    public InfoBlock(byte[] bArray) {
        // index handling to calculate the correct start index
        ValueBuffer wrapper = ValueBuffer.wrap(bArray);

        // first uint16 = 2 bytes - decode magic byte
        byte[] magicBytes = new byte[2];
        wrapper.get(magicBytes);
        this.modbusId = new StringType(HexUtils.bytesToHex(magicBytes));
        // first uint16 = 2 bytes - decode magic byte

        // unit8 (Modbus Major Version) + uint8 Modbus minor Version
        String modbusVersion = wrapper.getSInt8() + "." + wrapper.getSInt8();
        this.modbusVersion = new StringType(modbusVersion);

        // unit16 - supported registers
        short supportedRegisters = wrapper.getSInt16();
        this.supportedRegisters = new DecimalType(supportedRegisters);

        byte[] buffer = new byte[32];
        // 16 registers with uint16 = 32 bytes to decode a proper String
        wrapper.get(buffer);
        String manufacturer = DataConverter.getString(buffer);
        this.manufacturer = new StringType(manufacturer);

        // 16 registers with uint16 = 32 bytes to decode a proper String
        wrapper.get(buffer);
        String model = DataConverter.getString(buffer);
        this.modelName = new StringType(model);

        // 16 registers with uint16 = 32 bytes to decode a proper String
        wrapper.get(buffer);
        String serialNumber = DataConverter.getString(buffer);
        this.serialNumber = new StringType(serialNumber);

        // 16 registers with uint16 = 32 bytes to decode a proper String
        wrapper.get(buffer);
        String firmware = DataConverter.getString(buffer);
        this.firmware = new StringType(firmware);
    }
}
