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

import static org.openhab.binding.androidtv.internal.AndroidTVBindingConstants.*;
import static org.openhab.binding.androidtv.internal.protocol.googletv.GoogleTVConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Contains static methods for constructing LEAP messages
 *
 * @author Ben Rosenblum - Initial contribution
 */
@NonNullByDefault
public class GoogleTVRequest {

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
        // OLD
        if (PIN_REQUEST.equals(pin)) {
            return loginRequest(3);
        } else {
            // 080210c801c202 22 0a 20 0e066c3d1c3a6686edb6b2648ff25fcb3f0bf9cc81deeee9fad1a26073645e17
            // 080210c801c202 22 0a 20 530bb7c7ba06069997285aff6e0106adfb19ab23c18a7422f5f643b35a6467b3
            // -------------------------SHA HASH OF PIN

            int length = pin.length() / 2;
            String len1 = GoogleTVRequest.fixMessage(Integer.toHexString(length + 2));
            String len2 = GoogleTVRequest.fixMessage(Integer.toHexString(length));
            return "080210c801c202" + len1 + "0a" + len2 + pin;
        }
    }

    public static String loginRequest(int messageId) {
        return loginRequest(messageId, "");
    }

    public static String loginRequest(int messageId, String data) {
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
            // 0a 41 08 7e 12 3d 0a 08 534d2d4739393855 12 07 73616d73756e67 18 01 22 02 3133 2a
            // ---LEN---------LEN---LEN--SM-G998U----------LEN--samsung---------
            // 19 636f6d2e676f6f676c652e616e64726f69642e766964656f73 32
            // LEN-com.google.android.videos----------------------------
            // 07 342e33382e3138
            // LEN-4.38.18
            // message =
            // "0a41087e123d0a08534d2d4739393855120773616d73756e671801220231332a19636f6d2e676f6f676c652e616e64726f69642e766964656f733207342e33382e3138";

            // 0a 57 08 fe 04 12 52 0a 08 534d2d4739393855 12 07 73616d73756e67 18 01 22 02 3133 2a
            // ---LEN------------LEN---LEN--SM-G998U----------LEN--samsung---------
            // 19 636f6d2e676f6f676c652e616e64726f69642e766964656f73 32
            // LEN-com.google.android.videos---------------------------
            // 1c 342e33392e3538342e3532393538383538332e372d72656c65617365
            // LEN-4.39.584.529588583.7-release
            // message =
            // "0a5708fe0412520a08534d2d4739393855120773616d73756e671801220231332a19636f6d2e676f6f676c652e616e64726f69642e766964656f73321c342e33392e3538342e3532393538383538332e372d72656c65617365";

            // 0a 3b 08 ee 04 12 36 0a 06 6950686f6e65 12 05 4170706c65 18 02 22 04 31362e36 2a
            // ---LEN------------LEN---LEN-iPhone---------LEN-Apple--------------LEN-16.6
            // 11 636f6d2e676f6f676c652e4d6f76696573 32 0a 332e31332e3030303033
            // LEN-com.google.Movies--------------------LEN-3.13.00003

            // 0a 57 08 fe 04 12 52 0a 08 534d2d4739393855 12 07 73616d73756e67 18 01 22 02 3133 2a
            // ---LEN------------LEN---LEN--SM-G998U----------LEN--samsung---------
            // 19 636f6d2e676f-6f676c652e616e64726f69642e766964656f73 32
            // LEN-com.google.android.videos---------------------------
            // 1c 342e33392e3634342e3533343836353739392e332d72656c65617365
            // LEN-4.39.644.534865799.3-release
            int dataInt = Integer.parseInt(data, 16) - 1;
            String updatedData = Integer.toHexString(dataInt);
            message = "0a5708" + updatedData
                    + "0412520a08534d2d4739393855120773616d73756e671801220231332a19636f6d2e676f6f676c652e616e64726f69642e766964656f73321c342e33392e3634342e3533343836353739392e332d72656c65617365";
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
        StringBuilder sb = new StringBuilder();
        sb.append(request);
        sb.setLength(sb.toString().length() - 6);
        String st = "";
        do {
            int sbLen = sb.toString().length();
            st = "" + charArray[sbLen - 2] + charArray[sbLen - 1];
            if (!DELIMITER_10.equals(st)) {
                sb.setLength(sbLen - 2);
            }
        } while (!DELIMITER_10.equals(st));
        sb.setLength(sb.toString().length() - 2);

        StringBuilder sbReply = new StringBuilder();
        for (int i = 4; i < sb.toString().length(); i++) {
            sbReply.append(charArray[i]);
        }
        return "4a" + fixMessage(Integer.toHexString(sbReply.toString().length() / 2)) + sbReply.toString();
    }

    public static String fixMessage(String tempMsg) {
        if (tempMsg.length() % 2 > 0) {
            tempMsg = "0" + tempMsg;
        }
        return tempMsg;
    }
}
