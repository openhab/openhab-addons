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
package org.openhab.binding.androidtv.internal.protocol.googletv;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Contains static methods for constructing LEAP messages
 *
 * @author Ben Rosenblum - Initial contribution
 */
@NonNullByDefault
public class GoogleTVRequest {

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
            if (hexString.length() % 2 > 0) {
                sb.append('0');
            }
            sb.append(hexString);
        }
        String reply = sb.toString();
        return reply;
    }

    public static String pinRequest(String pin) {
        // OLD
        if (pin.equals("REQUEST")) {
            return loginRequest(3);
        } else {
            // 080210c801c202 22 0a 20 0e066c3d1c3a6686edb6b2648ff25fcb3f0bf9cc81deeee9fad1a26073645e17
            // 080210c801c202 22 0a 20 530bb7c7ba06069997285aff6e0106adfb19ab23c18a7422f5f643b35a6467b3
            // -------------------------SHA HASH OF PIN

            int length = pin.length() / 2;
            String len1 = GoogleTVRequest.fixMessage(Integer.toHexString(length + 2));
            String len2 = GoogleTVRequest.fixMessage(Integer.toHexString(length));
            String reply = "080210c801c202" + len1 + "0a" + len2 + pin;
            return reply;
        }
    }

    public static String loginRequest(int messageId) {
        String message = "";
        if (messageId == 1) {
            // Send app and device name
            // 080210c801522d 0a 19 636f6d2e676f6f676c652e616e64726f69642e766964656f73 12 10
            // 73616d73756e6720534d2d4739393855
            // ------------------LEN com.google.android.videos----------------------------LEN samsung SM-G998U
            message = "080210c801522d0a19636f6d2e676f6f676c652e616e64726f69642e766964656f73121073616d73756e6720534d2d4739393855";
        } else if (messageId == 2) {
            // Unknown but required
            // 080210c801a201 0e 0a 04 08031006 0a 04 08031004 1802
            // ---------------LEN---LEN------------LEN
            message = "080210c801a2010e0a04080310060a04080310041802";
        } else if (messageId == 3) {
            // Trigger PIN OSD
            // ---------------LEN---LEN
            // 080210c801a201 08 12 04 08031006 1801
            // 080210c801f201 08 0a 04 08031006 1001
            message = "080210c801f201080a04080310061001";
        } else if (messageId == 4) {
            // 0a41087e123d0a 08 534d2d4739393855 12 07 73616d73756e67 18 01 22 02 3133 2a
            // ---------------LEN--SM-G998U----------LEN--samsung---------
            // 19 636f6d2e676f6f676c652e616e64726f69642e766964656f73 32 07 342e33382e3138
            // LEN-com.google.android.videos----------------------------LEN-4.38.18
            message = "0a41087e123d0a08534d2d4739393855120773616d73756e671801220231332a19636f6d2e676f6f676c652e616e64726f69642e766964656f733207342e33382e3138";
        } else if (messageId == 5) {
            // Unknown. Sent after "1200" received
            message = "1202087e";
        }
        return message;
    }

    public static String keepAlive(String request) {
        // 42 07 08 01 10 e4f1 8d01
        // 4a 02 08 01

        // 42 08 08 7f 10 b4 908a a819
        // 4a 02 08 7f

        // 42 09 08 8001 10 ed b78a a819
        // 4a 03 08 8001

        char[] charArray = request.toCharArray();
        StringBuffer sb = new StringBuffer();
        sb.append(request);
        sb.setLength(sb.toString().length() - 6);
        String st = "";
        do {
            int sbLen = sb.toString().length();
            st = "" + charArray[sbLen - 2] + charArray[sbLen - 1];
            if (!st.equals("10")) {
                sb.setLength(sbLen - 2);
            }
        } while (!st.equals("10"));
        sb.setLength(sb.toString().length() - 2);

        StringBuffer sbReply = new StringBuffer();
        for (int i = 4; i < sb.toString().length(); i++) {
            sbReply.append(charArray[i]);
        }
        String reply = "4a" + fixMessage(Integer.toHexString(sbReply.toString().length() / 2)) + sbReply.toString();
        return reply;
    }

    public static String fixMessage(String tempMsg) {
        if (tempMsg.length() % 2 > 0) {
            tempMsg = "0" + tempMsg;
        }
        return tempMsg;
    }
}
