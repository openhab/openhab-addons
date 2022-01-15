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

import java.util.List;

import org.openhab.binding.squeezebox.internal.model.Favorite;

/**
 * @author Markus Wolters - Initial contribution
 * @author Ben Jones - ?
 * @author Dan Cunningham - OH2 port
 * @author Mark Hilbush - Added durationEvent
 * @author Mark Hilbush - Added event to update favorites list
 */
public interface SqueezeBoxPlayerEventListener {

    void playerAdded(SqueezeBoxPlayer player);

    void powerChangeEvent(String mac, boolean power);

    void modeChangeEvent(String mac, String mode);

    /**
     * Reports a new absolute volume for a given player.
     *
     * @param mac
     * @param volume
     */
    void absoluteVolumeChangeEvent(String mac, int volume);

    /**
     * Reports a relative volume change for a given player.
     *
     * @param mac
     * @param volumeChange
     */
    void relativeVolumeChangeEvent(String mac, int volumeChange);

    void muteChangeEvent(String mac, boolean mute);

    void currentPlaylistIndexEvent(String mac, int index);

    void currentPlayingTimeEvent(String mac, int time);

    void durationEvent(String mac, int duration);

    void numberPlaylistTracksEvent(String mac, int track);

    void currentPlaylistShuffleEvent(String mac, int shuffle);

    void currentPlaylistRepeatEvent(String mac, int repeat);

    void titleChangeEvent(String mac, String title);

    void albumChangeEvent(String mac, String album);

    void artistChangeEvent(String mac, String artist);

    void coverArtChangeEvent(String mac, String coverArtUrl);

    void yearChangeEvent(String mac, String year);

    void genreChangeEvent(String mac, String genre);

    void remoteTitleChangeEvent(String mac, String title);

    void irCodeChangeEvent(String mac, String ircode);

    void updateFavoritesListEvent(List<Favorite> favorites);

    void sourceChangeEvent(String mac, String source);

    void buttonsChangeEvent(String mac, String likeCommand, String unlikeCommand);

    void connectedStateChangeEvent(String mac, boolean connected);
}
