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
package org.openhab.binding.bosesoundtouch.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link RemoteKeyType} class is holding the Keys on a remote. For simulating key presses
 *
 * @author Christian Niessner - Initial contribution
 */
@NonNullByDefault
public enum RemoteKeyType {
    PLAY,
    PAUSE,
    STOP,
    PREV_TRACK,
    NEXT_TRACK,
    THUMBS_UP,
    THUMBS_DOWN,
    BOOKMARK,
    POWER,
    MUTE,
    VOLUME_UP,
    VOLUME_DOWN,
    PRESET_1,
    PRESET_2,
    PRESET_3,
    PRESET_4,
    PRESET_5,
    PRESET_6,
    AUX_INPUT,
    SHUFFLE_OFF,
    SHUFFLE_ON,
    REPEAT_OFF,
    REPEAT_ONE,
    REPEAT_ALL,
    PLAY_PAUSE,
    ADD_FAVORITE,
    REMOVE_FAVORITE,
    INVALID_KEY;

    private String name;

    private RemoteKeyType() {
        this.name = name();
    }

    @Override
    public String toString() {
        return name;
    }
}
