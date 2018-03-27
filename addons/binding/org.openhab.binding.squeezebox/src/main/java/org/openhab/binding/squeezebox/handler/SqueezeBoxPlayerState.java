/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.squeezebox.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SqueezeBoxPlayerState} is responsible for saving the state of a player.
 *
 * @author Mark Hilbush - Added support for AudioSink and notifications
 * @author Patrik Gfeller - Moved class to its own file.
 */
class SqueezeBoxPlayerState {
    boolean savedMute;
    boolean savedPower;
    boolean savedStop;
    boolean savedControl;

    int savedVolume;
    int savedShuffle;
    int savedRepeat;
    int savedPlaylistIndex;
    int savedNumberPlaylistTracks;
    int savedPlayingTime;

    private SqueezeBoxPlayerHandler playerHandler;
    private Logger logger = LoggerFactory.getLogger(SqueezeBoxPlayerState.class);

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
        return savedShuffle == 0 ? false : true;
    }

    int getShuffle() {
        return savedShuffle;
    }

    boolean isRepeating() {
        return savedRepeat == 0 ? false : true;
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
