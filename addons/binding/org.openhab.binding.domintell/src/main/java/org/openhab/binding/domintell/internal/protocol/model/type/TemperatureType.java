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
 * The {@link TemperatureType} it the enumeration of temperature modes.
 *
 * @author Gabor Bicskei - Initial contribution
 */
public enum TemperatureType implements SelectionProvider {

    ABSENCE(1),
    AUTO(2),
    COMFORT(5),
    FROST(6);

    private final int value;

    TemperatureType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TemperatureType byValue(int value) {
        switch (value) {
            case 1:
                return TemperatureType.ABSENCE;
            case 2:
                return AUTO;
            case 5:
                return COMFORT;
            case 6:
                return FROST;
        }
        throw new IllegalStateException("Unknown enum value: " + value);
    }
}
