/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
 * Possible values for an inverter's status field
 *
 * @author Nagy Attila Gábor - Initial contribution
 * @author Ferdinand Schwenk - reused for sungrow bundle
 */
@NonNullByDefault
public enum InverterStatus {

    OFF(1),
    SLEEP(2),
    ON(4),
    UNKNOWN(-1);

    private static final Map<Integer, InverterStatus> inverterStatusCodesIndex = new HashMap<>();

    static {
        for (InverterStatus code : InverterStatus.values()) {
            inverterStatusCodesIndex.put(code.getCode(), code);
        }
    }

    private int code;

    private InverterStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    @Nullable
    public static InverterStatus getByCode(int code) {
        return inverterStatusCodesIndex.get(code);
    }
}
