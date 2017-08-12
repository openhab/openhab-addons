/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.state;

/**
 * The play information state with current station, artist, song name
 *
 * @author David Graeff - Initial contribution
 */
public class PlayInfoState implements Invalidateable {
    public String station; // NET_RADIO. Will also be used for TUNER where Radio_Text_A/B will be used instead.
    public String artist; // USB, iPOD, PC
    public String album; // USB, iPOD, PC
    public String song; // USB, iPOD, PC

    public String playbackMode = "Stop"; // All inputs

    public void invalidate() {
        this.playbackMode = "N/A";
        this.station = "N/A";
        this.artist = "N/A";
        this.album = "N/A";
        this.song = "N/A";
    }
}