/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.protocol.model.type;

/**
 * The {@link RegulationType} is enumeration of temperature regulation types
 *
 * @author Gabor Bicskei - Initial contribution
 */
public enum RegulationType implements SelectionProvider {
    OFF(0),
    HEATING(1),
    COOLING(2),
    AUTO(3);

    private final int value;

    RegulationType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static RegulationType byValue(int value) {
        switch (value) {
            case 0:
                return RegulationType.OFF;
            case 1:
                return HEATING;
            case 2:
                return COOLING;
            case 3:
                return AUTO;
        }
        throw new IllegalStateException("Unknown enum value: " + value);
    }
}
