/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.modbus.sungrow.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Possible values for an inverter's system state field
 *
 * @author Ferdinand Schwenk - initial contribution
 */
@NonNullByDefault
public enum SungrowSystemState {

    STOP(0x0002),
    STANDBY(0x0008),
    INITIALSTANDBY(0x0010),
    STARTUP(0x0020),
    RUNNING(0x0040),
    FAULT(0x0100),
    RUNNING_MAINTAIN(0x0400),
    RUNNING_FORCED(0x0800),
    RUNNING_OFFGRID(0x1000),
    RESTARTING(0x2501),
    RUNNING_EXTERNALEMS(0x4000),
    UNKNOWN(-1);

    private static final Map<Integer, SungrowSystemState> sungrowSystemStateIndex = new HashMap<>();

    static {
        for (SungrowSystemState code : SungrowSystemState.values()) {
            sungrowSystemStateIndex.put(code.getCode(), code);
        }
    }

    private final int code;

    SungrowSystemState(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    @Nullable
    public static SungrowSystemState getByCode(int code) {
        return sungrowSystemStateIndex.get(code);
    }
}
