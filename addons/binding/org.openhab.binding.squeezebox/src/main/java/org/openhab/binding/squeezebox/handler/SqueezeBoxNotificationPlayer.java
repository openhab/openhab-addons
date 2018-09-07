/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.squeezebox.handler;

import java.io.Closeable;

import org.eclipse.smarthome.core.library.types.StringType;
import org.openhab.binding.squeezebox.internal.utils.SqueezeBoxTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * Utility class to play a notification message. The message is added
 * to the playlist, played and the previous state of the playlist and the
 * player is restored.
 *
 * @author Mark Hilbush - Initial Contribution
 * @author Patrik Gfeller - Utility class added reduce complexity and length of SqueezeBoxPlayerHandler.java
 * @author Mark Hilbush - Convert sound notification volume from channel to config parameter
 *
 */
class SqueezeBoxNotificationPlayer implements Closeable {
    // An exception is thrown if we do not receive an acknowledge
    // for a volume set command in the given amount of time [s].
    private static final int VOLUME_COMMAND_TIMEOUT = 4;

    // We expect the media server to acknowledge a playlist command.
    // An exception is thrown if the playlist command was not processed
    // after the defined amount in [s]
    private static final int PLAYLIST_COMMAND_TIMEOUT = 5;

    private final Logger logger = LoggerFactory.getLogger(SqueezeBoxNotificationPlayer.class);
    private final SqueezeBoxPlayerState playerState;
    private final SqueezeBoxPlayerHandler squeezeBoxPlayerHandler;
    private final SqueezeBoxServerHandler squeezeBoxServerHandler;
    private final StringType uri;
    private final String mac;

    boolean playlistModified;

    private int notificationMessagePlaylistsIndex;

    SqueezeBoxNotificationPlayer(SqueezeBoxPlayerHandler squeezeBoxPlayerHandler,
            SqueezeBoxServerHandler squeezeBoxServerHandler, StringType uri) {
        this.squeezeBoxPlayerHandler = squeezeBoxPlayerHandler;
        this.squeezeBoxServerHandler = squeezeBoxServerHandler;
        this.mac = squeezeBoxPlayerHandler.getMac();
        this.uri = uri;
        this.playerState = new SqueezeBoxPlayerState(squeezeBoxPlayerHandler);
    }

    void play() throws InterruptedException, SqueezeBoxTimeoutException {
        if (squeezeBoxServerHandler == null) {
            logger.warn("Server handler is null");
            return;
        }
        setupPlayerForNotification();
        addNotificationMessageToPlaylist();
        playNotification();
    }

    @Override
    public void close() {
        restorePlayerState();
    }

    private void setupPlayerForNotification() throws InterruptedException, SqueezeBoxTimeoutException {
        logger.debug("Setting up player for notification");
        if (!playerState.isPoweredOn()) {
            logger.debug("Powering on the player");
            squeezeBoxServerHandler.powerOn(mac);
        }
        if (playerState.isShuffling()) {
            logger.debug("Turning off shuffle");
            squeezeBoxServerHandler.setShuffleMode(mac, 0);
        }
        if (playerState.isRepeating()) {
            logger.debug("Turning off repeat");
            squeezeBoxServerHandler.setRepeatMode(mac, 0);
        }
        if (playerState.isPlaying()) {
            squeezeBoxServerHandler.stop(mac);
        }
        setVolume(squeezeBoxPlayerHandler.getNotificationSoundVolume().intValue());
    }

    /**
     * Sends a volume set command if target volume is not equal to the current volume.
     *
     * @param requestedVolume The requested volume value.
     * @throws InterruptedException Thread interrupted during while we were waiting for an answer from the media server.
     * @throws SqueezeBoxTimeoutException Volume command was not acknowledged by the media server.
     */
    private void setVolume(int requestedVolume) throws InterruptedException, SqueezeBoxTimeoutException {
        if (playerState.getVolume() == requestedVolume) {
            return;
        }

        SqueezeBoxNotificationListener listener = new SqueezeBoxNotificationListener(mac);
        listener.resetVolumeUpdated();

        squeezeBoxServerHandler.registerSqueezeBoxPlayerListener(listener);
        squeezeBoxServerHandler.setVolume(mac, requestedVolume);

        logger.trace("Waiting up to {} s for volume to be updated...", VOLUME_COMMAND_TIMEOUT);

        try {
            int timeoutCount = 0;

            while (!listener.isVolumeUpdated(requestedVolume)) {
                Thread.sleep(100);
                if (timeoutCount++ > VOLUME_COMMAND_TIMEOUT * 10) {
                    throw new SqueezeBoxTimeoutException("Unable to update volume.");
                }
            }
        } finally {
            squeezeBoxServerHandler.unregisterSqueezeBoxPlayerListener(listener);
        }
    }

    private void addNotificationMessageToPlaylist() throws InterruptedException, SqueezeBoxTimeoutException {
        logger.debug("Adding notification message to playlist");
        SqueezeBoxNotificationListener listener = new SqueezeBoxNotificationListener(mac);
        listener.resetPlaylistUpdated();

        squeezeBoxServerHandler.registerSqueezeBoxPlayerListener(listener);
        squeezeBoxServerHandler.addPlaylistItem(mac, uri.toString(), "Notification");

        try {
            updatePlaylist(listener);
            this.playlistModified = true;
        } finally {
            squeezeBoxServerHandler.unregisterSqueezeBoxPlayerListener(listener);
        }
    }

    private void removeNotificationMessageFromPlaylist() throws InterruptedException, SqueezeBoxTimeoutException {
        logger.debug("Removing notification message from playlist");
        SqueezeBoxNotificationListener listener = new SqueezeBoxNotificationListener(mac);
        listener.resetPlaylistUpdated();

        squeezeBoxServerHandler.registerSqueezeBoxPlayerListener(listener);
        squeezeBoxServerHandler.deletePlaylistItem(mac, notificationMessagePlaylistsIndex);

        try {
            updatePlaylist(listener);
        } finally {
            squeezeBoxServerHandler.unregisterSqueezeBoxPlayerListener(listener);
        }
    }

    /**
     * Monitor the number of playlist entries. When it changes, then we know the playlist
     * has been updated with the notification URL. There's probably an edge case here where
     * someone is updating the playlist at the same time, but that should be rare.
     *
     * @param listener
     * @throws InterruptedException
     * @throws SqueezeBoxTimeoutException
     */
    private void updatePlaylist(SqueezeBoxNotificationListener listener)
            throws InterruptedException, SqueezeBoxTimeoutException {
        logger.trace("Waiting up to {} s for playlist to be updated...", PLAYLIST_COMMAND_TIMEOUT);

        int timeoutCount = 0;

        while (!listener.isPlaylistUpdated()) {
            Thread.sleep(100);
            if (timeoutCount++ > PLAYLIST_COMMAND_TIMEOUT * 10) {
                logger.debug("Update playlist timed out after {} seconds", PLAYLIST_COMMAND_TIMEOUT);
                throw new SqueezeBoxTimeoutException("Unable to update playlist.");
            }
        }
        logger.debug("Playlist updated");
    }

    private void playNotification() throws InterruptedException, SqueezeBoxTimeoutException {
        logger.debug("Playing notification");

        notificationMessagePlaylistsIndex = squeezeBoxPlayerHandler.currentNumberPlaylistTracks() - 1;
        SqueezeBoxNotificationListener listener = new SqueezeBoxNotificationListener(mac);
        listener.resetStopped();

        squeezeBoxServerHandler.registerSqueezeBoxPlayerListener(listener);
        squeezeBoxServerHandler.playPlaylistItem(mac, notificationMessagePlaylistsIndex);

        try {
            int notificationTimeout = squeezeBoxPlayerHandler.getNotificationTimeout();
            int timeoutCount = 0;

            logger.trace("Waiting up to {} s for stop...", notificationTimeout);
            while (!listener.isStopped()) {
                Thread.sleep(100);
                if (timeoutCount++ > notificationTimeout * 10) {
                    logger.debug("Notification message timed out after {} seconds", notificationTimeout);
                    throw new SqueezeBoxTimeoutException("Notification message timed out");
                }
            }
        } finally {
            squeezeBoxServerHandler.unregisterSqueezeBoxPlayerListener(listener);
        }
    }

    private void restorePlayerState() {
        logger.debug("Restoring player state");

        // Mute the player to prevent any noise during the transition to saved state
        // Don't wait for the volume acknowledge as there´s nothing to do about it at this point.
        squeezeBoxServerHandler.setVolume(mac, 0);

        if (playlistModified) {
            try {
                removeNotificationMessageFromPlaylist();
            } catch (InterruptedException | SqueezeBoxTimeoutException e) {
                // Not much we can do here except log it and continue on
                logger.debug("Exception while removing notification from playlist: {}", e.getMessage());
            }
        }

        // Resume playing saved playlist item.
        // Note that setting the time doesn't work for remote streams.
        squeezeBoxServerHandler.playPlaylistItem(mac, playerState.getPlaylistIndex());
        squeezeBoxServerHandler.setPlayingTime(mac, playerState.getPlayingTime());

        switch (playerState.getPlayState()) {
            case PLAY:
                logger.debug("Resuming last item playing");
                break;
            case PAUSE:
                /*
                 * If the player was paused, stop it. We stop it because the LMS
                 * doesn't respond to a pause command while it's processing the
                 * above 'playPlaylist item' command. The consequence of this is
                 * we lose the ability to resume local music from saved playing time.
                 */
                logger.debug("Stopping the player");
                squeezeBoxServerHandler.stop(mac);
                break;
            case STOP:
                logger.debug("Stopping the player");
                squeezeBoxServerHandler.stop(mac);
                break;
        }

        // Restore the saved volume level
        squeezeBoxServerHandler.setVolume(mac, playerState.getVolume());

        if (playerState.isShuffling()) {
            logger.debug("Restoring shuffle mode");
            squeezeBoxServerHandler.setShuffleMode(mac, playerState.getShuffle());
        }
        if (playerState.isRepeating()) {
            logger.debug("Restoring repeat mode");
            squeezeBoxServerHandler.setRepeatMode(mac, playerState.getRepeat());
        }
        if (playerState.isMuted()) {
            logger.debug("Re-muting the player");
            squeezeBoxServerHandler.mute(mac);
        }
        if (!playerState.isPoweredOn()) {
            logger.debug("Powering off the player");
            squeezeBoxServerHandler.powerOff(mac);
        }
    }
}
