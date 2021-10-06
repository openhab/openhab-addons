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
 * Possible values for an inverter's device type field
 *
 * @author Ferdinand Schwenk - initial contribution
 */
public enum SungrowDeviceType {

    SH5K20(0xD09),
    SH3K6(0xD06),
    SH4K6(0xD07),
    SH5KV13(0xD03),
    SH5K30(0xD0C),
    SH3K630(0xD0A),
    SH4K630(0xD0B),
    SH50RS(0xD0F),
    SH36RS(0xD0D),
    SH46RS(0xD0E),
    SH60RS(0xD10),
    SH10RT(0xE03),
    SH80RT(0xE02),
    SH60RT(0xE01),
    SH50RT(0xE00),
    UNKNOWN(-1);

    private final int code;

    SungrowDeviceType(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static SungrowDeviceType getByCode(int code) {
        switch (code) {
            case 0xD09:
                return SungrowDeviceType.SH5K20;
            case 0xD06:
                return SungrowDeviceType.SH3K6;
            case 0xD07:
                return SungrowDeviceType.SH4K6;
            case 0xD03:
                return SungrowDeviceType.SH5KV13;
            case 0xD0C:
                return SungrowDeviceType.SH5K30;
            case 0xD0A:
                return SungrowDeviceType.SH3K630;
            case 0xD0B:
                return SungrowDeviceType.SH4K630;
            case 0xD0F:
                return SungrowDeviceType.SH50RS;
            case 0xD0D:
                return SungrowDeviceType.SH36RS;
            case 0xD0E:
                return SungrowDeviceType.SH46RS;
            case 0xD10:
                return SungrowDeviceType.SH60RS;
            case 0xE03:
                return SungrowDeviceType.SH10RT;
            case 0xE02:
                return SungrowDeviceType.SH80RT;
            case 0xE01:
                return SungrowDeviceType.SH60RT;
            case 0xE00:
                return SungrowDeviceType.SH50RT;
            default:
                return SungrowDeviceType.UNKNOWN;
        }
    }
}
