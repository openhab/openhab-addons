/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.squeezebox.handler;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This {@link SqueezeBoxNotificationListener}- is a type of PlayerEventListener
 * that's used to monitor certain events related to the notification functionality.
 *
 * @author Mark Hilbush - Implement AudioSink and notifications
 */
public final class SqueezeBoxNotificationListener implements SqueezeBoxPlayerEventListener {
    private Logger logger = LoggerFactory.getLogger(SqueezeBoxNotificationListener.class);

    private final String playerMAC;

    // Used to monitor when the player stops
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean stopped = new AtomicBoolean(false);

    // Used to monitor when the player pauses
    private final AtomicBoolean paused = new AtomicBoolean(false);

    // Used to monitor for updates to the playlist
    private final AtomicBoolean playlistUpdated = new AtomicBoolean(false);

    // Used to monitor when the player volume changes to a specific target value
    private final AtomicInteger volume = new AtomicInteger(-1);

    SqueezeBoxNotificationListener(String playerMAC) {
        this.playerMAC = playerMAC;
    }

    // Stopped
    public void resetStopped() {
        this.started.set(false);
        this.stopped.set(false);
    }

    public boolean isStopped() {
        return this.stopped.get();
    }

    // Paused
    public void resetPaused() {
        this.paused.set(false);
    }

    public boolean isPaused() {
        return this.paused.get();
    }

    // Playlist updated
    public void resetPlaylistUpdated() {
        this.playlistUpdated.set(false);
    }

    public boolean isPlaylistUpdated() {
        return this.playlistUpdated.get();
    }

    // Volume updated
    public void resetVolumeUpdated() {
        this.volume.set(-1);
    }

    public boolean isVolumeUpdated(int volume) {
        return this.volume.get() == volume;
    }

    // Implementation of listener interfaces
    @Override
    public void playerAdded(SqueezeBoxPlayer player) {
    }

    @Override
    public void powerChangeEvent(String mac, boolean power) {
    }

    /*
     * Monitor for player mode changing to stop.
     */
    @Override
    public void modeChangeEvent(String mac, String mode) {
        if (!this.playerMAC.equals(mac)) {
            return;
        }
        logger.trace("Mode is {} for player {}", mode, mac);

        if (mode.equals("play")) {
            this.started.set(true);
        } else if (this.started.get() && mode.equals("stop")) {
            this.stopped.set(true);
        }
        if (mode.equals("pause")) {
            this.paused.set(true);
        }
    }

    /*
     * Monitor for when the volume is updated to a specific target value
     */
    @Override
    public void volumeChangeEvent(String mac, int volume) {
        if (!this.playerMAC.equals(mac)) {
            return;
        }
        this.volume.set(volume);
        logger.trace("Volume is {} for player {}", volume, mac);
    }

    @Override
    public void muteChangeEvent(String mac, boolean mute) {
    }

    @Override
    public void currentPlaylistIndexEvent(String mac, int index) {
    }

    @Override
    public void currentPlayingTimeEvent(String mac, int time) {
    }

    @Override
    public void durationEvent(String mac, int duration) {
    }

    /*
     * Monitor for when the playlist is updated
     */
    @Override
    public void numberPlaylistTracksEvent(String mac, int track) {
        if (!this.playerMAC.equals(mac)) {
            return;
        }
        logger.trace("Number of playlist tracks is {} for player {}", track, mac);
        playlistUpdated.set(true);
    }

    @Override
    public void currentPlaylistShuffleEvent(String mac, int shuffle) {
    }

    @Override
    public void currentPlaylistRepeatEvent(String mac, int repeat) {
    }

    @Override
    public void titleChangeEvent(String mac, String title) {
    }

    @Override
    public void albumChangeEvent(String mac, String album) {
    }

    @Override
    public void artistChangeEvent(String mac, String artist) {
    }

    @Override
    public void coverArtChangeEvent(String mac, String coverArtUrl) {
    }

    @Override
    public void yearChangeEvent(String mac, String year) {
    }

    @Override
    public void genreChangeEvent(String mac, String genre) {
    }

    @Override
    public void remoteTitleChangeEvent(String mac, String title) {
    }

    @Override
    public void irCodeChangeEvent(String mac, String ircode) {
    }
}
