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

import static org.openhab.binding.androidtv.internal.protocol.googletv.GoogleTVConstants.*;

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
        if (msg.trim().isEmpty()) {
            return; // Ignore empty lines
        }

        String thingId = callback.getThingID();
        char[] charArray = msg.toCharArray();
        String lenString = "" + charArray[0] + charArray[1];
        int len = Integer.parseInt(lenString, 16);
        msg = msg.substring(2);
        charArray = msg.toCharArray();

        logger.trace("{} - Received GoogleTV message - Length: {} Message: {}", thingId, len, msg);

        callback.validMessageReceived();

        try {
            if (msg.startsWith(DELIMITER_1A)) {
                logger.warn("{} - GoogleTV Error Message: {}", thingId, msg);
                callback.getHandler().dispose();
            } else if (msg.startsWith(DELIMITER_0A)) {
                // First message on connection from GTV
                //
                // 0a 5b08 ff 041256 0a 11 534849454c4420416e64726f6964205456 12 06 4e5649444941 18 01 22 02 3131 2a
                // ---------------------LEN-SHIELD Android TV--------------------LEN-NVIDIA---------LEN---LEN-Android
                // 24 636f6d2e676f6f676c652e616e64726f69642e74762e72656d6f74652e73657276696365 32
                // LEN-com.google.android.tv.remote.service
                // 0d 352e322e343733323534313333
                // LEN-5.2.473254133
                //
                // 0a 5308 ff 04124e 0a 0c 42524156494120344b204742 12 04 536f6e79 18 01 22 01 39 2a
                // ---------------------LEN-BRAVIA 4K GB---------------LEN-Sony-------LEN---LEN-Android Version
                // 24 636f6d2e676f6f676c652e616e64726f69642e74762e72656d6f74652e73657276696365 32
                // 0d 352e322e343733323534313333
                //
                // 0a 5408 ff 04124f 0a 0a 4368726f6d6563617374 12 06 476f6f676c65 18 01 22 02 3132 2a
                // ---------------------LEN-Chromecast-------------LEN-Google---------LEN---LEN-Android Version
                // 24 636f6d2e676f6f676c652e616e64726f69642e74762e72656d6f74652e73657276696365 32
                // 0d 352e322e343733323534313333
                //
                // 0a 5708 ff 041252 0a 0d 4368726f6d6563617374204844 12 06 476f6f676c65 18 01 22 02 3132 2a
                // ---------------------LEN-Chromecast HD----------------LEN-Google---------LEN---LEN-Android Version
                // 24 636f6d2e676f6f676c652e616e64726f69642e74762e72656d6f74652e73657276696365 32
                // 0d352e322e343733323534313333

                if (callback.getLoggedIn()) {
                    logger.warn("{} - Unexpected Login Message: {}", thingId, msg);
                } else {
                    callback.sendCommand(
                            new GoogleTVCommand(GoogleTVRequest.encodeMessage(GoogleTVRequest.loginRequest(4))));
                }

                String st = "";
                int length = 0;
                StringBuilder preambleSb = new StringBuilder();
                StringBuilder manufacturerSb = new StringBuilder();
                StringBuilder modelSb = new StringBuilder();
                StringBuilder androidVersionSb = new StringBuilder();
                StringBuilder remoteServerSb = new StringBuilder();
                StringBuilder remoteServerVersionSb = new StringBuilder();

                int i = 0;
                int current = 0;

                for (; i < 14; i++) {
                    preambleSb.append(charArray[i]);
                }

                i += 2; // 0a delimiter

                st = "" + charArray[i] + charArray[i + 1];
                length = Integer.parseInt(st, 16) * 2;
                i += 2;
                current = i;

                for (; i < current + length; i++) {
                    modelSb.append(charArray[i]);
                }

                i += 2; // 12 delimiter

                st = "" + charArray[i] + charArray[i + 1];
                length = Integer.parseInt(st, 16) * 2;
                i += 2;
                current = i;

                for (; i < current + length; i++) {
                    manufacturerSb.append(charArray[i]);
                }

                i += 6; // 18 01 22

                st = "" + charArray[i] + charArray[i + 1];
                length = Integer.parseInt(st, 16) * 2;
                i += 2;
                current = i;

                for (; i < current + length; i++) {
                    androidVersionSb.append(charArray[i]);
                }

                i += 2; // 2a delimiter

                st = "" + charArray[i] + charArray[i + 1];
                length = Integer.parseInt(st, 16) * 2;
                i += 2;
                current = i;

                for (; i < current + length; i++) {
                    remoteServerSb.append(charArray[i]);
                }

                i += 2; // 32 delimiter

                st = "" + charArray[i] + charArray[i + 1];
                length = Integer.parseInt(st, 16) * 2;
                i += 2;
                current = i;

                for (; i < current + length; i++) {
                    remoteServerVersionSb.append(charArray[i]);
                }

                String preamble = preambleSb.toString();
                String model = GoogleTVRequest.encodeMessage(modelSb.toString());
                String manufacturer = GoogleTVRequest.encodeMessage(manufacturerSb.toString());
                String androidVersion = GoogleTVRequest.encodeMessage(androidVersionSb.toString());
                String remoteServer = GoogleTVRequest.encodeMessage(remoteServerSb.toString());
                String remoteServerVersion = GoogleTVRequest.encodeMessage(remoteServerVersionSb.toString());

                logger.debug("{} - {} \"{}\" \"{}\" {} {} {}", thingId, preamble, model, manufacturer, androidVersion,
                        remoteServer, remoteServerVersion);

                callback.setModel(model);
                callback.setManufacturer(manufacturer);
                callback.setAndroidVersion(androidVersion);
                callback.setRemoteServer(remoteServer);
                callback.setRemoteServerVersion(remoteServerVersion);

            } else if (msg.startsWith(DELIMITER_12)) {
                // Second message on connection from GTV
                // Login successful
                callback.sendCommand(
                        new GoogleTVCommand(GoogleTVRequest.encodeMessage(GoogleTVRequest.loginRequest(5))));
                logger.info("{} - Login Successful", thingId);
                callback.setLoggedIn(true);
            } else if (msg.startsWith(DELIMITER_92)) {
                // Third message on connection from GTV
                // Also sent on power state change (to ON only unless keypress triggers)i
                // 9203 21 08 02 10 02 1a 11 534849454c4420416e64726f6964205456 20 02 2800 30 0f 38 0e 40 00
                // --------DD----DD----DD-LEN-SHIELD Android TV
                // 9203 1e 08 9610 10 09 1a 0d 4368726f6d6563617374204844 20 02 2800 30 19 38 0a 40 00
                // --------DD------DD----DD-LEN-Chromecast HD
                // 9203 1a 08 f304 10 09 1a 11 534849454c4420416e64726f6964205456 20 01
                // 9203 1a 08 8205 10 09 1a 11 534849454c4420416e64726f6964205456 20 01
                // --------DD------DD----DD-LEN-SHIELD Android TV
                //
                // VOLUME:
                // ---------------DD----DD----DD-LEN-BRAVIA 4K GB------------DD---------DD-MAX---VOL---MUTE
                // 00 --- 9203 1c 08 03 10 06 1a 0c 42524156494120344b204742 20 02 2800 30 64 38 00 40 00
                // 01 --- 9203 1c 08 03 10 06 1a 0c 42524156494120344b204742 20 02 2800 30 64 38 01 40 00
                // 100 -- 9203 1c 08 03 10 06 1a 0c 42524156494120344b204742 20 02 2800 30 64 38 64 40 00
                // MUTE - 9203 1c 08 03 10 06 1a 0c 42524156494120344b204742 20 02 2800 30 64 38 00 40 01

                String st = "";
                int length = 0;

                StringBuilder preambleSb = new StringBuilder();
                StringBuilder modelSb = new StringBuilder();
                String volMax = "";
                String volCurr = "";
                String volMute = "";
                String audioMode = "";

                int i = 0;
                int current = 0;

                for (; i < 12; i++) {
                    preambleSb.append(charArray[i]);
                }

                st = "" + charArray[i] + charArray[i + 1];
                do {
                    if (!DELIMITER_1A.equals(st)) {
                        preambleSb.append(st);
                        i += 2;
                        st = "" + charArray[i] + charArray[i + 1];
                    }
                } while (!DELIMITER_1A.equals(st));

                i += 2; // 1a delimiter

                st = "" + charArray[i] + charArray[i + 1];
                length = Integer.parseInt(st, 16) * 2;
                i += 2;
                current = i;

                for (; i < current + length; i++) {
                    modelSb.append(charArray[i]);
                }

                i += 2; // 20 delimiter

                st = "" + charArray[i] + charArray[i + 1];

                audioMode = st; // 01 remote audio - 02 local audio

                if (DELIMITER_02.equals(st)) {
                    i += 2; // 02 longer message
                    i += 4; // Unknown 2800 message
                    i += 2; // 30 delimiter
                    volMax = "" + charArray[i] + charArray[i + 1];
                    i += 4; // volMax + 38 delimiter
                    volCurr = "" + charArray[i] + charArray[i + 1];
                    i += 4; // volCurr + 40 delimiter
                    volMute = "" + charArray[i] + charArray[i + 1];

                    callback.setVolMax(volMax);
                    callback.setVolCurr(volCurr);
                    callback.setVolMute(volMute);
                }

                String preamble = preambleSb.toString();
                String model = GoogleTVRequest.encodeMessage(modelSb.toString());
                logger.debug("{} - Device Update: {} \"{}\" {} {} {} {}", thingId, preamble, model, audioMode, volMax,
                        volCurr, volMute);
                callback.setAudioMode(audioMode);

            } else if (msg.startsWith(DELIMITER_08)) {
                // PIN Process Messages. Only used on 6467.
                if (msg.startsWith(MESSAGE_PINSUCCESS)) {
                    // PIN Process Successful
                    logger.debug("{} - PIN Process Successful!", thingId);
                    callback.finishPinProcess();
                } else {
                    // 080210c801a201081204080310061801
                    // 080210c801fa0100
                    logger.debug("{} - PIN Intermediary Message: {}", thingId, msg);
                }
            } else if (msg.startsWith(DELIMITER_C2)) {
                // Power State
                // c202020800 - OFF
                // c202020801 - ON
                if (MESSAGE_POWEROFF.equals(msg)) {
                    callback.setPower(false);
                } else if (MESSAGE_POWERON.equals(msg)) {
                    callback.setPower(true);
                } else {
                    logger.info("{} - Unknown power state received. {}", thingId, msg);
                }
            } else if (msg.startsWith(DELIMITER_42)) {
                // Keepalive request
                callback.sendKeepAlive(msg);
            } else if (msg.startsWith(DELIMITER_A2)) {
                // Current app name. Sent on keypress and power change.
                // a201 21 0a 1f 62 1d 636f6d2e676f6f676c652e616e64726f69642e796f75747562652e7476
                // -----------------LEN-com.google.android.youtube.tv
                // a201 21 0a 1f 62 1d 636f6d2e676f6f676c652e616e64726f69642e74766c61756e63686572
                // -----------------LEN-com.google.android.tvlauncher
                // a201 14 0a 12 62 10 636f6d2e736f6e792e6474762e747678
                // -----------------LEN-com.sony.dtv.tvx
                // a201 15 0a 13 62 11 636f6d2e6e6574666c69782e6e696e6a61
                // -----------------LEN-com.netflix.ninja

                StringBuilder preambleSb = new StringBuilder();
                StringBuilder appNameSb = new StringBuilder();
                int i = 0;
                int current = 0;

                for (; i < 10; i++) {
                    preambleSb.append(charArray[i]);
                }

                i += 2; // 62 delimiter

                String st = "" + charArray[i] + charArray[i + 1];
                int length = Integer.parseInt(st, 16) * 2;
                i += 2;
                current = i;

                for (; i < current + length; i++) {
                    appNameSb.append(charArray[i]);
                }

                String preamble = preambleSb.toString();
                String appName = GoogleTVRequest.encodeMessage(appNameSb.toString());

                logger.debug("{} - Current App: {} {}", thingId, preamble, appName);
                callback.setCurrentApp(appName);
            } else {
                logger.info("{} - Unknown payload received. {} {}", thingId, len, msg);
            }
        } catch (Exception e) {
            logger.warn("{} - Message Parser Exception on {}", thingId, msg);
            logger.warn("{} - Message Parser Caught Exception", thingId, e);
        }
    }
}
