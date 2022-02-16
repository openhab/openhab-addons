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
public enum SungrowGridState {

    OFF_GRID(0xAA),
    ON_GRID(0x55),
    UNKNOWN(-1);

    private static final Map<Integer, SungrowGridState> SungrowGridStateIndex = new HashMap<>();

    static {
        for (SungrowGridState code : SungrowGridState.values()) {
            SungrowGridStateIndex.put(code.getCode(), code);
        }
    }

    private final int code;

    SungrowGridState(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    @Nullable
    public static SungrowGridState getByCode(int code) {
        return SungrowGridStateIndex.get(code);
    }
}
