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
package org.openhab.binding.pleos.internal.model;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Supported vehicle powertrain types according to Pleos API specification.
 *
 * @author Stephan Sandmann - Initial contribution
 */
@NonNullByDefault
public enum VehicleType {
    GN("Internal Combustion"),
    EV("Electric"),
    HV("Hybrid"),
    PE("Plug-in Hybrid"),
    FV("Fuel Cell Electric"),
    UNKNOWN("Unknown");

    private final String description;

    VehicleType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Safely extracts the type from the API string (with a fallback to UNKNOWN).
     */
    public static VehicleType fromApiValue(@Nullable String rawType) {
        if (rawType == null || rawType.isBlank()) {
            return UNKNOWN;
        }
        try {
            return VehicleType.valueOf(rawType.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }

    /**
     * Returns true if the vehicle has a high-voltage battery that can be charged
     * (EV, plug-in hybrid, or unknown).
     */
    public boolean supportsCharging() {
        return this == EV || this == PE || this == UNKNOWN;
    }

    /**
     * Returns true if the vehicle has a combustion/hydrogen tank
     * (GN, HV, PE, FV, or unknown).
     */
    public boolean supportsFuel() {
        return this == GN || this == HV || this == PE || this == FV || this == UNKNOWN;
    }
}
