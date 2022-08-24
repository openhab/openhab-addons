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
package org.openhab.binding.vizio.internal.enums;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link KeyCommand} class provides enum values for remote control button press commands.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public enum KeyCommand {
    SEEKFWD(2, 0),
    SEEKBACK(2, 1),
    PAUSE(2, 2),
    PLAY(2, 3),
    DOWN(3, 0),
    LEFT(3, 1),
    OK(3, 2),
    LEFT2(3, 4),
    RIGHT(3, 7),
    UP(3, 8),
    BACK(4, 0),
    SMARTCAST(4, 3),
    CCTOGGLE(4, 4),
    INFO(4, 6),
    MENU(4, 8),
    HOME(4, 15),
    VOLUMEDOWN(5, 0),
    VOLUMEUP(5, 1),
    MUTEOFF(5, 2),
    MUTEON(5, 3),
    MUTETOGGLE(5, 4),
    PICTUREMODE(6, 0),
    WIDEMODE(6, 1),
    WIDETOGGLE(6, 2),
    INPUTTOGGLE(7, 1),
    CHANNELDOWN(8, 0),
    CHANNELUP(8, 1),
    PREVIOUSCH(8, 2),
    EXIT(9, 0),
    POWEROFF(11, 0),
    POWERON(11, 1),
    POWERTOGGLE(11, 2);

    private static final String KEY_COMMAND_STR = "{\"KEYLIST\": [{\"CODESET\": %d,\"CODE\": %d,\"ACTION\":\"KEYPRESS\"}]}";

    private final int codeSet;
    private final int code;

    KeyCommand(int codeSet, int code) {
        this.codeSet = codeSet;
        this.code = code;
    }

    public String getJson() {
        return String.format(KEY_COMMAND_STR, codeSet, code);
    }
}
