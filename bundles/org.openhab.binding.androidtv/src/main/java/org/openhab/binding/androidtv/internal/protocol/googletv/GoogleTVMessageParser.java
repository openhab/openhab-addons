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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class responsible for parsing incoming GoogleTV messages. Calls back to an object implementing the
 * GoogleTVMessageParserCallbacks interface.
 *
 * Adapted from Lutron Leap binding
 *
 * @author Ben Rosenblum - Initial contribution
 */

@NonNullByDefault
public class GoogleTVMessageParser {
    private final Logger logger = LoggerFactory.getLogger(GoogleTVMessageParser.class);

    private final GoogleTVConnectionManager callback;

    public GoogleTVMessageParser(GoogleTVConnectionManager callback) {
        this.callback = callback;
    }

    public void handleMessage(String msg) {
        if (msg.trim().equals("")) {
            return; // Ignore empty lines
        }

        char[] charArray = msg.toCharArray();
        String lenString = "" + charArray[0] + charArray[1];
        int length = Integer.parseInt(lenString, 16);
        msg = msg.substring(2);

        logger.trace("Received GoogleTV message from: {} - Length: {} Message: {}", callback.getHostName(), length,
                msg);
        // logger.trace("Encoded message: {}", GoogleTVRequest.encodeMessage(msg));

        callback.validMessageReceived();

        try {
            if (msg.startsWith("0a5b08ff041256")) {
                // First message on connection from GTV
                // 0a5b08ff041256 0a 11 534849454c4420416e64726f6964205456 12 06 4e5649444941 18 01 220231312a
                // ------------------LEN-SHIELD Android TV---------------------LEN-NVIDIA
                // 24 636f6d2e676f6f676c652e616e64726f69642e74762e72656d6f74652e73657276696365 32
                // LEN-com.google.android.tv.remote.service
                // 0d 352e322e343733323534313333
                // LEN-5.2.473254133
            } else {
                logger.debug("Unknown payload received. {} {}", length, msg);
            }
        } catch (Exception e) {
            logger.debug("Message Parser Caught Exception", e);
        } finally {
            return;
        }
    }
}
