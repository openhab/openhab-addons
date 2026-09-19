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
package org.openhab.binding.lghorizon.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * W3C key names accepted by the set-top box's {@code CPE.KeyEvent} MQTT message.
 *
 * @author Mark - Initial contribution
 */
@NonNullByDefault
public final class LGHorizonKeys {

    // Power
    public static final String POWER = "Power";
    public static final String STANDBY = "Standby";
    public static final String WAKE_UP = "WakeUp";

    // Playback
    public static final String PLAY = "MediaPlay";
    public static final String PAUSE = "MediaPause";
    public static final String PLAY_PAUSE = "MediaPlayPause";
    public static final String STOP = "MediaStop";
    public static final String RECORD = "MediaRecord";
    public static final String FAST_FORWARD = "MediaFastForward";
    public static final String REWIND = "MediaRewind";
    public static final String TRACK_NEXT = "MediaTrackNext";
    public static final String TRACK_PREVIOUS = "MediaTrackPrevious";

    // Jump to linear TV
    public static final String TV = "TV";

    // Channel / navigation
    public static final String CHANNEL_UP = "ChannelUp";
    public static final String CHANNEL_DOWN = "ChannelDown";
    public static final String TOP_MENU = "MediaTopMenu";
    public static final String GUIDE = "Guide";
    public static final String INFO = "Info";
    public static final String CONTEXT_MENU = "ContextMenu";

    // D-Pad
    public static final String ARROW_UP = "ArrowUp";
    public static final String ARROW_DOWN = "ArrowDown";
    public static final String ARROW_LEFT = "ArrowLeft";
    public static final String ARROW_RIGHT = "ArrowRight";
    public static final String ENTER = "Enter";
    public static final String ESCAPE = "Escape";
    public static final String BACKSPACE = "Backspace";

    // Color buttons
    public static final String RED = "Red";
    public static final String GREEN = "Green";
    public static final String YELLOW = "Yellow";
    public static final String BLUE = "Blue";

    private LGHorizonKeys() {
        // constants
    }
}
