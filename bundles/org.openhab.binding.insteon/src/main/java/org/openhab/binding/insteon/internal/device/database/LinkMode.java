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
package org.openhab.binding.insteon.internal.device.database;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link LinkMode} represents an Insteon all-link record linking mode
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public enum LinkMode {
    RESPONDER(0x00, RecordFlags.RESPONDER),
    CONTROLLER(0x01, RecordFlags.CONTROLLER),
    EITHER(0x03, RecordFlags.HIGH_WATER_MARK),
    UNKNOWN(0xFE, RecordFlags.HIGH_WATER_MARK),
    DELETE(0xFF, RecordFlags.INACTIVE);

    private static final Map<Integer, LinkMode> CODE_MAP = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(mode -> mode.code, Function.identity()));

    private final int code;
    private final RecordFlags flags;

    private LinkMode(int code, RecordFlags flags) {
        this.code = code;
        this.flags = flags;
    }

    public int getLinkCode() {
        return code;
    }

    public RecordType getRecordType() {
        return flags.getRecordType();
    }

    /**
     * Factory method for getting a LinkMode from a link code
     *
     * @param code the link code
     * @return the link mode
     */
    public static LinkMode valueOf(int code) {
        return CODE_MAP.getOrDefault(code, LinkMode.UNKNOWN);
    }
}
