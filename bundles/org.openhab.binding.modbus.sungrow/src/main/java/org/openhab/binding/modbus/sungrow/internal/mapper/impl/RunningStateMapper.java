/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.modbus.sungrow.internal.mapper.impl;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.sungrow.internal.mapper.ToStringMapper;

/**
 * This mapper implements {@link ToStringMapper} and maps the hex codes of the sungrow modbus register to the human
 * readable running states.
 *
 * @author Tim Scholand - Initial contribution
 */
@NonNullByDefault
public class RunningStateMapper implements ToStringMapper {

    private static final RunningStateMapper INSTANCE = new RunningStateMapper();

    /**
     * @return a singleton instance of the mapper
     */
    public static RunningStateMapper instance() {
        return INSTANCE;
    }

    private RunningStateMapper() {
        // use instance()
    }

    @Override
    public String map(BigDecimal value) {
        return switch (value.intValue()) {
            case 0x0000, 0x0040 -> "RUNNING";
            case 0x0041 -> "OFF_GRID_CHARGE";
            case 0x0200 -> "UPDATE_FAILED";
            case 0x0400 -> "MAINTAIN_MODE";
            case 0x0800 -> "FORCED_MODE";
            case 0x1000 -> "OFF_GRID_MODE";
            case 0x1111 -> "UNINITIALIZED";
            case 0x1200, 0x0010 -> "INITIAL_STANDBY";
            case 0x1300, 0x0002 -> "SHUTDOWN";
            case 0x1400, 0x0008 -> "STANDBY";
            case 0x1500, 0x0004 -> "EMERGENCY_STOP";
            case 0x1600, 0x0020 -> "STARTUP";
            case 0x1700 -> "AFCI_SELF_TEST_SHUTDOWN";
            case 0x1800 -> "INTELLIGENT_STATION_BUILDING_STATUS";
            case 0x1900 -> "SAFE_MODE";
            case 0x2000 -> "OPEN_LOOP";
            case 0x2501 -> "RESTARTING";
            case 0x4000 -> "RUNNING_EXTERNAL_EMS_MODE";
            case 0x4001 -> "EMERGENCY_CHARGING_OPERATION";
            case 0x5500, 0x0100 -> "FAULT";
            case 0x8000, 0x0001 -> "STOP";
            case 0x8100 -> "DERATING_RUNNING";
            case 0x8200 -> "DISPATCH_RUNNING";
            case 0x9100 -> "WARN_RUN";
            default -> "UNKNOWN: " + String.format("0x%04X", value.intValue());
        };
    }
}
