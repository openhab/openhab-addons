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
package org.openhab.binding.boschshc.internal.services.vibrationsensor.dto;

/**
 * Possible vibration sensor sensitivity settings.
 * 
 * @author David Pace - Initial contribution
 *
 */
public enum VibrationSensorSensitivity {
    VERY_HIGH,
    HIGH,
    MEDIUM,
    LOW,
    VERY_LOW;

    /**
     * Returns the sensitivity matching the given string or <code>null</code> if no match was found.
     * 
     * @param identifier the string identifier of a vibration sensor sensitivity
     * @return the matching sensitivity enum value or <code>null</code>
     */
    public static VibrationSensorSensitivity from(String identifier) {
        try {
            return VibrationSensorSensitivity.valueOf(identifier);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
