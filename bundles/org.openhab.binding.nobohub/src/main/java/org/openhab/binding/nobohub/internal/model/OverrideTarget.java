/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
 * The target of the {@link OverridePlan}. What it applies to.
 *
 * @author Jørgen Austvik - Initial contribution
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
public enum OverrideTarget {

    HUB(0),
    ZONE(1),
    COMPONENT(2);

    private final int numValue;

    private OverrideTarget(int numValue) {
        this.numValue = numValue;
    }

    public static OverrideTarget getByNumber(int value) throws NoboDataException {
        switch (value) {
            case 0:
                return HUB;
            case 1:
                return ZONE;
            case 2:
                return COMPONENT;
            default:
                throw new NoboDataException(String.format("Unknown override target %d", value));
        }
    }

    public int getNumValue() {
        return numValue;
    }
}
