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

/**
 * Possible values for an inverter's system state field
 *
 * @author Ferdinand Schwenk - initial contribution
 */
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

    private final int code;

    SungrowSystemState(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static SungrowSystemState getByCode(int code) {
        switch (code) {
            case 0x0002:
                return SungrowSystemState.STOP;
            case 0x0008:
                return SungrowSystemState.STANDBY;
            case 0x0010:
                return SungrowSystemState.INITIALSTANDBY;
            case 0x0020:
                return SungrowSystemState.STARTUP;
            case 0x0040:
                return SungrowSystemState.RUNNING;
            case 0x0100:
                return SungrowSystemState.FAULT;
            case 0x0400:
                return SungrowSystemState.RUNNING_MAINTAIN;
            case 0x0800:
                return SungrowSystemState.RUNNING_FORCED;
            case 0x1000:
                return SungrowSystemState.RUNNING_OFFGRID;
            case 0x2501:
                return SungrowSystemState.RESTARTING;
            case 0x4000:
                return SungrowSystemState.RUNNING_EXTERNALEMS;
            default:
                return SungrowSystemState.UNKNOWN;
        }
    }
}
