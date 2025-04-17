/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.linky.internal.helpers;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.linky.internal.types.LinkyTicMode;

/**
 * The {@link LinkyFrameUtil} class defines a utility class for
 * {@link org.openhab.binding.linky.internal.data.FrameType#CBETM_LONG_BASE}.
 *
 * @author Nicolas SIBERIL - Initial contribution
 * @author Laurent Arnal - Refactor to integrate into Linky Binding
 */
@NonNullByDefault
public class LinkyFrameUtil {

    private LinkyFrameUtil() {
        // private constructor (utility class)
    }

    /**
     * Compute the checksum of the given group line.
     *
     * @param groupLine group line {@literal ("etiquette" <SPACE> "valeur")}.
     *            Note: the SPACE before the checksum of the group line
     *            must not include in checksum computation.
     * @return the checksum of the given group line.
     */
    public static char computeGroupLineChecksum(final String groupLine, LinkyTicMode ticMode) {
        int sum = 0;
        for (int i = 0; i < groupLine.length(); i++) {
            sum += groupLine.codePointAt(i);
        }
        if (ticMode == LinkyTicMode.STANDARD) {
            sum += 0x09;
        }
        sum = (sum & 0x3F) + 0x20;
        return (char) sum;
    }

    /**
     * Parse relais states.
     *
     * @param relais integer string
     * @return State of each relais
     */
    public static boolean[] parseRelaisStates(String relais) {
        boolean[] relaisState = new boolean[8];
        int value = Integer.parseInt(relais);
        for (int i = 0; i <= 7; i++) {
            relaisState[i] = (value & 1) == 1;
            value >>= 1;
        }
        return relaisState;
    }
}
