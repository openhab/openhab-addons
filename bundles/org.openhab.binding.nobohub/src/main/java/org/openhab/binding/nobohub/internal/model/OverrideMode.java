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
package org.openhab.binding.nobohub.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The mode of the {@link OverridePlan}. What the value is overridden to.
 *
 * @author Jørgen Austvik - Initial contribution
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
public enum OverrideMode {

    NORMAL(0),
    COMFORT(1),
    ECO(2),
    AWAY(3);

    private final int numValue;

    OverrideMode(int numValue) {
        this.numValue = numValue;
    }

    public static OverrideMode getByNumber(int value) throws NoboDataException {
        switch (value) {
            case 0:
                return NORMAL;
            case 1:
                return COMFORT;
            case 2:
                return ECO;
            case 3:
                return AWAY;
            default:
                throw new NoboDataException(String.format("Unknown override mode %d", value));
        }
    }

    public int getNumValue() {
        return numValue;
    }

    public static OverrideMode getByName(String name) throws NoboDataException {
        if (name.isEmpty()) {
            throw new NoboDataException("Missing name");
        }

        if ("Normal".equalsIgnoreCase(name)) {
            return NORMAL;
        } else if ("Comfort".equalsIgnoreCase(name)) {
            return COMFORT;
        } else if ("Eco".equalsIgnoreCase(name)) {
            return ECO;
        } else if ("Away".equalsIgnoreCase(name)) {
            return AWAY;
        }

        throw new NoboDataException(String.format("Unknown name of override mode: '%s'", name));
    }
}
