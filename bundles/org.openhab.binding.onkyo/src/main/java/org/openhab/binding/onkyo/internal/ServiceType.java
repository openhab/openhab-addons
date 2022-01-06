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
package org.openhab.binding.onkyo.internal;

/**
 * List of Onkyo Net service types
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public enum ServiceType {

    MUSIC_SERVER(0x00),
    FAVORITE(0x01),
    VTUNER(0x02),
    SIRIUSXM(0x03),
    PANDORA(0x04),
    RHAPSODY(0x06),
    LASTFM(0x06),
    NAPSTER(0x07),
    SLACKER(0x08),
    MEDIAFLY(0x09),
    SPOTIFY(0x0A),
    AUPEO(0x0B),
    RADIKO(0x0C),
    EONKYO(0x0D),
    TUNEIN(0x0E),
    MP3TUNES(0x0F),
    SIMFY(0x10),
    HOMEMEDIA(0x11),
    DEEZER(0x12),
    IHEARTRADIO(0x13),
    AIRPLAY(0x18),
    TIDAL(0x19),
    ONKYO_MUSIC(0x1A),
    USB(0xF0),
    USB_REAR(0xF1),
    INTERNETRADIO(0xF2),
    NET(0xF3),
    NONE(0xFF);

    private final int id;

    ServiceType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static ServiceType getType(int value) {
        for (ServiceType st : ServiceType.values()) {
            if (st.getId() == value) {
                return st;
            }
        }
        return NONE;
    }

    @Override
    public String toString() {
        switch (this) {
            case MUSIC_SERVER:
                return "Music Server (DLNA)";
            case FAVORITE:
                return "Favorite";
            case VTUNER:
                return "vTuner";
            case SIRIUSXM:
                return "SiriusXM";
            case PANDORA:
                return "Pandora";
            case RHAPSODY:
                return "Rhapsody";
            case LASTFM:
                return "Last.fm";
            case NAPSTER:
                return "Napster";
            case SLACKER:
                return "Slacker";
            case MEDIAFLY:
                return "Mediafly";
            case SPOTIFY:
                return "Spotify";
            case AUPEO:
                return "AUPEO!";
            case RADIKO:
                return "radiko";
            case EONKYO:
                return "e-onkyo";
            case TUNEIN:
                return "TuneIn Radio";
            case MP3TUNES:
                return "MP3tunes";
            case SIMFY:
                return "Simfy";
            case HOMEMEDIA:
                return "Home Media";
            case DEEZER:
                return "Deezer";
            case IHEARTRADIO:
                return "iHeartRadio";
            case AIRPLAY:
                return "Airplay";
            case TIDAL:
                return "TIDAL";
            case ONKYO_MUSIC:
                return "onkyo music";
            case USB:
                return "USB/USB(Front)";
            case USB_REAR:
                return "USB(Rear)";
            case INTERNETRADIO:
                return "Internet Radio";
            case NET:
                return "NET";
            case NONE:
                return "None";
            default:
                return "Invalid/unknown";
        }
    }
}
