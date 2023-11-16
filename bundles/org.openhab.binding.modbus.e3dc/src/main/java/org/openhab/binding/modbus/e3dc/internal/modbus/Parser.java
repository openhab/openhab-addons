/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.modbus.e3dc.internal.modbus;

import static org.openhab.binding.modbus.e3dc.internal.modbus.E3DCModbusConstans.*;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.e3dc.internal.dto.EmergencyBlock;
import org.openhab.binding.modbus.e3dc.internal.dto.InfoBlock;
import org.openhab.binding.modbus.e3dc.internal.dto.PowerBlock;
import org.openhab.binding.modbus.e3dc.internal.dto.StringBlock;
import org.openhab.binding.modbus.e3dc.internal.dto.WallboxArray;
import org.openhab.binding.modbus.e3dc.internal.modbus.Data.DataType;
import org.openhab.core.io.transport.modbus.AsyncModbusReadResult;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Parser} class receives callbacks from modbus poller
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class Parser {
    private static final int MEASURE_COUNT = 100;
    private final Logger logger = LoggerFactory.getLogger(Parser.class);
    private DataType callbackType;
    private byte[] bArray;
    private int size;
    private int counter = 0;
    private long maxDuration = Long.MIN_VALUE;
    private long minDuration = Long.MAX_VALUE;
    private long avgDuration = 0;

    public Parser(DataType type) {
        callbackType = type;
        if (type.equals(DataType.INFO)) {
            size = INFO_REG_SIZE * 2;
            bArray = new byte[size];
        } else {
            size = (REGISTER_LENGTH - INFO_REG_SIZE) * 2;
            bArray = new byte[size];
        }
    }

    public void handle(AsyncModbusReadResult result) {
        long startTime = System.currentTimeMillis();
        Optional<ModbusRegisterArray> opt = result.getRegisters();
        if (opt.isPresent()) {
            setArray(opt.get().getBytes());

            long duration = System.currentTimeMillis() - startTime;
            avgDuration += duration;
            minDuration = Math.min(minDuration, duration);
            maxDuration = Math.max(maxDuration, duration);
            counter++;
            if (counter % MEASURE_COUNT == 0) {
                logger.debug("Min {} Max {} Avg {}", minDuration, maxDuration, avgDuration / MEASURE_COUNT);
                avgDuration = 0;
                minDuration = Long.MAX_VALUE;
                maxDuration = Long.MIN_VALUE;
            }
        } else {
            logger.warn("Modbus read result doesn't return expected registers");
        }
    }

    public synchronized void setArray(byte[] b) {
        if (b.length != size) {
            logger.warn("Wrong byte size received. Should be {} but is {}. Data maybe corrupted!", size, b.length);
        }
        bArray = b.clone();
    }

    public Optional<Data> parse(DataType type) {
        synchronized (bArray) {
            if (type.equals(DataType.INFO) && callbackType.equals(DataType.INFO)) {
                return Optional.of(new InfoBlock(Arrays.copyOfRange(bArray, INFO_REG_START, INFO_REG_SIZE * 2)));
            } else if (type.equals(DataType.POWER) && callbackType.equals(DataType.DATA)) {
                int start = (POWER_REG_START - INFO_REG_SIZE) * 2;
                int end = start + POWER_REG_SIZE * 2;
                return Optional.of(new PowerBlock(Arrays.copyOfRange(bArray, start, end)));
            } else if (type.equals(DataType.EMERGENCY) && callbackType.equals(DataType.DATA)) {
                int start = (EMS_REG_START - INFO_REG_SIZE) * 2;
                int end = start + EMS_REG_SIZE * 2;
                return Optional.of(new EmergencyBlock(Arrays.copyOfRange(bArray, start, end)));
            } else if (type.equals(DataType.WALLBOX) && callbackType.equals(DataType.DATA)) {
                int start = (WALLBOX_REG_START - INFO_REG_SIZE) * 2;
                int end = start + WALLBOX_REG_SIZE * 2;
                return Optional.of(new WallboxArray(Arrays.copyOfRange(bArray, start, end)));
            } else if (type.equals(DataType.STRINGS) && callbackType.equals(DataType.DATA)) {
                int start = (STRINGS_REG_START - INFO_REG_SIZE) * 2;
                int end = start + STRINGS_REG_SIZE * 2;
                return Optional.of(new StringBlock(Arrays.copyOfRange(bArray, start, end)));
            }
        }
        logger.warn("Wrong Block requested. Request is {} but type is {}", type, callbackType);
        return Optional.empty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getName()).append(":").append(callbackType);
        return sb.toString();
    }
}
