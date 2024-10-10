/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.insteon.internal.device;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link X10Command} represents an X10 command
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public enum X10Command {
    ALL_UNITS_OFF(0x00),
    ALL_LIGHTS_ON(0x01),
    ALL_LIGHTS_OFF(0x06),
    ON(0x02),
    OFF(0x03),
    DIM(0x04),
    BRIGHT(0x05),
    EXTENDED_CODE(0x07),
    HAIL_REQUEST(0x08),
    HAIL_ACKNOWLEDGEMENT(0x09),
    PRESET_DIM_1(0x0A),
    PRESET_DIM_2(0x0B),
    EXTENDED_DATA(0x0C),
    STATUS_ON(0x0D),
    STATUS_OFF(0x0E),
    STATUS_REQUEST(0x0F);

    private final byte code;

    private X10Command(int code) {
        this.code = (byte) code;
    }

    public byte code() {
        return code;
    }
}
