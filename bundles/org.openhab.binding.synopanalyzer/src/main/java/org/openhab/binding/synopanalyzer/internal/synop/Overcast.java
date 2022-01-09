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
package org.openhab.binding.synopanalyzer.internal.synop;

/**
 * The {@link WindDirections} enum possible overcast descriptions
 *
 * @author Gaël L'hopital - Initial contribution
 */
public enum Overcast {
    UNDEFINED,
    CLEAR_SKY,
    CLOUDY,
    SKY_NOT_VISIBLE;

    /**
     * Returns the overcast level depending upon octa
     */
    public static Overcast fromOcta(int octa) {
        if (octa == 0) {
            return Overcast.CLEAR_SKY;
        } else if (octa > 0 && octa < 9) {
            return Overcast.CLOUDY;
        } else if (octa == 9) {
            return Overcast.SKY_NOT_VISIBLE;
        }
        return Overcast.UNDEFINED;
    }
}
