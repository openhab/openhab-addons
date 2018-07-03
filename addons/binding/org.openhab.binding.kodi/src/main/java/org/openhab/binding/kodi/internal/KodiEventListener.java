/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kodi.internal;

import java.util.EventListener;
import java.util.List;

import org.eclipse.smarthome.core.library.types.RawType;
import org.openhab.binding.kodi.internal.protocol.KodiConnection;

/**
 * Interface which has to be implemented by a class in order to get status
 * updates from a {@link KodiConnection}
 *
 * @author Paul Frank - Initial contribution
 * @author Christoph Weitkamp - Added channels for opening PVR TV or Radio streams
 * @author Christoph Weitkamp - Improvements for playing audio notifications
 */
public interface KodiEventListener extends EventListener {
    public enum KodiState {
        PLAY,
        PAUSE,
        END,
        STOP,
        REWIND,
        FASTFORWARD
    }

    public enum KodiPlaylistState {
        ADD,
        ADDED,
        INSERT,
        REMOVE,
        REMOVED,
        CLEAR
    }

    void updateConnectionState(boolean connected);

    void updateScreenSaverState(boolean screenSaveActive);

    void updatePlaylistState(KodiPlaylistState playlistState);

    void updateVolume(int volume);

    void updatePlayerState(KodiState state);

    void updateMuted(boolean muted);

    void updateTitle(String title);

    void updateShowTitle(String title);

    void updateAlbum(String album);

    void updateArtistList(List<String> artistList);

    void updateMediaType(String mediaType);

    void updateGenreList(List<String> genreList);

    void updatePVRChannel(final String channel);

    void updateThumbnail(RawType thumbnail);

    void updateFanart(RawType fanart);

    void updateCurrentTime(long currentTime);

    void updateCurrentTimePercentage(double currentTimePercentage);

    void updateDuration(long duration);
}
