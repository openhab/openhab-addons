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
package org.openhab.binding.e3dc.internal.modbus;

import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.openhab.binding.e3dc.internal.dto.E3DCInfoBlock;
import org.openhab.io.transport.modbus.BitArray;
import org.openhab.io.transport.modbus.ModbusReadCallback;
import org.openhab.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.io.transport.modbus.ModbusRegister;
import org.openhab.io.transport.modbus.ModbusRegisterArray;
import org.openhab.io.transport.modbus.ModbusTransportException;
import org.openhab.io.transport.modbus.ModbusUnexpectedTransactionIdException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.wimpi.modbus.ModbusSlaveException;

/**
 * The {@link InfoBlockCallback} class receives callbacks from modbus poller
 *
 * @author Bernd Weymann - Initial contribution
 */
public class InfoBlockCallback extends BaseCallback implements ModbusReadCallback {
    private final Logger logger = LoggerFactory.getLogger(InfoBlockCallback.class);

    private E3DCInfoBlock infoBlock = new E3DCInfoBlock();

    @Override
    public void onRegisters(@NonNull ModbusReadRequestBlueprint request, @NonNull ModbusRegisterArray registers) {
        byte[] bArray = new byte[68 * 2];
        Iterator<ModbusRegister> iter = registers.iterator();
        int i = 0;
        while (iter.hasNext()) {
            ModbusRegister reg = iter.next();
            byte[] b = reg.getBytes();
            for (int j = 0; j < b.length; j++) {
                bArray[i] = b[j];
                i++;
            }
        }
        // decode magic byte
        StringBuilder magicByte = new StringBuilder();
        magicByte.append(String.format("%02X", bArray[0]));
        magicByte.append(String.format("%02X", bArray[1]));
        infoBlock.modbusId = new StringType(magicByte.toString());
        logger.info("Magic byte: {}", magicByte.toString());

        // modbus firmware
        String modbusVersion = bArray[2] + "." + bArray[3];
        infoBlock.modbusVersion = new StringType(modbusVersion);
        logger.info("Modbus version: {}", modbusVersion);

        int supportedRegisters = getIntValue(bArray, 4);
        infoBlock.supportedRegisters = new DecimalType(supportedRegisters);
        logger.info("Supported Regiters: {}", supportedRegisters);

        String manufacturer = getString(bArray, 6);
        infoBlock.manufacturer = new StringType(manufacturer);
        logger.info("Manufacturer: {}", manufacturer);

        String model = getString(bArray, 38);
        infoBlock.modelName = new StringType(model);
        logger.info("Model: {}", model);

        String serialNumber = getString(bArray, 70);
        infoBlock.serialNumber = new StringType(serialNumber);
        logger.info("Serial Number: {}", serialNumber);

        String firmware = getString(bArray, 102);
        infoBlock.firmware = new StringType(firmware);
        logger.info("Firmware: {}", firmware);

        super.informAllListeners();
    }

    @Override
    public void onBits(@NonNull ModbusReadRequestBlueprint request, @NonNull BitArray bits) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onError(@NonNull ModbusReadRequestBlueprint request, @NonNull Exception error) {
        ModbusUnexpectedTransactionIdException e;
        ModbusTransportException e1;
        ModbusSlaveException e2;
    }

    public E3DCInfoBlock getData() {
        return infoBlock;
    }

    private int getIntValue(byte[] bytes, int start) {
        return ((bytes[start] & 0xff) << 8) | (bytes[start + 1] & 0xff);
    }

    private String getString(byte[] bArray, int i) {
        byte[] slice = Arrays.copyOfRange(bArray, i, i + 32);
        return new String(slice);
    }
}
