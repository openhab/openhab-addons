/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.openhab.binding.squeezebox.internal.model.Favorite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This {@link SqueezeBoxNotificationListener}- is a type of PlayerEventListener
 * that's used to monitor certain events related to the notification functionality.
 *
 * @author Mark Hilbush - Initial contribution
 * @author Mark Hilbush - Added event to update favorites list
 */
public final class SqueezeBoxNotificationListener implements SqueezeBoxPlayerEventListener {
    private final Logger logger = LoggerFactory.getLogger(SqueezeBoxNotificationListener.class);

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

        if ("play".equals(mode)) {
            this.started.set(true);
        } else if (this.started.get() && "stop".equals(mode)) {
            this.stopped.set(true);
        }
        if ("pause".equals(mode)) {
            this.paused.set(true);
        }
    }

    /*
     * Monitor for when the volume is updated to a specific target value
     */
    @Override
    public void absoluteVolumeChangeEvent(String mac, int volume) {
        if (!this.playerMAC.equals(mac)) {
            return;
        }
        this.volume.set(volume);
        logger.trace("Volume is {} for player {}", volume, mac);
    }

    @Override
    public void relativeVolumeChangeEvent(String mac, int volumeChange) {
        if (!this.playerMAC.equals(mac)) {
            return;
        }

        int newVolume = this.volume.get() + volumeChange;
        newVolume = Math.min(newVolume, 100);
        newVolume = Math.max(newVolume, 0);

        this.volume.set(newVolume);
        logger.trace("Volume changed [{}] for player {}. New volume: {}", volumeChange, mac, volume);
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
    public void albumArtistChangeEvent(String mac, String albumArtist) {
    }

    @Override
    public void trackArtistChangeEvent(String mac, String trackArtist) {
    }

    @Override
    public void bandChangeEvent(String mac, String band) {
    }

    @Override
    public void composerChangeEvent(String mac, String composer) {
    }

    @Override
    public void conductorChangeEvent(String mac, String conductor) {
    }

    @Override
    public void remoteTitleChangeEvent(String mac, String title) {
    }

    @Override
    public void irCodeChangeEvent(String mac, String ircode) {
    }

    @Override
    public void updateFavoritesListEvent(List<Favorite> favorites) {
    }

    @Override
    public void sourceChangeEvent(String mac, String source) {
    }

    @Override
    public void buttonsChangeEvent(String mac, String likeCommand, String unlikeCommand) {
    }

    @Override
    public void connectedStateChangeEvent(String mac, boolean connected) {
    }
}
