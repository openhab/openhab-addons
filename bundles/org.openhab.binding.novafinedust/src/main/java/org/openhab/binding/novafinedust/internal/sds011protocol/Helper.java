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
package org.openhab.binding.novafinedust.internal.sds011protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Class with useful utility functions
 *
 * @author Stefan Triller - Initial contribution
 *
 */
@NonNullByDefault
public class Helper {

    private Helper() {
    }

    /**
     * Converts a byte array to a hexadecimal string, handy for printing
     *
     * @param bytes the byte array to be converted
     * @return a String describing the byte array in hexadecimal values
     */
    public static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }
}
