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

        callback.validMessageReceived();

        try {
            if (msg.startsWith("0a")) {
                // First message on connection from GTV
                //
                // 0a5b08ff041256 0a 11 534849454c4420416e64726f6964205456 12 06 4e5649444941 18 01 22 02 3131 2a
                // ------------------LEN-SHIELD Android TV--------------------LEN-NVIDIA---------LEN---LEN-Android
                // Version
                // 24 636f6d2e676f6f676c652e616e64726f69642e74762e72656d6f74652e73657276696365 32
                // LEN-com.google.android.tv.remote.service
                // 0d 352e322e343733323534313333
                // LEN-5.2.473254133
                //
                // 0a5308ff04124e 0a 0c 42524156494120344b204742 12 04 536f6e79 18 01 22 01 39 2a
                // ------------------LEN-BRAVIA 4K GB---------------LEN-Sony-------LEN---LEN-Android Version
                // 24 636f6d2e676f6f676c652e616e64726f69642e74762e72656d6f74652e73657276696365 32
                // 0d 352e322e343733323534313333
                //
                // 0a5408ff04124f 0a 0a 4368726f6d6563617374 12 06 476f6f676c65 18 01 22 02 3132 2a
                // ------------------LEN-Chromecast-------------LEN-Google---------LEN---LEN-Android Version
                // 24 636f6d2e676f6f676c652e616e64726f69642e74762e72656d6f74652e73657276696365 32
                // 0d 352e322e343733323534313333
                //
                // 0a5708ff041252 0a 0d 4368726f6d6563617374204844 12 06 476f6f676c65 18 01 22 02 3132 2a
                // ------------------LEN-Chromecast HD----------------LEN-Google---------LEN---LEN-Android Version
                // 24 636f6d2e676f6f676c652e616e64726f69642e74762e72656d6f74652e73657276696365 32
                // 0d352e322e343733323534313333

                callback.sendCommand(
                        new GoogleTVCommand(GoogleTVRequest.encodeMessage(GoogleTVRequest.loginRequest(4))));

                StringBuffer manufacturer = new StringBuffer();
                StringBuffer model = new StringBuffer();

            } else if (msg.startsWith("1200")) {
                // Second message on connection from GTV
                // Login successful
                callback.sendCommand(
                        new GoogleTVCommand(GoogleTVRequest.encodeMessage(GoogleTVRequest.loginRequest(5))));
                callback.setLoggedIn(true);
            } else if (msg.startsWith("9203")) {
                // Third message on connection from GTV
                // Also sent on power state change (to ON only unless keypress triggers)
                // 9203 2108 0210 02 1a 11 534849454c4420416e64726f6964205456 20 02 2800 30 0f38 0e 40 00
                // ---------------------LEN-SHIELD Android TV
                // 9203 1e08 9610 10 09 1a 0d 4368726f6d6563617374204844 20 02 2800 30 1938 0a 40 00
                // ------------------------LEN-Chromecast HD
                // 9203 1a08 f304 10 09 1a 11 534849454c4420416e64726f6964205456 20 01
                // 9203 1a08 8205 10 09 1a 11 534849454c4420416e64726f6964205456 20 01
                // ------------------------LEN-SHIELD Android TV
                //
                // VOLUME:
                // ----------------------------LEN-BRAVIA 4K GB-------------------------------VOL---MUTE
                // 00 --- 9203 1c08 0310 06 1a 0c 42524156494120344b204742 20 02 2800 30 6438 00 40 00
                // 01 --- 9203 1c08 0310 06 1a 0c 42524156494120344b204742 20 02 2800 30 6438 01 40 00
                // 100 -- 9203 1c08 0310 06 1a 0c 42524156494120344b204742 20 02 2800 30 6438 64 40 00
                // MUTE - 9203 1c08 0310 06 1a 0c 42524156494120344b204742 20 02 2800 30 6438 00 40 01
            } else if (msg.startsWith("0802")) {
                // PIN Process Messages. Only used on 6467.
                if (msg.startsWith("080210c801ca02")) {
                    // PIN Process Successful
                    logger.trace("PIN Process Successful!");
                    callback.finishPinProcess();
                } else {
                    // 080210c801a201081204080310061801
                    // 080210c801fa0100
                }
            } else if (msg.startsWith("c202")) {
                // Power State
                // c202020800 - OFF
                // c202020801 - ON
                if (msg.equals("c202020800")) {
                    callback.setPower(false);
                } else if (msg.equals("c202020801")) {
                    callback.setPower(true);
                }
            } else if (msg.startsWith("42")) {
                // Keepalive request
                callback.sendKeepAlive(msg);
            } else if (msg.startsWith("a201")) {
                // Current app name. Sent on keypress and power change.
                // a201 210a1f62 1d 636f6d2e676f6f676c652e616e64726f69642e796f75747562652e7476
                // --------------LEN-com.google.android.youtube.tv
                // a201 210a1f62 1d 636f6d2e676f6f676c652e616e64726f69642e74766c61756e63686572
                // --------------LEN-com.google.android.tvlauncher
                // a201 140a1262 10 636f6d2e736f6e792e6474762e747678
                // --------------LEN-com.sony.dtv.tvx
                // a201 150a1362 11 636f6d2e6e6574666c69782e6e696e6a61
                // --------------LEN-com.netflix.ninja

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
