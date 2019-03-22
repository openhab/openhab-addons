/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
