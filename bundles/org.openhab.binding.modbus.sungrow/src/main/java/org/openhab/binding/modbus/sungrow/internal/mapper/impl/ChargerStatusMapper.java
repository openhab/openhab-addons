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

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.sungrow.internal.mapper.ToStringMapper;

/**
 * Maps charger status codes to human readable values.
 *
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
public class ChargerStatusMapper implements ToStringMapper {

    private static final ChargerStatusMapper INSTANCE = new ChargerStatusMapper();

    /**
     * @return a singleton instance of the mapper
     */
    public static ChargerStatusMapper instance() {
        return INSTANCE;
    }

    private ChargerStatusMapper() {
        // use instance()
    }

    @Override
    public String map(BigDecimal value) {
        return switch (value.intValue()) {
            case 1 -> "Idle (unplugged)";
            case 2 -> "Standby (plugged)";
            case 3 -> "Charging";
            case 6 -> "Charging completed";
            default -> "UNKNOWN: " + value.intValue();
        };
    }
}
