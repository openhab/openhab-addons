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

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

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

    private final ShieldTVConnectionManager callback;

    public ShieldTVMessageParser(ShieldTVConnectionManager callback) {
        this.callback = callback;
    }

    public void handleMessage(String msg) {
        if (msg.trim().equals("")) {
            return; // Ignore empty lines
        }

        logger.trace("Received ShieldTV message from: {} - Message: {}", callback.getHostName(), msg);
        // logger.trace("Encoded message: {}", ShieldTVRequest.encodeMessage(msg));

        callback.validMessageReceived();

        char[] charArray = msg.toCharArray();

        try {
            // All lengths are little endian when larger than 0xff
            if (msg.startsWith("080a12") && msg.startsWith("08e807", 8)) {
                // Pre-login Hostname of Shield Replied
                // 080a 12 1408e807 12 0f08e807 12 LEN Hostname 18d7fd04 180a
                // 080a 12 1d08e807 12 180801 12 LEN Hostname 18d7fd04 180a
                // 080a 12 2208e807 12 1d08e807 12 LEN Hostname 18d7fd04 180a
                // Each chunk ends in 12
                // 4th chunk represent length of the name.
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

                callback.setHostName(ShieldTVRequest.encodeMessage(hostname.toString()));

            } else if (msg.startsWith("080b12")) {
                // Longer hostname reply
                // 080b 12 5b08b510 12 TOTALLEN? 0a LEN Hostname 12 LEN IPADDR Padding? 22 LEN DeviceID 2a LEN arm64-v8a
                // 2a LEN armeabi-v7a 2a LEN armeabi 180b
                // It's possible for there to be more or less of the arm lists
                logger.trace("Longer Hostname Reply");

                int i = 20;
                int length;
                int current;

                // Hostname
                String st = "" + charArray[i] + "" + charArray[i + 1];
                length = Integer.parseInt(st, 16) * 2;
                i += 2;

                StringBuffer hostname = new StringBuffer();
                current = i;

                for (; i < current + length; i = i + 2) {
                    st = "" + charArray[i] + "" + charArray[i + 1];
                    hostname.append(st);
                }

                i += 2; // 12

                // ipAddress
                st = "" + charArray[i] + "" + charArray[i + 1];
                length = Integer.parseInt(st, 16) * 2;
                i += 2;

                StringBuffer ipAddress = new StringBuffer();
                current = i;

                for (; i < current + length; i = i + 2) {
                    st = "" + charArray[i] + "" + charArray[i + 1];
                    ipAddress.append(st);
                }

                st = "" + charArray[i] + "" + charArray[i + 1];
                while (!st.equals("22")) {
                    i += 2;
                    st = "" + charArray[i] + "" + charArray[i + 1];
                }

                i += 2; // 22

                // deviceId

                st = "" + charArray[i] + "" + charArray[i + 1];
                length = Integer.parseInt(st, 16) * 2;
                i += 2;

                StringBuffer deviceId = new StringBuffer();
                current = i;

                for (; i < current + length; i = i + 2) {
                    st = "" + charArray[i] + "" + charArray[i + 1];
                    deviceId.append(st);
                }

                // architectures
                st = "" + charArray[i] + "" + charArray[i + 1];
                StringBuffer arch = new StringBuffer();
                while (st.equals("2a")) {
                    i += 2;
                    st = "" + charArray[i] + "" + charArray[i + 1];
                    length = Integer.parseInt(st, 16) * 2;
                    i += 2;
                    current = i;
                    for (; i < current + length; i = i + 2) {
                        st = "" + charArray[i] + "" + charArray[i + 1];
                        arch.append(st);
                    }
                    st = "" + charArray[i] + "" + charArray[i + 1];
                    if (st.equals("2a")) {
                        arch.append("2c");
                    }
                }

                logger.trace("Hostname: {}", ShieldTVRequest.encodeMessage(hostname.toString()));
                logger.trace("ipAddress: {}", ShieldTVRequest.encodeMessage(ipAddress.toString()));
                logger.trace("deviceId: {}", ShieldTVRequest.encodeMessage(deviceId.toString()));
                logger.trace("arch: {}", ShieldTVRequest.encodeMessage(arch.toString()));

                callback.setHostName(ShieldTVRequest.encodeMessage(hostname.toString()));
                callback.setDeviceID(ShieldTVRequest.encodeMessage(deviceId.toString()));
                callback.setArch(ShieldTVRequest.encodeMessage(arch.toString()));

            } else if (msg.startsWith("08f10712")) {
                // Massive dump of currently installed apps
                // 08f10712 d81f080112 d31f0a540a LEN app.name 12 LEN app.real.name 22 LEN URL 2801 30010a650a LEN

                Map<String, String> appNameDB = new HashMap<>();
                Map<String, String> appURLDB = new HashMap<>();
                int i = 18;
                String st = "";
                int length;
                int current;
                StringBuffer appSBPrepend = new StringBuffer();
                StringBuffer appSBDN = new StringBuffer();

                // Load default apps that don't get sent in payload

                appNameDB.put("com.google.android.tvlauncher", "Android TV Home");
                appURLDB.put("com.google.android.tvlauncher", "");

                appNameDB.put("com.google.android.katniss", "Google app for Android TV");
                appURLDB.put("com.google.android.katniss", "");

                appNameDB.put("com.google.android.katnisspx", "Google app for Android TV (Pictures)");
                appURLDB.put("com.google.android.katnisspx", "");

                appNameDB.put("com.google.android.backdrop", "Backdrop Daydream");
                appURLDB.put("com.google.android.backdrop", "");

                // Packet will end with 300118f107 after last entry

                while (i < msg.length() - 10) {
                    StringBuffer appSBName = new StringBuffer();
                    StringBuffer appSBURL = new StringBuffer();

                    // There are instances such as plex where multiple apps are sent as part of the same payload
                    // This is identified when 12 is the beginning of the set

                    st = "" + charArray[i] + "" + charArray[i + 1];

                    if (!st.toString().equals("12")) {
                        appSBPrepend = new StringBuffer();
                        appSBDN = new StringBuffer();

                        // App Prepend
                        // Usually 10 in length but can be longer or shorter so look for 0a twice
                        do {
                            st = "" + charArray[i] + "" + charArray[i + 1];
                            appSBPrepend.append(st);
                            i += 2;
                        } while (!st.toString().equals("0a"));
                        do {
                            st = "" + charArray[i] + "" + charArray[i + 1];
                            appSBPrepend.append(st);
                            i += 2;
                        } while (!st.toString().equals("0a"));
                        st = "" + charArray[i] + "" + charArray[i + 1];
                        if (st.toString().equals("0a")) {
                            appSBPrepend.append(st);
                            i += 2;
                            st = "" + charArray[i] + "" + charArray[i + 1];
                        }

                        // app DN
                        length = Integer.parseInt(st, 16) * 2;
                        i += 2;
                        current = i;
                        for (; i < current + length; i = i + 2) {
                            st = "" + charArray[i] + "" + charArray[i + 1];
                            appSBDN.append(st);
                        }
                    } else {
                        logger.debug("Second Entry");
                    }

                    // App Name

                    i += 2; // 12 delimiter
                    st = "" + charArray[i] + "" + charArray[i + 1];
                    i += 2;
                    length = Integer.parseInt(st, 16) * 2;
                    current = i;
                    for (; i < current + length; i = i + 2) {
                        st = "" + charArray[i] + "" + charArray[i + 1];
                        appSBName.append(st);
                    }

                    // There are times where there is padding here for no reason beyond the specified length.
                    // Proceed forward until we get to the 22 delimiter

                    st = "" + charArray[i] + "" + charArray[i + 1];
                    while (!st.toString().equals("22")) {
                        i += 2;
                        st = "" + charArray[i] + "" + charArray[i + 1];
                    }

                    // App URL
                    i += 2; // 22 delimiter
                    st = "" + charArray[i] + "" + charArray[i + 1];
                    i += 2;
                    length = Integer.parseInt(st, 16) * 2;
                    current = i;
                    for (; i < current + length; i = i + 2) {
                        st = "" + charArray[i] + "" + charArray[i + 1];
                        appSBURL.append(st);
                    }
                    st = "" + charArray[i] + "" + charArray[i + 1];
                    if (!st.toString().equals("12")) {
                        i += 4;
                    }
                    String appPrepend = appSBPrepend.toString();
                    String appDN = ShieldTVRequest.encodeMessage(appSBDN.toString());
                    String appName = ShieldTVRequest.encodeMessage(appSBName.toString());
                    String appURL = ShieldTVRequest.encodeMessage(appSBURL.toString());
                    logger.debug("AppPrepend: {} AppDN: {}", appPrepend, appDN);
                    logger.debug("AppName: {} AppURL: {}", appName, appURL);
                    appNameDB.put(appDN, appName);
                    appURLDB.put(appDN, appURL);
                }
                logger.trace("MP appNameDB: {} appURLDB: {}", appNameDB.toString(), appURLDB.toString());
                callback.setAppDB(appNameDB, appURLDB);
            } else if (msg.startsWith("08f30712")) {
                // This has something to do with successful command response, maybe.
            } else if (msg.equals("080028fae0a6c0d130")) {
                // Keepalive Reply
            } else if (msg.startsWith("080a12") && msg.startsWith("0308cf08", 6)) {
                // 080a 12 0308cf08 180a
                logger.debug("PIN Process Started");
            } else if (msg.startsWith("20") && msg.length() == 6) {
                // This seems to be 20**** when observed. It is unclear what this does.
                // This seems to send immediately before the certificate reply and as a reply to the pin being sent
            } else if (msg.startsWith("08f007")) {
                // Login successful???
                // This seems to happen after a successful PIN/Cert as well as on login with a valid cert
                // Maybe this should be what we use to set the shield online?
                // 08f007 12 0c0804 12 08 0a060804100e200f 18f007
                // 08f007 12 0c0804 12 LEN 0a060804100e200f 18f007
                logger.info("Login Successful to {}", callback.getHostName());
                callback.setLoggedIn(true);
            } else if (msg.equals("080a121108b510120c0804120854696d65206f7574180a")) {
                // Timeout
                // 080a 12 1108b510 12 0c0804 12 08 54696d65206f7574 180a
                // 080a 12 1108b510 12 0c0804 12 LEN Timeout 180a
                logger.debug("Timeout");
            } else if (msg.startsWith("08ec07")) {
                // Current App
                // 08ec07 12 2a0807 22 262205 656e5f555342 1d 636f6d2e676f6f676c652e616e64726f69642e74766c61756e63686572
                // 18ec07
                // 08ec07 12 2a0807 22 262205 en_USB LEN AppName 18ec07
                StringBuffer appName = new StringBuffer();
                for (int i = 36; i < msg.length() - 6; i++) {
                    appName.append(charArray[i]);
                }
                callback.setCurrentApp(ShieldTVRequest.encodeMessage(appName.toString()));
            } else if (msg.startsWith("080a12") && msg.startsWith("1008b510", 8)) {
                // Certificate Reply
                // |--6-----------12-----------10---------------16---------6--- = 50 characters long
                // |080a 12 ad10 08b510 12 a710 0801 12 07 53756363657373 1ac009 3082... 3082... 180a
                // |080a 12 9f10 08b510 12 9910 0801 12 07 53756363657373 1ac209 3082... 3082... 180a
                // |--------Little Endian Total Payload Length
                // |-----------------------Little Endian Remaining Payload Length
                // |-----------------------------------Length of SUCCESS
                // |--------------------------------------ASCII: SUCCESS
                // |-----------------------------------------------------Little Endian Length (e.g. 09c0 and 09c2 above)
                // |------------------------------------------------------------Priv Key RSA 2048
                // |--------------------------------------------------------------------Cert X.509
                if (msg.startsWith("0753756363657373", 28)) {
                    StringBuffer preamble = new StringBuffer();
                    StringBuffer privKey = new StringBuffer();
                    StringBuffer pubKey = new StringBuffer();
                    int i = 0;
                    int current;
                    for (; i < 44; i++) {
                        preamble.append(charArray[i]);
                    }
                    logger.trace("Cert Preamble:   {}", preamble.toString());

                    i += 2; // 1a
                    String st = "" + charArray[i + 2] + "" + charArray[i + 3] + "" + charArray[i] + ""
                            + charArray[i + 1];
                    int privLen = 2246 + ((Integer.parseInt(st, 16) - 2400) * 2);
                    i += 4; // length
                    current = i;

                    logger.trace("Cert privLen: {} {}", st, privLen);

                    for (; i < current + privLen; i++) {
                        privKey.append(charArray[i]);
                    }

                    logger.trace("Cert privKey: {} {}", privLen, privKey.toString());

                    for (; i < msg.length() - 4; i++) {
                        pubKey.append(charArray[i]);
                    }

                    logger.trace("Cert pubKey:  {} {}", msg.length() - privLen - 4, pubKey.toString());

                    byte[] privKeyB64Byte = DatatypeConverter.parseHexBinary(privKey.toString());
                    byte[] pubKeyB64Byte = DatatypeConverter.parseHexBinary(pubKey.toString());

                    String privKeyB64 = Base64.getEncoder().encodeToString(privKeyB64Byte);
                    String pubKeyB64 = Base64.getEncoder().encodeToString(pubKeyB64Byte);

                    callback.setKeys(privKeyB64, pubKeyB64);
                } else {
                    logger.info("Pin Process Failed.");
                }
            } else {
                logger.debug("Unknown payload received. {}", msg);
            }
        } catch (Exception e) {
            logger.debug("Message Parser Caught Exception", e);
        } finally {
            return;
        }
    }
}
