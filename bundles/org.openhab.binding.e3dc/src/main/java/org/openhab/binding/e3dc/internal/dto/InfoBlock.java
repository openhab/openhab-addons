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
    public StringType allInfo = EMPTY;

    public InfoBlock(byte[] bArray) {
        // decode magic byte
        StringBuilder magicByte = new StringBuilder();
        magicByte.append(String.format("%02X", bArray[0]));
        magicByte.append(String.format("%02X", bArray[1]));
        modbusId = new StringType(magicByte.toString());
        // logger.info("Magic byte: {}", magicByte.toString());

        // modbus firmware
        String modbusVersion = bArray[2] + "." + bArray[3];
        this.modbusVersion = new StringType(modbusVersion);
        // logger.info("Modbus version: {}", modbusVersion);

        int supportedRegisters = DataConverter.getIntValue(bArray, 4);
        this.supportedRegisters = new DecimalType(supportedRegisters);
        // logger.info("Supported Regiters: {}", supportedRegisters);

        String manufacturer = DataConverter.getString(bArray, 6);
        this.manufacturer = new StringType(manufacturer);
        // logger.info("Manufacturer: {}", manufacturer);

        String model = DataConverter.getString(bArray, 38);
        this.modelName = new StringType(model);
        // logger.info("Model: {}", model);

        String serialNumber = DataConverter.getString(bArray, 70);
        this.serialNumber = new StringType(serialNumber);
        // logger.info("Serial Number: {}", serialNumber);

        String firmware = DataConverter.getString(bArray, 102);
        this.firmware = new StringType(firmware);
        // logger.info("Firmware: {}", firmware);

        // create String with
        StringBuilder sb = new StringBuilder();
        sb.append(manufacturer).append("\n").append(modelName).append("\n").append(serialNumber).append("\n")
                .append(firmware);
        sb.append("\n").append(modbusId).append("\n").append(modbusVersion);
        allInfo = new StringType(sb.toString());
    }
}
