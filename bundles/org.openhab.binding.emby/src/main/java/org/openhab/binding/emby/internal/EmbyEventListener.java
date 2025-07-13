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
package org.openhab.binding.emby.internal;

import java.util.EventListener;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.emby.internal.model.EmbyPlayStateModel;

/**
 * Listener interface to receive status updates from an {@link EmbyConnection}.
 * Implementations of this interface will be notified of connection changes,
 * playback events, and metadata updates from the Emby server.
 *
 * @author Zachary Christiansen - Initial Contribution
 */
@NonNullByDefault
public interface EmbyEventListener extends EventListener {

    /**
     * Enumeration of possible player states reported by the Emby server.
     */
    public enum EmbyState {
        /** Playback has started or resumed. */
        PLAY,
        /** Playback is paused. */
        PAUSE,
        /** Playback has reached the end of media. */
        END,
        /** Playback has been stopped. */
        STOP,
        /** Playback is rewinding. */
        REWIND,
        /** Playback is fast-forwarding. */
        FASTFORWARD
    }

    /**
     * Enumeration of playlist modifications events reported by the Emby server.
     */
    public enum EmbyPlaylistState {
        /** Item(s) are about to be added to the playlist. */
        ADD,
        /** Item(s) have been added to the playlist. */
        ADDED,
        /** Item(s) are about to be inserted into the playlist. */
        INSERT,
        /** Item(s) are about to be removed from the playlist. */
        REMOVE,
        /** Item(s) have been removed from the playlist. */
        REMOVED,
        /** The playlist has been cleared. */
        CLEAR
    }

    /**
     * Called when the connection state to the Emby server changes.
     *
     * @param connected true if connected to the server, false otherwise
     */
    void updateConnectionState(boolean connected);

    /**
     * Called when the screen saver state changes.
     *
     * @param screenSaveActive true if the screen saver is active, false otherwise
     */
    void updateScreenSaverState(boolean screenSaveActive);

    /**
     * Called when the player state changes (play, pause, stop, etc.).
     *
     * @param state new playback state
     */
    void updatePlayerState(EmbyState state);

    /**
     * Called when the media title changes.
     *
     * @param title the current title of the media
     */
    void updateTitle(String title);

    /**
     * Called when the show or series title changes.
     *
     * @param title the current title of the show or series
     */
    void updateShowTitle(String title);

    /**
     * Called when the media type changes (e.g., Movie, Episode, Song).
     *
     * @param mediaType the type of media currently playing
     */
    void updateMediaType(String mediaType);

    /**
     * Called to report the current playback position.
     *
     * @param currentTime playback position in milliseconds
     */
    void updateCurrentTime(long currentTime);

    /**
     * Called to report the total duration of the media.
     *
     * @param duration total duration in milliseconds
     */
    void updateDuration(long duration);

    /**
     * Called when the primary image URL (e.g., cover art) changes.
     *
     * @param imageURL URL of the new primary image
     */
    void updatePrimaryImageURL(String imageURL);

    /**
     * Generic handler for play state change events, providing detailed model data.
     *
     * @param playstate model with detailed playback state information
     * @param hostname host name of the Emby server
     * @param embyport port number used for the Emby connection
     */
    void handleEvent(EmbyPlayStateModel playstate, String hostname, int embyport);
}
