/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.digiplex.communication;

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
