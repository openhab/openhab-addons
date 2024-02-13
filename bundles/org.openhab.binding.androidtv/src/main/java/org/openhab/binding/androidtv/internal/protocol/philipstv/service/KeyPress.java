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
package org.openhab.binding.androidtv.internal.protocol.philipstv.service;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The {@link KeyPress} presents all available key codes of Philips TV.
 *
 * @see <a
 *      href=
 *      "http://jointspace.sourceforge.net/projectdata/documentation/jasonApi/1/doc/API-Method-input-key-POST.html">http://jointspace.sourceforge.net/projectdata/documentation/jasonApi/1/doc/API-Method-input-key-POST.html
 *      </a>
 *
 *
 * @author Benjamin Meyer - Initial contribution
 * @author Ben Rosenblum - Merged into AndroidTV
 */
@NonNullByDefault
public enum KeyPress {

    KEY_STANDBY("Standby"),
    KEY_BACK("Back"),
    KEY_FIND("Find"),
    KEY_RED_COLOR("RedColour"),
    KEY_GREEN_COLOR("GreenColour"),
    KEY_YELLOW_COLOR("YellowColour"),
    KEY_BLUE_COLOR("BlueColour"),
    KEY_HOME("Home"),
    KEY_VOLUME_UP("VolumeUp"),
    KEY_VOLUME_DOWN("VolumeDown"),
    KEY_MUTE("Mute"),
    KEY_OPTIONS("Options"),
    KEY_DOT("Dot"),
    KEY_0("Digit0"),
    KEY_1("Digit1"),
    KEY_2("Digit2"),
    KEY_3("Digit3"),
    KEY_4("Digit4"),
    KEY_5("Digit5"),
    KEY_6("Digit6"),
    KEY_7("Digit7"),
    KEY_8("Digit8"),
    KEY_9("Digit9"),
    KEY_INFO("Info"),
    KEY_CURSOR_UP("CursorUp"),
    KEY_CURSOR_DOWN("CursorDown"),
    KEY_CURSOR_LEFT("CursorLeft"),
    KEY_CURSOR_RIGHT("CursorRight"),
    KEY_CONFIRM("Confirm"),
    KEY_NEXT("Next"),
    KEY_PREVIOUS("Previous"),
    KEY_ADJUST("Adjust"),
    KEY_WATCH_TV("WatchTV"),
    KEY_VIEW_MODE("Viewmode"),
    KEY_TELETEXT("Teletext"),
    KEY_SUBTITLE("Subtitle"),
    KEY_CHANNEL_STEP_UP("ChannelStepUp"),
    KEY_CHANNEL_STEP_DOWN("ChannelStepDown"),
    KEY_SOURCE("Source"),
    KEY_AMBILIGHT_ON_OFF("AmbilightOnOff"),
    KEY_PLAY("Play"),
    KEY_PAUSE("Pause"),
    KEY_FAST_FORWARD("FastForward"),
    KEY_STOP("Stop"),
    KEY_REWIND("Rewind"),
    KEY_RECORD("Record"),
    KEY_ONLINE("Online");

    private final String value;

    KeyPress(String value) {
        this.value = value;
    }

    public static KeyPress getKeyPressForValue(String value) throws IllegalArgumentException {
        return Arrays.stream(values()).filter(v -> v.value.equalsIgnoreCase(value)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Key code could not be recognized: " + value));
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }
}
