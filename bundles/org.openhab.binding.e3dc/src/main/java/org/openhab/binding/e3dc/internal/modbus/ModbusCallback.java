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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.e3dc.internal.dto.InfoBlock;
import org.openhab.binding.e3dc.internal.dto.PowerBlock;
import org.openhab.binding.e3dc.internal.dto.StringBlock;
import org.openhab.binding.e3dc.internal.modbus.Data.DataType;
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
@NonNullByDefault
public class ModbusCallback extends ModbusDataProvider implements ModbusReadCallback {
    public static final int REGISTER_LENGTH = 104;

    private final Logger logger = LoggerFactory.getLogger(ModbusCallback.class);
    private @Nullable InfoBlock infoBlock;
    private byte[] bArray = new byte[REGISTER_LENGTH * 2];
    private int counter = 0;
    private long maxDuration = Long.MIN_VALUE;
    private long minDuration = Long.MAX_VALUE;
    private long avgDuration = 0;

    @Override
    public void onRegisters(ModbusReadRequestBlueprint request, @NonNull ModbusRegisterArray registers) {
        byte[] newArray = new byte[REGISTER_LENGTH * 2];
        long startTime = System.currentTimeMillis();
        Iterator<ModbusRegister> iter = registers.iterator();
        int i = 0;
        int registerCounter = 0;
        while (iter.hasNext()) {
            ModbusRegister reg = iter.next();
            // if (counter % 30 == 0) {
            // logger.info("Reg {} value {} bytes {}", registerCounter, reg.getValue(), reg.getBytes());
            // }
            System.arraycopy(reg.getBytes(), 0, newArray, i, 2);
            i += 2;
            // byte[] b = reg.getBytes();
            // for (int j = 0; j < b.length; j++) {
            // newArray[i] = b[j];
            // i++;
            // }
            registerCounter++;
        }

        // logger.info("######################################");
        // logger.info("Byte size {}", newArray.length);
        // for (int j = 0; j < newArray.length; j++) {
        // logger.info("Byte {} is {}", j, newArray[j]);
        // }

        synchronized (bArray) {
            bArray = newArray.clone();
        }
        super.informAllListeners();

        long duration = System.currentTimeMillis() - startTime;
        avgDuration += duration;
        minDuration = Math.min(minDuration, duration);
        maxDuration = Math.max(maxDuration, duration);
        counter++;
        if (counter % 30 == 0) {
            logger.info("Min {} Max {} Avg {}", minDuration, maxDuration, avgDuration / 30);
            avgDuration = 0;
            minDuration = Long.MAX_VALUE;
            maxDuration = Long.MIN_VALUE;
        }
    }

    @Override
    public void onBits(ModbusReadRequestBlueprint request, BitArray bits) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onError(ModbusReadRequestBlueprint request, Exception error) {
        ModbusUnexpectedTransactionIdException e;
        ModbusTransportException e1;
        ModbusSlaveException e2;
    }

    @Override
    public @Nullable Data getData(DataType type) {
        synchronized (bArray) {
            if (type.equals(DataType.INFO)) {
                return new InfoBlock(Arrays.copyOfRange(bArray, 0, 133));
            } else if (type.equals(DataType.POWER)) {
                return new PowerBlock(Arrays.copyOfRange(bArray, 67 * 2, 67 * 2 + 32));
            } else if (type.equals(DataType.EMERGENCY)) {
                return new StringBlock(Arrays.copyOfRange(bArray, 83 * 2, 83 * 2 + 4));
            } else if (type.equals(DataType.STRINGS)) {
                return new StringBlock(Arrays.copyOfRange(bArray, 95 * 2, 95 * 2 + 18));
            }
            return null;
        }
    }
}
