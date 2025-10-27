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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Player operating mode as reported by the HTTP API "getPlayerStatus" field "mode".
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public enum PlayerMode {
    IDLE(0),
    AIRPLAY(1),
    DLNA(2),
    NETWORK(10),
    UDISK_PLAYBACK(11),
    HTTPAPI(20),
    SPOTIFY_CONNECT(31),
    LINE_IN(40),
    BLUETOOTH(41),
    OPTICAL(43),
    LINE_IN_2(47),
    USBDAC(51),
    GUEST_MULTIROOM(99);

    private final int code;

    PlayerMode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static @Nullable PlayerMode fromCode(int code) {
        switch (code) {
            case 0:
                return IDLE;
            case 1:
                return AIRPLAY;
            case 2:
                return DLNA;
            case 10:
                return NETWORK;
            case 11:
                return UDISK_PLAYBACK;
            case 20:
                return HTTPAPI;
            case 31:
                return SPOTIFY_CONNECT;
            case 40:
                return LINE_IN;
            case 41:
                return BLUETOOTH;
            case 43:
                return OPTICAL;
            case 47:
                return LINE_IN_2;
            case 51:
                return USBDAC;
            case 99:
                return GUEST_MULTIROOM;
            default:
                return null;
        }
    }

    public @Nullable String toSourceInputValue() {
        switch (this) {
            case BLUETOOTH:
                return "bluetooth";
            case LINE_IN:
            case LINE_IN_2:
                return "line-in";
            case OPTICAL:
                return "optical";
            case UDISK_PLAYBACK:
                return "udisk";
            case USBDAC:
                return "PCUSB";
            default:
                // For streaming/idle/guest modes, do not force a source input
                return null;
        }
    }
}
