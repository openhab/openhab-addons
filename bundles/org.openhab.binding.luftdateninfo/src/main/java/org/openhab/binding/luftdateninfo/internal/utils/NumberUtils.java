/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.luftdateninfo.internal.utils;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link NumberUtils} class provides helpers for converting Numbers.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class NumberUtils {
    public static double round(Object o, int places) {
        // LOGGER.info("Round "+o);
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        Double value = null;
        if (o instanceof Integer) {
            value = (double) ((Integer) o).intValue();
        } else {
            value = (Double) o;
        }

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

}
