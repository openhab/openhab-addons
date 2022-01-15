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
package org.openhab.binding.squeezebox.internal.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SqueezeBoxPlayerState} is responsible for saving the state of a player.
 *
 * @author Mark Hilbush - Initial contribution
 * @author Patrik Gfeller - Moved class to its own file.
 */
class SqueezeBoxPlayerState {
    private final Logger logger = LoggerFactory.getLogger(SqueezeBoxPlayerState.class);

    private boolean savedMute;
    private boolean savedPower;
    private boolean savedStop;
    private boolean savedControl;

    private int savedVolume;
    private int savedShuffle;
    private int savedRepeat;
    private int savedPlaylistIndex;
    private int savedNumberPlaylistTracks;
    private int savedPlayingTime;

    private SqueezeBoxPlayerHandler playerHandler;

    public SqueezeBoxPlayerState(SqueezeBoxPlayerHandler playerHandler) {
        this.playerHandler = playerHandler;
        save();
    }

    boolean isMuted() {
        return savedMute;
    }

    boolean isPoweredOn() {
        return savedPower;
    }

    boolean isStopped() {
        return savedStop;
    }

    boolean isPlaying() {
        return savedControl;
    }

    boolean isShuffling() {
        return savedShuffle != 0;
    }

    int getShuffle() {
        return savedShuffle;
    }

    boolean isRepeating() {
        return savedRepeat != 0;
    }

    int getRepeat() {
        return savedRepeat;
    }

    int getVolume() {
        return savedVolume;
    }

    int getPlaylistIndex() {
        return savedPlaylistIndex;
    }

    private int getNumberPlaylistTracks() {
        return savedNumberPlaylistTracks;
    }

    int getPlayingTime() {
        return savedPlayingTime;
    }

    private void save() {
        savedVolume = playerHandler.currentVolume();
        savedMute = playerHandler.currentMute();
        savedPower = playerHandler.currentPower();
        savedStop = playerHandler.currentStop();
        savedControl = playerHandler.currentControl();
        savedShuffle = playerHandler.currentShuffle();
        savedRepeat = playerHandler.currentRepeat();
        savedPlaylistIndex = playerHandler.currentPlaylistIndex();
        savedNumberPlaylistTracks = playerHandler.currentNumberPlaylistTracks();
        savedPlayingTime = playerHandler.currentPlayingTime();

        logger.debug("Cur State: vol={}, mut={}, pwr={}, stp={}, ctl={}, shf={}, rpt={}, tix={}, tnm={}, tim={}",
                savedVolume, muteAsString(), powerAsString(), stopAsString(), controlAsString(), shuffleAsString(),
                repeatAsString(), getPlaylistIndex(), getNumberPlaylistTracks(), getPlayingTime());
    }

    private String muteAsString() {
        return isMuted() ? "MUTED" : "NOT MUTED";
    }

    private String powerAsString() {
        return isPoweredOn() ? "ON" : "OFF";
    }

    private String stopAsString() {
        return isStopped() ? "STOPPED" : "NOT STOPPED";
    }

    private String controlAsString() {
        return isPlaying() ? "PLAYING" : "PAUSED";
    }

    private String shuffleAsString() {
        String shuffle = "OFF";
        if (getShuffle() == 1) {
            shuffle = "SONG";
        } else if (getShuffle() == 2) {
            shuffle = "ALBUM";
        }
        return shuffle;
    }

    private String repeatAsString() {
        String repeat = "OFF";
        if (getRepeat() == 1) {
            repeat = "SONG";
        } else if (getRepeat() == 2) {
            repeat = "PLAYLIST";
        }
        return repeat;
    }

    /***
     * Return the player state as {@link SqueezeBoxPlayerPlayState}
     *
     * @return {@link SqueezeBoxPlayerPlayState}
     */
    SqueezeBoxPlayerPlayState getPlayState() {
        if (!isPlaying() && !isStopped()) {
            return SqueezeBoxPlayerPlayState.PAUSE;
        }

        if (isPlaying()) {
            return SqueezeBoxPlayerPlayState.PLAY;
        }

        return SqueezeBoxPlayerPlayState.STOP;
    }
}
