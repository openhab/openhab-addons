/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch.internal.items;

/**
 * The {@link RemoteKey} class is holding the Keys on a remote. For simulating key presses
 *
 * @author Christian Niessner - Initial contribution
 */
public enum RemoteKey {
    PLAY(0),
    PAUSE(0),
    STOP(0),
    PREV_TRACK(0),
    NEXT_TRACK(0),
    THUMBS_UP(0),
    THUMBS_DOWN(0),
    BOOKMARK(0),
    POWER(0),
    MUTE(0),
    VOLUME_UP(0),
    VOLUME_DOWN(0),
    PRESET_1(1),
    PRESET_2(2),
    PRESET_3(3),
    PRESET_4(4),
    PRESET_5(5),
    PRESET_6(6),
    AUX_INPUT(0),
    SHUFFLE_OFF(0),
    SHUFFLE_ON(0),
    REPEAT_OFF(0),
    REPEAT_ONE(0),
    REPEAT_ALL(0),
    PLAY_PAUSE(0),
    ADD_FAVORITE(0),
    REMOVE_FAVORITE(0),
    INVALID_KEY(0);

    private String name;
    private int value;

    private RemoteKey(int value) {
        this.name = name();
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

}