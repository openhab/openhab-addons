/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.modbus.sungrow.internal.mapper.impl;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Maps the integer codes of the sungrow modbus register to the human readable output types.
 *
 * @author Tim Scholand - Initial contribution
 */
@NonNullByDefault
public class OutputTypeMapper {

    private static final OutputTypeMapper INSTANCE = new OutputTypeMapper();

    /**
     * @return a singleton instance of the mapper
     */
    public static OutputTypeMapper instance() {
        return INSTANCE;
    }

    private OutputTypeMapper() {
        // use instance()
    }

    /**
     * Maps value to string.
     */
    public String map(int value) {
        return switch (value) {
            case 0 -> "SINGLE";
            case 1 -> "3P4L";
            case 2 -> "3P3L";
            default -> "UNKNOWN: " + value;
        };
    }
}
