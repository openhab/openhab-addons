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
        String decodedMessage;
        if (msg.trim().equals("")) {
            return; // Ignore empty lines
        }

        logger.trace("Received message: {}", msg);

        decodedMessage = ShieldTVRequest.decodeMessage(msg);

        logger.trace("Decoded message: {}", decodedMessage);

        return;
    }
}
