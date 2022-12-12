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
package org.openhab.binding.shieldtv.internal.protocol.shieldtv;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Contains static methods for constructing LEAP messages
 *
 * @author Ben Rosenblum - Initial contribution
 */
@NonNullByDefault
public class ShieldTVRequest {

    public static String encodeMessage(String message) {
        String reply = new String();
        char[] charArray = message.toCharArray();
        for (int i = 0; i < charArray.length; i = i + 2) {
            String st = "" + charArray[i] + "" + charArray[i + 1];
            char ch = (char) Integer.parseInt(st, 16);
            reply = reply + ch;
        }
        return reply;
    }

    public static String decodeMessage(String message) {
        StringBuffer sb = new StringBuffer();
        char ch[] = message.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            String hexString = Integer.toHexString(ch[i]);
            sb.append(hexString);
        }
        String reply = sb.toString();
        return reply;
    }
}
