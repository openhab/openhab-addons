/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.e3dc.internal.dto;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.openhab.binding.e3dc.internal.modbus.Data;

/**
 * The {@link InfoBlock} Data object for E3DC Info Block
 *
 * @author Bernd Weymann - Initial contribution
 */
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
        int byteIndex = 0;

        // first uint16 = 2 bytes - decode magic byte
        StringBuilder magicByte = new StringBuilder();
        magicByte.append(String.format("%02X", bArray[byteIndex]));
        magicByte.append(String.format("%02X", bArray[byteIndex + 1]));
        this.modbusId = new StringType(magicByte.toString());
        // first uint16 = 2 bytes - decode magic byte
        byteIndex += 2;

        // unit8 (Modbus Major Version) + uint8 Modbus minor Version
        String modbusVersion = bArray[byteIndex] + "." + bArray[byteIndex + 1];
        this.modbusVersion = new StringType(modbusVersion);
        byteIndex += 2;

        // unit16 - supported registers
        int supportedRegisters = DataConverter.getIntValue(bArray, byteIndex);
        this.supportedRegisters = new DecimalType(supportedRegisters);
        byteIndex += 2;

        // 16 registers with uint16 = 32 bytes to decode a proper String
        String manufacturer = DataConverter.getString(bArray, byteIndex);
        this.manufacturer = new StringType(manufacturer);
        byteIndex += 32;

        // 16 registers with uint16 = 32 bytes to decode a proper String
        String model = DataConverter.getString(bArray, byteIndex);
        this.modelName = new StringType(model);
        byteIndex += 32;

        // 16 registers with uint16 = 32 bytes to decode a proper String
        String serialNumber = DataConverter.getString(bArray, byteIndex);
        this.serialNumber = new StringType(serialNumber);
        byteIndex += 32;

        // 16 registers with uint16 = 32 bytes to decode a proper String
        String firmware = DataConverter.getString(bArray, byteIndex);
        this.firmware = new StringType(firmware);
        byteIndex += 32;
    }
}
