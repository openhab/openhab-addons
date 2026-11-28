/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.atv.internal.client.protocols.companion;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * HID command constants.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public enum HidCommand {
    Up(1),
    Down(2),
    Left(3),
    Right(4),
    Menu(5),
    Select(6),
    Home(7),
    VolumeUp(8),
    VolumeDown(9),
    Siri(10),
    Screensaver(11),
    Sleep(12),
    Wake(13),
    PlayPause(14),
    ChannelIncrement(15),
    ChannelDecrement(16),
    Guide(17),
    PageUp(18),
    PageDown(19);

    private final int value;

    HidCommand(int value) {
        this.value = value;
    }

    /** Numeric value used on the wire. */
    public int value() {
        return value;
    }

    /**
     * Looks up the constant matching an on-wire value.
     *
     * @param value the numeric value
     * @return the matching constant
     * @throws IllegalArgumentException if no constant has the given value
     */
    public static HidCommand fromValue(int value) {
        for (HidCommand command : values()) {
            if (command.value == value) {
                return command;
            }
        }
        throw new IllegalArgumentException("Unknown HidCommand value: " + value);
    }
}
