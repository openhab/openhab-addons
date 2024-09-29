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
package org.openhab.binding.alarmdecoder.internal.protocol;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The various message types that come from the ad2usb/ad2pi interface
 *
 * @author Bernd Pfrommer - Initial contribution (OH1)
 * @author Bob Adair - Re-factored and removed methods unused in OH2 binding
 */
@NonNullByDefault
public enum ADMsgType {
    EXP, // zone expander message
    KPM, // keypad message
    LRR, // long range radio message
    REL, // relay message
    RFX, // wireless message
    VER, // version message
    INVALID; // invalid message

    /** hash map from protocol message heading to type */
    private static Map<String, ADMsgType> startToMsgType = new HashMap<>();

    static {
        startToMsgType.put("!REL", ADMsgType.REL);
        startToMsgType.put("!SER", ADMsgType.INVALID);
        startToMsgType.put("!RFX", ADMsgType.RFX);
        startToMsgType.put("!EXP", ADMsgType.EXP);
        startToMsgType.put("!LRR", ADMsgType.LRR);
        startToMsgType.put("!VER", ADMsgType.VER);
        startToMsgType.put("!KPM", ADMsgType.KPM);
    }

    /**
     * Extract message type from message. Relies on static map startToMsgType.
     *
     * @param s message string
     * @return message type
     */
    public static ADMsgType getMsgType(@Nullable String s) {
        if (s == null || s.length() < 4) {
            return ADMsgType.INVALID;
        }
        if (s.startsWith("[")) {
            return ADMsgType.KPM;
        }
        ADMsgType mt = startToMsgType.get(s.substring(0, 4));
        if (mt == null) {
            mt = ADMsgType.INVALID;
        }
        return mt;
    }
}
