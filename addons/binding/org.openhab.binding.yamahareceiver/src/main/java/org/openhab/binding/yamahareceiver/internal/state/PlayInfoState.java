/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.state;

import static org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants.VALUE_EMPTY;
import static org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants.VALUE_NA;

/**
 * The play information state with current station, artist, song name
 *
 * @author David Graeff - Initial contribution
 * @author Tomasz Maruszak - Spotify support
 *
 */
public class PlayInfoState implements Invalidateable {

    public String station = VALUE_NA; // NET_RADIO. Will also be used for TUNER where Radio_Text_A/B will be used instead.
    public String artist = VALUE_NA; // USB, iPOD, PC
    public String album = VALUE_NA; // USB, iPOD, PC
    public String song = VALUE_NA; // USB, iPOD, PC
    public String songImageUrl = VALUE_EMPTY; // Spotify

    public String playbackMode = "Stop"; // All inputs

    public void invalidate() {
        this.playbackMode = VALUE_NA;
        this.station = VALUE_NA;
        this.artist = VALUE_NA;
        this.album = VALUE_NA;
        this.song = VALUE_NA;
        this.songImageUrl = VALUE_EMPTY;
    }
}
