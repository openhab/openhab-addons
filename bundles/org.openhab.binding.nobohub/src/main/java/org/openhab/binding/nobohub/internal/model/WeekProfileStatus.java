/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
 * The status of the {@link WeekProfile}. What the value is in the week profile. Status OFF is matched both to value 3
 * and 4, while the documentation says 3, Hub with Hardware version 11123610_rev._1 and production date 20180305
 * will send value 4 for OFF.
 * compatibility.
 *
 * @author Jørgen Austvik - Initial contribution
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
public enum WeekProfileStatus {

    ECO(0),
    COMFORT(1),
    AWAY(2),
    OFF(3);

    private final int numValue;

    private WeekProfileStatus(int numValue) {
        this.numValue = numValue;
    }

    public static WeekProfileStatus getByNumber(int value) throws NoboDataException {
        switch (value) {
            case 0:
                return ECO;
            case 1:
                return COMFORT;
            case 2:
                return AWAY;
            case 3:
            case 4:
                return OFF;
            default:
                throw new NoboDataException(String.format("Unknown week profile status  %d", value));
        }
    }

    public int getNumValue() {
        return numValue;
    }
}
