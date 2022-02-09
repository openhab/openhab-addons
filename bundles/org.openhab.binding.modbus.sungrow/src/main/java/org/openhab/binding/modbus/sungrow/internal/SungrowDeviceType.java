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
 * Possible values for an inverter's device type field
 *
 * @author Ferdinand Schwenk - initial contribution
 */
@NonNullByDefault
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

    private static final Map<Integer, SungrowDeviceType> sungrowDeviceTypeIndex = new HashMap<>();

    static {
        for (SungrowDeviceType code : SungrowDeviceType.values()) {
            sungrowDeviceTypeIndex.put(code.getCode(), code);
        }
    }

    private final int code;

    SungrowDeviceType(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    @Nullable
    public static SungrowDeviceType getByCode(int code) {
        return sungrowDeviceTypeIndex.get(code);
    }
}
