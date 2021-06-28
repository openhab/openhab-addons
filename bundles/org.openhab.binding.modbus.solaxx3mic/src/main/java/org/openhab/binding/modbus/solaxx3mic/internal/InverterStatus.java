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
package org.openhab.binding.modbus.solaxx3mic.internal;

/**
 * The {@link InverterStatus} describes
 * possible values for an inverter's status field
 *
 * @author Stanislaw Wawszczak - Initial contribution
 */
public enum InverterStatus {
    WAIT(0),
    CHECK(1),
    NORMAL(2),
    FAULT(3),
    PERMANENT_FAULT(4),
    UPDATE(5),
    EPS_CHECK(6),
    EPS(7),
    SELF_TEST(8),
    IDLE(9),
    STANDBY(10),
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
            case 0:
                return InverterStatus.WAIT;
            case 1:
                return InverterStatus.CHECK;
            case 2:
                return InverterStatus.NORMAL;
            case 3:
                return InverterStatus.FAULT;
            case 4:
                return InverterStatus.PERMANENT_FAULT;
            case 5:
                return InverterStatus.UPDATE;
            case 6:
                return InverterStatus.EPS_CHECK;
            case 7:
                return InverterStatus.EPS;
            case 8:
                return InverterStatus.SELF_TEST;
            case 9:
                return InverterStatus.IDLE;
            case 10:
                return InverterStatus.STANDBY;
            default:
                return InverterStatus.UNKNOWN;
        }
    }
}
