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
package org.openhab.binding.nuvo.internal.communication;

import static java.util.Map.entry;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Provides mapping of various Nuvo status codes to plain language meanings
 *
 * @author Michael Lobstein - Initial contribution
 */

@NonNullByDefault
public class NuvoStatusCodes {
    private static final String L = "L";
    private static final String C = "C";
    private static final String R = "R";
    private static final String DASH = "-";
    private static final String ZERO = "0";

    // map to lookup play mode
    public static final Map<String, String> PLAY_MODE = Map.ofEntries(entry("0", "Normal"), entry("1", "Idle"),
            entry("2", "Playing"), entry("3", "Paused"), entry("4", "Fast Forward"), entry("5", "Rewind"),
            entry("6", "Play Shuffle"), entry("7", "Play Repeat"), entry("8", "Play Shuffle Repeat"),
            entry("9", "Step Tune"), entry("10", "Seek Tune"), entry("11", "Preset Tune"), entry("12", "unknown-12"));

    // map to lookup button action name from NuvoNet button code
    public static final Map<String, String> BUTTON_CODE = Map.ofEntries(entry("1", "OK"), entry("2", "PLAYPAUSE"),
            entry("3", "PREV"), entry("4", "NEXT"), entry("5", "POWERMUTE"), // source will not receive this
            entry("6", "UP"), // source will not receive this
            entry("7", "DOWN"), // source will not receive this
            entry("41", "DISCRETEPLAYPAUSE"), entry("42", "DISCRETENEXTTRACK"), entry("43", "DISCRETEPREVIOUSTRACK"),
            entry("44", "SHUFFLETOGGLE"), entry("45", "REPEATTOGGLE"), entry("46", "TUNEUP"), entry("47", "TUNEDOWN"),
            entry("48", "SEEKUP"), entry("49", "SEEKDOWN"), entry("50", "PRESETUP"), entry("51", "PRESETDOWN"),
            entry("52", "DIRECTFREQUENCYENTRY"), entry("53", "DIRECTPRESETENTRY"), entry("54", "NEXTBAND"),
            entry("55", "THUMBSUP"), entry("56", "THUMBSDOWN"));

    /*
     * This looks broken because the controller is seriously broken...
     * On the keypad when adjusting the balance to "Left 18", the serial data reports R18 ¯\_(ツ)_/¯
     * So on top of the weird translation, the value needs to be reversed by the binding
     * to ensure that it will match what is displayed on the keypad.
     * For display purposes we want -18 to be full left, 0 = center, and +18 to be full right
     */
    public static String getBalanceFromStr(String value) {
        // example L2; return 2 | C; return 0 | R10; return -10
        if (value.substring(0, 1).equals(L)) {
            return (value.substring(1));
        } else if (value.equals(C)) {
            return ZERO;
        } else if (value.substring(0, 1).equals(R)) {
            return (DASH + value.substring(1));
        }
        return ZERO;
    }

    // see above comment
    public static String getBalanceFromInt(Integer value) {
        if (value < 0) {
            return (L + Math.abs(value));
        } else if (value == 0) {
            return C;
        } else if (value > 0) {
            return (R + value);
        }
        return C;
    }
}
