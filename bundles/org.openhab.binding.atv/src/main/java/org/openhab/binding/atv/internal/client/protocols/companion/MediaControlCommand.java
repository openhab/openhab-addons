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
 * Media Control command constants.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public enum MediaControlCommand {
    Play(1),
    Pause(2),
    NextTrack(3),
    PreviousTrack(4),
    GetVolume(5),
    SetVolume(6),
    SkipBy(7),
    FastForwardBegin(8),
    FastForwardEnd(9),
    RewindBegin(10),
    RewindEnd(11),
    GetCaptionSettings(12),
    SetCaptionSettings(13);

    private final int value;

    MediaControlCommand(int value) {
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
    public static MediaControlCommand fromValue(int value) {
        for (MediaControlCommand command : values()) {
            if (command.value == value) {
                return command;
            }
        }
        throw new IllegalArgumentException("Unknown MediaControlCommand value: " + value);
    }
}
