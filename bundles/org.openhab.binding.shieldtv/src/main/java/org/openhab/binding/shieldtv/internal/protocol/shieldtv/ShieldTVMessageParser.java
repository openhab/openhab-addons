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
        logger.trace("Encoded message: {}", ShieldTVRequest.encodeMessage(msg));

        char[] charArray = msg.toCharArray();

        if (msg.startsWith("080a12")) {
            // Hostname of Shield Replied
            // 080a12 1d08e80712 18080112 10 5b534849454c445d2054686561746572 18d7fd04180a
            // 080a12 2208e80712 1d08e80712 14 5b534849454c445d204c6976696e6720526f6f6d 18d7fd04180a
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
            int length = Integer.parseInt(st, 16);
            int current = i;
            for (; i < current + length; i = i + 2) {
                st = "" + charArray[i] + "" + charArray[i + 1];
                hostname.append(st);
            }
            logger.trace("Shield Hostname: {}", hostname);
            logger.trace("Shield Hostname Encoded: {}", ShieldTVRequest.encodeMessage(hostname.toString()));

        }

        return;
    }
}
