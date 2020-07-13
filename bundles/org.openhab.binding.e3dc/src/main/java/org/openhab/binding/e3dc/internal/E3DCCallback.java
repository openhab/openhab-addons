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
package org.openhab.binding.e3dc.internal;

import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.jdt.annotation.NonNull;
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
 * The {@link E3DCCallback} class receives callbacks from modbus poller
 *
 * @author Bernd Weymann - Initial contribution
 */
public class E3DCCallback implements ModbusReadCallback {
    private final Logger logger = LoggerFactory.getLogger(E3DCCallback.class);

    @Override
    public void onRegisters(@NonNull ModbusReadRequestBlueprint request, @NonNull ModbusRegisterArray registers) {
        byte[] bArray = new byte[256];
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
        logger.info("Magic byte: {}", magicByte.toString());
        // modbus firmware
        String modbusVersion = bArray[2] + "." + bArray[3];
        logger.info("Modbus version: {}", modbusVersion);
        int supportedRegisters = getIntValue(bArray, 4);
        logger.info("Supported Regiters: {}", supportedRegisters);
        String manufacturer = getString(bArray, 6);
        logger.info("Manufacturer: {}", manufacturer);
        String model = getString(bArray, 38);
        logger.info("Model: {}", model);
        String serialNumber = getString(bArray, 70);
        logger.info("Serial Number: {}", serialNumber);
        String firmware = getString(bArray, 102);
        logger.info("Firmware: {}", firmware);
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

    private int getIntValue(byte[] bytes, int start) {
        return ((bytes[start] & 0xff) << 8) | (bytes[start + 1] & 0xff);
    }

    private String getString(byte[] bArray, int i) {
        byte[] slice = Arrays.copyOfRange(bArray, i, i + 32);
        return new String(slice);
    }

}
