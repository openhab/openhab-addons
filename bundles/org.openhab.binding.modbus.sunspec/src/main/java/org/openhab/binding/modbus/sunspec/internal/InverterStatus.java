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
package org.openhab.binding.modbus.sunspec.internal;

/**
 * Possible values for an inverter's status field
 *
 * @author Nagy Attila Gábor - Initial contribution
 */
public enum InverterStatus {

    OFF(1),
    SLEEP(2),
    ON(4),
    UNKNOWN(-1);

    private final int code;

    InverterStatus(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static InverterStatus getByCode(int code) {
        switch (code) {
            case 1:
                return InverterStatus.OFF;
            case 2:
                return InverterStatus.SLEEP;
            case 4:
                return InverterStatus.ON;
            default:
                return InverterStatus.UNKNOWN;
        }
    }
}
