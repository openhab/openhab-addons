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
package org.openhab.binding.modbus.kermi.internal.modbus;

import static org.openhab.binding.modbus.kermi.internal.modbus.KermiModbusConstans.ALARM_REG_SIZE;
import static org.openhab.binding.modbus.kermi.internal.modbus.KermiModbusConstans.CHARGING_CIRCUIT_REG_SIZE;
import static org.openhab.binding.modbus.kermi.internal.modbus.KermiModbusConstans.DATA_REGISTER_LENGTH;
import static org.openhab.binding.modbus.kermi.internal.modbus.KermiModbusConstans.ENERGY_SOURCE_REG_SIZE;
import static org.openhab.binding.modbus.kermi.internal.modbus.KermiModbusConstans.POWER_REG_SIZE;
import static org.openhab.binding.modbus.kermi.internal.modbus.KermiModbusConstans.PV_MODULATION_REG_SIZE;
import static org.openhab.binding.modbus.kermi.internal.modbus.KermiModbusConstans.STATE_REG_SIZE;
import static org.openhab.binding.modbus.kermi.internal.modbus.KermiModbusConstans.WORK_HOURS_REG_SIZE;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.kermi.internal.dto.AlarmDTO;
import org.openhab.binding.modbus.kermi.internal.dto.ChargingCircuitDTO;
import org.openhab.binding.modbus.kermi.internal.dto.EnergySourceDTO;
import org.openhab.binding.modbus.kermi.internal.dto.PowerDTO;
import org.openhab.binding.modbus.kermi.internal.dto.PvDTO;
import org.openhab.binding.modbus.kermi.internal.dto.StateDTO;
import org.openhab.binding.modbus.kermi.internal.dto.WorkHoursDTO;
import org.openhab.binding.modbus.kermi.internal.modbus.Data.DataType;
import org.openhab.core.io.transport.modbus.AsyncModbusReadResult;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Parser} class receives callbacks from modbus poller
 *
 * @author Bernd Weymann - Initial contribution
 * @author Kai Neuhaus - adapted for Kermi
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
        if (type.equals(DataType.STATE)) {
            size = (STATE_REG_SIZE) * 2;
            bArray = new byte[size];
        } else if (type.equals(DataType.ENERGY_SOURCE)) {
            size = ENERGY_SOURCE_REG_SIZE * 2;
            bArray = new byte[size];
        } else if (type.equals(DataType.CHARGING_CIRCUIT)) {
            size = CHARGING_CIRCUIT_REG_SIZE * 2;
            bArray = new byte[size];
        } else if (type.equals(DataType.POWER)) {
            size = POWER_REG_SIZE * 2;
            bArray = new byte[size];
        } else if (type.equals(DataType.ALARM_STATE)) {
            size = ALARM_REG_SIZE * 2;
            bArray = new byte[size];
        } else if (type.equals(DataType.PV)) {
            size = PV_MODULATION_REG_SIZE * 2;
            bArray = new byte[size];
        } else if (type.equals(DataType.WORK_HOURS)) {
            size = WORK_HOURS_REG_SIZE * 2;
            bArray = new byte[size];
        } else {
            size = DATA_REGISTER_LENGTH * 2;
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
            if (type.equals(DataType.STATE) && callbackType.equals(DataType.STATE)) {
                return Optional.of(new StateDTO(bArray));
            } else if (type.equals(DataType.ENERGY_SOURCE) && callbackType.equals(DataType.ENERGY_SOURCE)) {
                return Optional.of(new EnergySourceDTO(bArray));
            } else if (type.equals(DataType.CHARGING_CIRCUIT) && callbackType.equals(DataType.CHARGING_CIRCUIT)) {
                return Optional.of(new ChargingCircuitDTO(bArray));
            } else if (type.equals(DataType.POWER) && callbackType.equals(DataType.POWER)) {
                return Optional.of(new PowerDTO(bArray));
            } else if (type.equals(DataType.PV) && callbackType.equals(DataType.PV)) {
                return Optional.of(new PvDTO(bArray));
            } else if (type.equals(DataType.ALARM_STATE) && callbackType.equals(DataType.ALARM_STATE)) {
                return Optional.of(new AlarmDTO(bArray));
            } else if (type.equals(DataType.WORK_HOURS) && callbackType.equals(DataType.WORK_HOURS)) {
                return Optional.of(new WorkHoursDTO(bArray));
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
