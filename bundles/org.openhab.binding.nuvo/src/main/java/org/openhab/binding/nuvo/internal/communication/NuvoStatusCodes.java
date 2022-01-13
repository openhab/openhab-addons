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
package org.openhab.binding.nuvo.internal.communication;

import java.util.HashMap;
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
    public static final Map<String, String> PLAY_MODE = new HashMap<>();
    static {
        PLAY_MODE.put("0", "Normal");
        PLAY_MODE.put("1", "Idle");
        PLAY_MODE.put("2", "Playing");
        PLAY_MODE.put("3", "Paused");
        PLAY_MODE.put("4", "Fast Forward");
        PLAY_MODE.put("5", "Rewind");
        PLAY_MODE.put("6", "Play Shuffle");
        PLAY_MODE.put("7", "Play Repeat");
        PLAY_MODE.put("8", "Play Shuffle Repeat");
        PLAY_MODE.put("9", "unknown-9");
        PLAY_MODE.put("10", "unknown-10");
        PLAY_MODE.put("11", "Radio"); // undocumented
        PLAY_MODE.put("12", "unknown-12");
    }

    // map to lookup button action name from NuvoNet button code
    public static final Map<String, String> BUTTON_CODE = new HashMap<>();
    static {
        BUTTON_CODE.put("1", "OK");
        BUTTON_CODE.put("2", "PLAYPAUSE");
        BUTTON_CODE.put("3", "PREV");
        BUTTON_CODE.put("4", "NEXT");
        BUTTON_CODE.put("5", "POWERMUTE"); // source will not receive this
        BUTTON_CODE.put("6", "UP"); // source will not receive this
        BUTTON_CODE.put("7", "DOWN"); // source will not receive this
        BUTTON_CODE.put("41", "DISCRETEPLAYPAUSE");
        BUTTON_CODE.put("42", "DISCRETENEXTTRACK");
        BUTTON_CODE.put("43", "DISCRETEPREVIOUSTRACK");
        BUTTON_CODE.put("44", "SHUFFLETOGGLE");
        BUTTON_CODE.put("45", "REPEATTOGGLE");
        BUTTON_CODE.put("46", "TUNEUP");
        BUTTON_CODE.put("47", "TUNEDOWN");
        BUTTON_CODE.put("48", "SEEKUP");
        BUTTON_CODE.put("49", "SEEKDOWN");
        BUTTON_CODE.put("50", "PRESETUP");
        BUTTON_CODE.put("51", "PRESETDOWN");
        BUTTON_CODE.put("52", "DIRECTFREQUENCYENTRY");
        BUTTON_CODE.put("53", "DIRECTPRESETENTRY");
        BUTTON_CODE.put("54", "NEXTBAND");
        BUTTON_CODE.put("55", "THUMBSUP");
        BUTTON_CODE.put("56", "THUMBSDOWN");
    }

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
