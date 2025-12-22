/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.linkplay.internal.client.http.dto;

/**
 * Combined shuffle/repeat loop mode reported by the HTTP API as codes 0..5.
 *
 * Mapping (code → shuffle/repeat):
 * 0: shuffle=off, repeat=on (loop all)
 * 1: shuffle=off, repeat=on (loop one)
 * 2: shuffle=on, repeat=on (loop all)
 * 3: shuffle=on, repeat=off
 * 4: shuffle=off, repeat=off
 * 5: shuffle=on, repeat=on (loop one)
 * 
 * @author Dan Cunningham - Initial contribution
 */
public enum LoopMode {
    REPEAT_ALL(0, false, true, false),
    REPEAT_ONE(1, false, true, true),
    SHUFFLE_REPEAT_ALL(2, true, true, false),
    SHUFFLE_NO_REPEAT(3, true, false, false),
    NORMAL(4, false, false, false),
    SHUFFLE_REPEAT_ONE(5, true, true, true);

    private final int code;
    private final boolean shuffleEnabled;
    private final boolean repeatEnabled;
    private final boolean repeatOne;

    LoopMode(int code, boolean shuffleEnabled, boolean repeatEnabled, boolean repeatOne) {
        this.code = code;
        this.shuffleEnabled = shuffleEnabled;
        this.repeatEnabled = repeatEnabled;
        this.repeatOne = repeatOne;
    }

    public int getCode() {
        return code;
    }

    public boolean isShuffleEnabled() {
        return shuffleEnabled;
    }

    public boolean isRepeatEnabled() {
        return repeatEnabled;
    }

    public boolean isRepeatOne() {
        return repeatOne;
    }

    public static LoopMode fromCode(int code) {
        for (LoopMode m : values()) {
            if (m.code == code) {
                return m;
            }
        }
        return null;
    }
}
