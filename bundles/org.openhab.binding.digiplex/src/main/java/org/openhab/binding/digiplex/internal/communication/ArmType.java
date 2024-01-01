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
package org.openhab.binding.digiplex.internal.communication;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Indicates arm type
 *
 * @author Robert Michalak - Initial contribution
 *
 */
@NonNullByDefault
public enum ArmType {
    REGULAR_ARM('A'),
    FORCE_ARM('F'),
    STAY_ARM('S'),
    INSTANT_ARM('I'),
    UNKNOWN('u');

    private char indicator;

    ArmType(char indicator) {
        this.indicator = indicator;
    }

    public char getIndicator() {
        return indicator;
    }

    public static ArmType fromMessage(char indicator) {
        return Arrays.stream(values()).filter(type -> type.indicator == indicator).findFirst().orElse(UNKNOWN);
    }
}
