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
    
    // map to lookup play mode
    public static final Map<String, String> playMode = new HashMap<>();
    static {
        playMode.put("0", "Normal");
        playMode.put("1", "Idle");
        playMode.put("2", "Playing");
        playMode.put("3", "Paused");
        playMode.put("4", "Fast Forward");
        playMode.put("5", "Rewind");
        playMode.put("6", "Play Shuffle");
        playMode.put("7", "Play Repeat");
        playMode.put("8", "Play Shuffle Repeat");
        playMode.put("9", "unknown-9");
        playMode.put("10", "unknown-10");
        playMode.put("11", "Radio"); // undocumented
        playMode.put("12", "unknown-12");
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
        if (value.substring(0, 1).equals("L")) {
            return (value.substring(1));
        } else if (value.equals("C")) {
            return "0";
        } else if (value.substring(0, 1).equals("R")) {
            return ("-" + value.substring(1));
        } else {
            return "0";
        }
    }
    
    // see above comment
    public static String getBalanceFromInt(Integer value) {
        if (value < 0) {
            return ("L" + Integer.toString(value * -1));
        } else if (value == 0) {
            return "C";
        } else if (value > 0) {
            return ("R" + Integer.toString(value));
        } else {
            return "C";
        }
    }

}
