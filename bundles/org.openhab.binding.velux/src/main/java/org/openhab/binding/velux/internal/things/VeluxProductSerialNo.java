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
package org.openhab.binding.velux.internal.things;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class support handling of Serial Number used with Velux.
 * <UL>
 * <LI>{@link #UNKNOWN} defines an unknown serial number as constant,</LI>
 * </UL>
 * <UL>
 * <LI>{@link #toString} converts the serial number as array of bytes into a human-readable String,</LI>
 * <LI>{@link #isInvalid} evaluates whether the given serial number is valid,</LI>
 * <LI>{@link #indicatesRevertedValues} returns a flag whether the serial number indicates inversion,</LI>
 * <LI>{@link #cleaned} returns a plain serial number without any inverse indicators.</LI>
 * </UL>
 * <P>
 * Example:
 * <UL>
 * <LI><I>12:23:34:45:56:67:78:89</I> represents a normal serial number,</LI>
 * <LI><I>12:23:34:45:56:67:78:89*</I> represents a serial number which leads to an inverted value handling.</LI>
 * </UL>
 *
 * @author Guenther Schreiner - Initial contribution
 */
@NonNullByDefault
public final class VeluxProductSerialNo {

    /*
     * ***************************
     * ***** Private Objects *****
     */

    private static final String HEXBYTE_SEPARATOR = ":";
    private static final char SUFFIX_MARKER = '*';

    /*
     * **************************
     * ***** Public Objects *****
     */

    public static final String UNKNOWN = "00:00:00:00:00:00:00:00";

    /*
     * ************************
     * ***** Constructors *****
     */

    // Suppress default constructor for creating a non-instantiable class.

    private VeluxProductSerialNo() {
        throw new AssertionError();
    }

    /*
     * ***************************
     * ***** Utility Methods *****
     */

    /**
     * Returns the complete serial number as human-readable sequence of hex bytes each separated by the given separator.
     *
     * @param serialNumber as array of Type byte.
     * @param separator as of Type String.
     * @return <b>serialNumberString</b> of type String.
     */
    public static String toString(byte[] serialNumber, String separator) {
        StringBuilder sb = new StringBuilder();
        for (byte b : serialNumber) {
            sb.append(String.format("%02X", b));
            sb.append(separator);
        }
        if (sb.lastIndexOf(separator) > 0) {
            sb.deleteCharAt(sb.lastIndexOf(separator));
        }
        return (sb.toString());
    }

    /**
     * Returns the complete serial number as human-readable sequence of hex bytes each separated by a colon.
     *
     * @param serialNumber as array of Type byte.
     * @return <b>serialNumberString</b> of type String.
     */
    public static String toString(byte[] serialNumber) {
        return toString(serialNumber, HEXBYTE_SEPARATOR);
    }

    /**
     * Evaluates whether the given serial number is valid.
     *
     * @param serialNumber as array of type {@link byte},
     * @return <b>invalid</B> of type {@link boolean}.
     */
    public static boolean isInvalid(byte[] serialNumber) {
        if (serialNumber.length != 8) {
            return true;
        }
        return ((serialNumber[0] == 0) && (serialNumber[1] == 0) && (serialNumber[2] == 0) && (serialNumber[3] == 0)
                && (serialNumber[4] == 0) && (serialNumber[5] == 0) && (serialNumber[6] == 0)
                && (serialNumber[7] == 0));
    }

    /**
     * Evaluates a given serial number to determine whether any item value should be handled inverted.
     *
     * @param serialNumberString as type {@link String},
     * @return <b>isInverted</B> of type {@link boolean}.
     */
    public static boolean indicatesRevertedValues(String serialNumberString) {
        return (serialNumberString.length() == 0) ? false
                : serialNumberString.charAt(serialNumberString.length() - 1) == SUFFIX_MARKER;
    }

    /**
     * Converts a given serial number into a plain serial number without any inversion markers being left.
     *
     * @param serialNumberString as type {@link String},
     * @return <b>cleanedSerialNumberString</B> of type {@link String}.
     */
    public static String cleaned(String serialNumberString) {
        return indicatesRevertedValues(serialNumberString)
                ? serialNumberString.substring(0, serialNumberString.length() - 1)
                : serialNumberString;
    }
}
