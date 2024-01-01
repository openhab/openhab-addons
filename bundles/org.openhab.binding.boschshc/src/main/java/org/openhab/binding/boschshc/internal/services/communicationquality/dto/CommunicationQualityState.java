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
package org.openhab.binding.boschshc.internal.services.communicationquality.dto;

import org.openhab.core.library.types.DecimalType;

/**
 * Possible states for the communication quality between a device and the
 * bridge.
 * 
 * @author David Pace - Initial contribution
 *
 */
public enum CommunicationQualityState {
    BAD,
    MEDIUM,
    NORMAL,
    GOOD,
    UNKNOWN,
    FETCHING;

    /**
     * Converts this Bosch-specific communication quality state into a numeric state
     * for the system channel of type <code>signal-strength</code>.
     * 
     * @return
     */
    public DecimalType toSystemSignalStrength() {
        switch (this) {
            case BAD:
                return new DecimalType(1);
            case MEDIUM:
                return new DecimalType(2);
            case NORMAL:
                return new DecimalType(3);
            case GOOD:
                return new DecimalType(4);
            default:
                // includes UNKNOWN and FETCHING
                return new DecimalType(0);
        }
    }
}
