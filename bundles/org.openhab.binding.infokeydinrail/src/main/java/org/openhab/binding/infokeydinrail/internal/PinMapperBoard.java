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
package org.openhab.binding.infokeydinrail.internal;

import static org.openhab.binding.infokeydinrail.internal.InfokeyBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Themistoklis Anastasopoulos - Initial contribution
 */

public class PinMapperBoard {

    private static final Map<String, Integer> PIN_MAP = new HashMap<>();
    private static final Map<Integer, String> PIN_TO_MAP = new HashMap<>();

    static {
        PIN_MAP.put(CHANNEL_A0, 0);
        PIN_MAP.put(CHANNEL_A1, 1);
        PIN_MAP.put(CHANNEL_A2, 2);
        PIN_MAP.put(CHANNEL_A3, 3);
        PIN_MAP.put(CHANNEL_A4, 4);
        PIN_MAP.put(CHANNEL_A5, 5);
        PIN_MAP.put(CHANNEL_A6, 6);
        PIN_MAP.put(CHANNEL_A7, 7);
        PIN_MAP.put(CHANNEL_B0, 8);
        PIN_MAP.put(CHANNEL_B1, 9);
        PIN_MAP.put(CHANNEL_B2, 10);
        PIN_MAP.put(CHANNEL_B3, 11);
        PIN_MAP.put(CHANNEL_B4, 12);
        PIN_MAP.put(CHANNEL_B5, 13);
        PIN_MAP.put(CHANNEL_B6, 14);
        PIN_MAP.put(CHANNEL_B7, 15);

        PIN_TO_MAP.put(0, "A0");
        PIN_TO_MAP.put(1, "A1");
        PIN_TO_MAP.put(2, "A2");
        PIN_TO_MAP.put(3, "A3");
        PIN_TO_MAP.put(4, "A4");
        PIN_TO_MAP.put(5, "A5");
        PIN_TO_MAP.put(6, "A6");
        PIN_TO_MAP.put(7, "A7");
        PIN_TO_MAP.put(8, "B0");
        PIN_TO_MAP.put(9, "B1");
        PIN_TO_MAP.put(10, "B2");
        PIN_TO_MAP.put(11, "B3");
        PIN_TO_MAP.put(12, "B4");
        PIN_TO_MAP.put(13, "B5");
        PIN_TO_MAP.put(14, "B6");
        PIN_TO_MAP.put(15, "B7");
    }

    public static Integer get(String pinCode) {
        return PIN_MAP.get(pinCode);
    }

    public static String get(Integer pinNo) {
        return PIN_TO_MAP.get(pinNo);
    }
}
