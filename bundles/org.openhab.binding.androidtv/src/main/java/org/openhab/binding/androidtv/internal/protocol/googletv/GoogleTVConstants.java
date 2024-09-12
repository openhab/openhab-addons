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
package org.openhab.binding.androidtv.internal.protocol.googletv;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link GoogleTVConstants} class defines common constants, which are
 * used across the googletv protocol.
 *
 * @author Ben Rosenblum - Initial contribution
 */
@NonNullByDefault
public class GoogleTVConstants {

    // List of all static String literals
    public static final String DELIMITER_00 = "00";
    public static final String DELIMITER_01 = "01";
    public static final String DELIMITER_02 = "02";
    public static final String DELIMITER_08 = "08";
    public static final String DELIMITER_0A = "0a";
    public static final String DELIMITER_10 = "10";
    public static final String DELIMITER_12 = "12";
    public static final String DELIMITER_1A = "1a";
    public static final String DELIMITER_42 = "42";
    public static final String DELIMITER_92 = "92";
    public static final String DELIMITER_A2 = "a2";
    public static final String DELIMITER_C2 = "c2";

    public static final String MESSAGE_POWEROFF = "c202020800";
    public static final String MESSAGE_POWERON = "c202020801";
    public static final String MESSAGE_PINSUCCESS = "080210c801ca02";
    public static final String HARD_DROP = "ffffffff";
    public static final String VERSION_01 = "7b2270726f746f636f6c5f76657273696f6e223a312c22737461747573223a3430307d";
}
