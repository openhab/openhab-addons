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
package org.openhab.binding.enigma2.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Enigma2RemoteKey} class defines the remote keys of an enigma2 device
 * used across the whole binding.
 *
 * @author Guido Dolfen - Initial contribution
 */
@NonNullByDefault
public enum Enigma2RemoteKey {
    POWER(116),

    KEY_0(11),
    KEY_1(2),
    KEY_2(3),
    KEY_3(4),
    KEY_4(5),
    KEY_5(6),
    KEY_6(7),
    KEY_7(8),
    KEY_8(9),
    KEY_9(10),

    ARROW_LEFT(412),
    ARROW_RIGHT(407),

    VOLUME_DOWN(114),
    VOLUME_UP(115),
    MUTE(113),

    CHANNEL_UP(402),
    CHANNEL_DOWN(403),

    LEFT(105),
    RIGHT(106),
    UP(103),
    DOWN(108),
    OK(352),
    EXIT(174),

    RED(398),
    GREEN(399),
    YELLOW(400),
    BLUE(401),

    PLAY(207),
    PAUSE(119),
    STOP(128),
    RECORD(167),
    FAST_FORWARD(208),
    FAST_BACKWARD(168),

    TV(377),
    RADIO(385),
    AUDIO(392),
    VIDEO(393),
    TEXT(388),
    INFO(358),
    MENU(139),
    HELP(138),
    SUBTITLE(370),
    EPG(358);

    private final int value;

    Enigma2RemoteKey(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
