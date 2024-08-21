/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ShieldTVConstants} class defines common constants, which are
 * used across the shieldtv protocol.
 *
 * @author Ben Rosenblum - Initial contribution
 */
@NonNullByDefault
public class ShieldTVConstants {

    // List of all static String literals
    public static final String DELIMITER_0 = "0";
    public static final String DELIMITER_00 = "00";
    public static final String DELIMITER_08 = "08";
    public static final String DELIMITER_0A = "0a";
    public static final String DELIMITER_12 = "12";
    public static final String DELIMITER_18 = "18";
    public static final String DELIMITER_22 = "22";
    public static final String DELIMITER_2A = "2a";
    public static final String DELIMITER_E9 = "e9";
    public static final String DELIMITER_EC = "ec";
    public static final String DELIMITER_F0 = "f0";
    public static final String DELIMITER_F1 = "f1";
    public static final String DELIMITER_F3 = "f3";

    public static final String HARD_DROP = "ffffffff";

    public static final String APP_START_SUCCESS = "08f1071202080318f107";
    public static final String APP_START_FAILED = "08f107120608031202080118f107";
    public static final String KEEPALIVE_REPLY = "080028fae0a6c0d130";
    public static final String TIMEOUT = "080a121108b510120c0804120854696d65206f7574180a";

    public static final String MESSAGE_LOWPRIV = "080a12";
    public static final String MESSAGE_HOSTNAME = "080b12";
    public static final String MESSAGE_APPDB = "08f10712";
    public static final String MESSAGE_APPDB_FULL = "080112";
    public static final String MESSAGE_GOOD_COMMAND = "08f30712";
    public static final String MESSAGE_PINSTART = "0308cf08";
    public static final String MESSAGE_CERT_COMING = "20";
    public static final String MESSAGE_SUCCESS = "08f007";
    public static final String MESSAGE_APP_SUCCESS = "08ec07";
    public static final String MESSAGE_APP_GET_SUCCESS = "0803";
    public static final String MESSAGE_APP_CURRENT = "0807";
    public static final String MESSAGE_SHORTNAME = "08e807";
    public static final String MESSAGE_CERT = "08b510";
    public static final String MESSAGE_CERT_PAYLOAD = "0753756363657373";
}
