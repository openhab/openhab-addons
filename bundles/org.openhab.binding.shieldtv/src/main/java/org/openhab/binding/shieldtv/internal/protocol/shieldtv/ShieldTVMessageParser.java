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

import java.util.Base64;

import javax.xml.bind.DatatypeConverter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class responsible for parsing incoming ShieldTV messages. Calls back to an object implementing the
 * ShieldTVMessageParserCallbacks interface.
 *
 * Adapted from Lutron Leap binding
 *
 * @author Ben Rosenblum - Initial contribution
 */

@NonNullByDefault
public class ShieldTVMessageParser {
    private final Logger logger = LoggerFactory.getLogger(ShieldTVMessageParser.class);

    public ShieldTVMessageParser(ShieldTVMessageParserCallbacks callback) {
        return;
    }

    public void handleMessage(String msg) {
        if (msg.trim().equals("")) {
            return; // Ignore empty lines
        }

        logger.trace("Received message: {}", msg);
        // logger.trace("Encoded message: {}", ShieldTVRequest.encodeMessage(msg));

        char[] charArray = msg.toCharArray();

        /*
         * MOST payloads start with 080a and end in 180a
         * Some seem to use 12 as a delimiter of some kind
         * There are some random payloads that don't follow any rules: e.g. "20a610" sends when the PIN is approved.
         */

        if (msg.startsWith("080a12") && msg.startsWith("08e807", 8)) {
            // Hostname of Shield Replied
            // 080a12 1d08e80712 18080112 10 5b534849454c445d2054686561746572 18d7fd04 180a
            // 080a12 2208e80712 1d08e80712 14 5b534849454c445d204c6976696e6720526f6f6d 18d7fd04 180a
            // Each chunk ends in 12
            // 4th chunk (10 and 14 above) represent length of the name.
            // 5th chunk is the name
            int chunk = 0;
            int i = 0;
            String st = "";
            StringBuffer hostname = new StringBuffer();
            while (chunk < 3) {
                st = "" + charArray[i] + "" + charArray[i + 1];
                if (st.equals("12")) {
                    chunk++;
                }
                i += 2;
            }
            st = "" + charArray[i] + "" + charArray[i + 1];
            i += 2;
            int length = Integer.parseInt(st, 16) * 2;
            int current = i;
            for (; i < current + length; i = i + 2) {
                st = "" + charArray[i] + "" + charArray[i + 1];
                hostname.append(st);
            }
            logger.trace("Shield Hostname: {} {}", hostname, length);
            logger.trace("Shield Hostname Encoded: {}", ShieldTVRequest.encodeMessage(hostname.toString()));

        } else if (msg.startsWith("080b12")) {
            // Longer hostname reply
            logger.trace("Longer Hostname Reply");
        } else if (msg.startsWith("080a12") && msg.startsWith("0308cf08", 6)) {
            logger.trace("PIN Process Started");
        } else if (msg.startsWith("20") && msg.startsWith("10", 4) && msg.length() == 6) {
            // This seems to be 20**10 when observed.
            // This seems to send immediately before the certificate reply and as a reply to the pin being sent
            logger.trace("PIN Process Successful");
        } else if (msg.startsWith("08f007")) {
            // Login successful???
            // This seems to happen after a successful PIN/Cert as well as on login with a valid cert
            // Maybe this should be what we use to set the shield online?
            logger.trace("Login Successful");
        } else if (msg.equals("080a121108b510120c0804120854696d65206f7574180a")) {
            // Timeout
            logger.trace("Timeout");
        } else if (msg.startsWith("08ec07")) {
            // Current App
            // 08ec07122a080722262205656e5f5553421d636f6d2e676f6f676c652e616e64726f69642e74766c61756e6368657218ec07
            //
        } else if (msg.startsWith("080a12") && msg.startsWith("1008b510", 8)) {
            // Certificate Reply
            // |--6---------12----------10--------------16---------6--- = 50 characters long
            // |080a12 ad1008b51012 a710080112 07 53756363657373 1ac009 3082... 3082... 180a
            // |080a12 9f1008b51012 9910080112 07 53756363657373 1ac209 3082... 3082... 180a
            // |-------------------------------Length of SUCCESS
            // |----------------------------------ASCII: SUCCESS
            // |--------------------------------------------------------Priv Key RSA 2048
            // |----------------------------------------------------------------Cert X.509
            StringBuffer preamble = new StringBuffer();
            StringBuffer privKey = new StringBuffer();
            StringBuffer pubKey = new StringBuffer();
            String privLenSt = "" + charArray[6] + "" + charArray[7];
            int privLen = 2400;
            for (int i = 0; i < 50; i++) {
                preamble.append(charArray[i]);
            }
            for (int i = 50; i < privLen; i++) {
                privKey.append(charArray[i]);
                if ((i + 1) == privLen) {
                    String checkForPub = "" + charArray[i + 1] + "" + charArray[i + 2] + "" + charArray[i + 3] + ""
                            + charArray[i + 4];
                    if (!checkForPub.equals("3082")) {
                        privLen += 2;
                    }
                }
            }
            for (int i = privLen; i < msg.length() - 4; i++) {
                pubKey.append(charArray[i]);
            }
            logger.trace("Cert Preamble:   {}", preamble.toString());
            logger.trace("Cert privKey: {} {}", privLen, privKey.toString());
            logger.trace("Cert pubKey:  {} {}", msg.length() - privLen - 4, pubKey.toString());

            byte[] privKeyB64Byte = DatatypeConverter.parseHexBinary(privKey.toString());
            byte[] pubKeyB64Byte = DatatypeConverter.parseHexBinary(pubKey.toString());

            String privKeyB64 = Base64.getEncoder().encodeToString(privKeyB64Byte);
            String pubKeyB64 = Base64.getEncoder().encodeToString(pubKeyB64Byte);

            logger.trace("Cert privKey: {}", privKeyB64);
            logger.trace("Cert pubKey:  {}", pubKeyB64);

        } else {
            logger.trace("Unknown payload received. {}", msg);
        }
        return;
    }
}
