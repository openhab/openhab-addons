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

import static org.openhab.binding.e3dc.internal.modbus.E3DCModbusConstans.*;

import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.e3dc.internal.dto.EmergencyBlock;
import org.openhab.binding.e3dc.internal.dto.InfoBlock;
import org.openhab.binding.e3dc.internal.dto.PowerBlock;
import org.openhab.binding.e3dc.internal.dto.StringBlock;
import org.openhab.binding.e3dc.internal.dto.WallboxArray;
import org.openhab.binding.e3dc.internal.modbus.Data.DataType;
import org.openhab.io.transport.modbus.BitArray;
import org.openhab.io.transport.modbus.ModbusReadCallback;
import org.openhab.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.io.transport.modbus.ModbusRegister;
import org.openhab.io.transport.modbus.ModbusRegisterArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link InfoBlockCallback} class receives callbacks from modbus poller
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ModbusCallback extends ModbusDataProvider implements ModbusReadCallback {
    private final Logger logger = LoggerFactory.getLogger(ModbusCallback.class);
    private @Nullable InfoBlock infoBlock;
    private DataType callbackType;
    private byte[] bArray;
    private int counter = 0;
    private long maxDuration = Long.MIN_VALUE;
    private long minDuration = Long.MAX_VALUE;
    private long avgDuration = 0;

    public ModbusCallback(DataType type) {
        callbackType = type;
        if (type.equals(DataType.INFO)) {
            bArray = new byte[INFO_REG_SIZE * 2];
        } else {
            bArray = new byte[(REGISTER_LENGTH - INFO_REG_SIZE) * 2];
        }
    }

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
    }

    @Override
    public @Nullable Data getData(DataType type) {
        synchronized (bArray) {
            if (type.equals(DataType.INFO) && callbackType.equals(DataType.INFO)) {
                return new InfoBlock(Arrays.copyOfRange(bArray, INFO_REG_START, INFO_REG_SIZE * 2));
            } else if (type.equals(DataType.POWER) && callbackType.equals(DataType.DATA)) {
                int start = (POWER_REG_START - INFO_REG_SIZE) * 2;
                int end = start + POWER_REG_SIZE * 2;
                return new PowerBlock(Arrays.copyOfRange(bArray, start, end));
            } else if (type.equals(DataType.EMERGENCY) && callbackType.equals(DataType.DATA)) {
                int start = (EMS_REG_START - INFO_REG_SIZE) * 2;
                int end = start + EMS_REG_SIZE * 2;
                return new EmergencyBlock(Arrays.copyOfRange(bArray, start, end));
            } else if (type.equals(DataType.WALLBOX) && callbackType.equals(DataType.DATA)) {
                int start = (WALLBOX_REG_START - INFO_REG_SIZE) * 2;
                int end = start + WALLBOX_REG_SIZE * 2;
                return new WallboxArray(Arrays.copyOfRange(bArray, start, end));
            } else if (type.equals(DataType.STRINGS) && callbackType.equals(DataType.DATA)) {
                int start = (STRINGS_REG_START - INFO_REG_SIZE) * 2;
                int end = start + STRINGS_REG_SIZE * 2;
                return new StringBlock(Arrays.copyOfRange(bArray, start, end));
            }
            return null;
        }
    }
}
