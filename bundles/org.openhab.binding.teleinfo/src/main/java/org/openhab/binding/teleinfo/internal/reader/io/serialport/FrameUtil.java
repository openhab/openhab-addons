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
package org.openhab.binding.teleinfo.internal.reader.io.serialport;

/**
 * The {@link FrameUtil} class defines a utility class for {@link FrameCbetmLong}.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
public class FrameUtil {

    private FrameUtil() {
        // private constructor (utility class)
    }

    /**
     * Compute the checksum of the given group line.
     *
     * @param groupLine group line ("etiquette" <SPACE> "valeur"). Note: the SPACE before the checksum of the group line
     *                      must not include in checksum computation.
     * @return the checksum of the given group line.
     */
    public static char computeGroupLineChecksum(final String label, final String value) {
        final String groupLine = label + " " + value;
        int sum = 0;
        for (int i = 0; i < groupLine.length(); i++) {
            sum = sum + groupLine.codePointAt(i);
        }
        sum = (sum & 0x3F) + 0x20;

        return (char) sum;
    }
}
