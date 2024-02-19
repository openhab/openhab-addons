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
public enum SungrowOutputType {

    OUTPUT_SINGLEPHASE(0),
    OUTPUT_3P4L(1),
    OUTPUT_3P3L(2),
    UNKNOWN(-1);

    private static final Map<Integer, SungrowOutputType> sungrowOutputTypeIndex = new HashMap<>();

    static {
        for (SungrowOutputType code : SungrowOutputType.values()) {
            sungrowOutputTypeIndex.put(code.getCode(), code);
        }
    }

    private final int code;

    SungrowOutputType(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    @Nullable
    public static SungrowOutputType getByCode(int code) {
        return sungrowOutputTypeIndex.get(code);
    }
}
