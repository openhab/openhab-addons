/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enigma2.internal;

/**
 * The {@link Enigma2RemoteKey} represents the remote keys of an enigma2 device
 *
 * @author Thomas Traunbauer - Initial contribution
 */
public enum Enigma2RemoteKey {
    POWER1(116),

    KEY1(2),
    KEY2(3),
    KEY3(4),
    KEY4(5),
    KEY5(6),
    KEY6(7),
    KEY7(8),
    KEY8(9),
    KEY9(10),
    KEY0(11),

    ARROW_LEFT(412),
    ARROW_RIGHT(407),

    VOLUME_DOWN(114),
    VOLUME_UP(115),
    EXIT(1),
    MUTE(113),

    CHANNEL_UP(402),
    CHANNEL_DOWN(403),

    INF0(358),
    MENU(141),
    LEFT(105),
    RIGHT(106),
    UP(103),
    DOWN(108),
    OK(352),

    AUDIO(392),
    VIDEO(393),

    RED(398),
    GREEN(399),
    YELLOW(400),
    BLUE(401),

    PLAY(207),
    PAUSE(119),

    FAST_FORWARD(208),
    FAST_BACKWARD(168),

    TV(385),
    RADIO(377),
    TEXT(66),
    HELP(138),

    NUMBER(128),
    EQUALS(385);

    private int value;

    Enigma2RemoteKey(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(value);
        return buffer.toString();
    }
}
