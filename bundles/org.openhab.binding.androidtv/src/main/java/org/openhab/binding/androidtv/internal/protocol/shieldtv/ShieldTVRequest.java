/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.androidtv.internal.protocol.shieldtv;

import static org.openhab.binding.androidtv.internal.AndroidTVBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Contains static methods for constructing LEAP messages
 *
 * @author Ben Rosenblum - Initial contribution
 */
@NonNullByDefault
public class ShieldTVRequest {

    public static String encodeMessage(String message) {
        StringBuilder reply = new StringBuilder();
        char[] charArray = message.toCharArray();
        for (int i = 0; i < charArray.length; i = i + 2) {
            String st = "" + charArray[i] + "" + charArray[i + 1];
            char ch = (char) Integer.parseInt(st, 16);
            reply.append(ch);
        }
        return reply.toString();
    }

    public static String decodeMessage(String message) {
        StringBuilder sb = new StringBuilder();
        char ch[] = message.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            String hexString = Integer.toHexString(ch[i]);
            if (hexString.length() % 2 > 0) {
                sb.append('0');
            }
            sb.append(hexString);
        }
        return sb.toString();
    }

    public static String pinRequest(String pin) {
        if (PIN_REQUEST.equals(pin)) {
            String message = "080a120308cd08";
            return message;
        } else {
            String prefix = "080a121f08d108121a0a06";
            String encodedPin = decodeMessage(pin);
            String suffix = "121036646564646461326639366635646261";
            return prefix + encodedPin + suffix;
        }
    }

    public static String loginRequest() {
        String message = "0801121a0801121073616d73756e6720534d2d4739393855180128fbff04";
        return message;
    }

    public static String keepAlive() {
        String message = "080028fae0a6c0d130";
        return message;
    }

    private static String fixMessage(String tempMsg) {
        if (tempMsg.length() % 2 > 0) {
            tempMsg = "0" + tempMsg;
        }
        return tempMsg;
    }

    public static String startApp(String message) {
        int length = message.length();
        String len1 = fixMessage(Integer.toHexString(length + 6));
        String len2 = fixMessage(Integer.toHexString(length + 2));
        String len3 = fixMessage(Integer.toHexString(length));
        String reply = "08f10712" + len1 + "080212" + len2 + "0a" + len3 + decodeMessage(message);
        return reply;
    }
    // 080b120308cd08 - Longer Hostname Reply
    // 08f30712020805 - Unknown
    // 08f10712020800 - Get all apps
    // 08ec0712020806 - Get current app

    public static String keyboardEntry(String entry) {
        // 08ec07120d08081205616263646532020a0a
        // 08ec0712 0d 0808 12 05 6162636465 3202 0a0a
        int length = entry.length();
        String len1 = fixMessage(Integer.toHexString(length + 8));
        String len2 = fixMessage(Integer.toHexString(length));
        String len3 = fixMessage(Integer.toHexString(length * 2));
        String reply = "08ec0712" + len1 + "080812" + len2 + decodeMessage(entry) + "3202" + len3 + len3;
        return reply;
    }
}
