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
package org.openhab.binding.yamahareceiver.internal.state;

import static org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.*;

/**
 * The play information state with current station, artist, song name
 *
 * @author David Graeff - Initial contribution
 * @author Tomasz Maruszak - Spotify support
 *
 */
public class PlayInfoState implements Invalidateable {

    public String station = VALUE_NA; // NET_RADIO. Will also be used for TUNER where Radio_Text_A/B will be used
                                      // instead.
    public String artist = VALUE_NA; // USB, iPOD, PC
    public String album = VALUE_NA; // USB, iPOD, PC
    public String song = VALUE_NA; // USB, iPOD, PC
    public String songImageUrl = VALUE_EMPTY; // Spotify

    public String playbackMode = "Stop"; // All inputs

    @Override
    public void invalidate() {
        this.playbackMode = VALUE_NA;
        this.station = VALUE_NA;
        this.artist = VALUE_NA;
        this.album = VALUE_NA;
        this.song = VALUE_NA;
        this.songImageUrl = VALUE_EMPTY;
    }
}
