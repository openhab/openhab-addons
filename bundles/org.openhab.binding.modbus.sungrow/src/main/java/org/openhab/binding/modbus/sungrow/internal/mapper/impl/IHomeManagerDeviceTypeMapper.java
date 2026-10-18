/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
 * Maps the hex codes of the sungrow modbus register to the human readable device names.
 *
 * @author Tim Scholand - Initial contribution
 */
@NonNullByDefault
public class IHomeManagerDeviceTypeMapper {

    private static final IHomeManagerDeviceTypeMapper INSTANCE = new IHomeManagerDeviceTypeMapper();

    /**
     * @return a singleton instance of the mapper
     */
    public static IHomeManagerDeviceTypeMapper instance() {
        return INSTANCE;
    }

    private IHomeManagerDeviceTypeMapper() {
        // use instance()
    }

    /**
     * Maps value to string.
     */
    public String map(int value) {
        return switch (value) {
            case 0x072A -> "iHomeManager";
            default -> "UNKNOWN: " + String.format("0x%03X", value);
        };
    }
}
