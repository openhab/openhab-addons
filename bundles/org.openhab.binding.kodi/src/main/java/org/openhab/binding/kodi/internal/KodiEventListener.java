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
package org.openhab.binding.kodi.internal;

import java.util.EventListener;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.kodi.internal.model.KodiAudioStream;
import org.openhab.binding.kodi.internal.model.KodiSubtitle;
import org.openhab.binding.kodi.internal.model.KodiSystemProperties;
import org.openhab.binding.kodi.internal.protocol.KodiConnection;
import org.openhab.core.library.types.RawType;

/**
 * Interface which has to be implemented by a class in order to get status
 * updates from a {@link KodiConnection}
 *
 * @author Paul Frank - Initial contribution
 * @author Christoph Weitkamp - Added channels for opening PVR TV or Radio streams
 * @author Christoph Weitkamp - Improvements for playing audio notifications
 */
public interface KodiEventListener extends EventListener {
    enum KodiState {
        PLAY,
        PAUSE,
        END,
        STOP,
        REWIND,
        FASTFORWARD
    }

    enum KodiPlaylistState {
        ADD,
        ADDED,
        INSERT,
        REMOVE,
        REMOVED,
        CLEAR
    }

    void updateConnectionState(boolean connected);

    void updateScreenSaverState(boolean screenSaverActive);

    void updateInputRequestedState(boolean inputRequested);

    void updatePlaylistState(KodiPlaylistState playlistState);

    void updateVolume(int volume);

    void updatePlayerState(KodiState state);

    void updateMuted(boolean muted);

    void updateMediaID(int mediaid);

    void updateUniqueIDDouban(String uniqueid);

    void updateUniqueIDImdb(String uniqueid);

    void updateUniqueIDTmdb(String uniqueid);

    void updateUniqueIDImdbtvshow(String uniqueid);

    void updateUniqueIDTmdbtvshow(String uniqueid);

    void updateUniqueIDTmdbepisode(String uniqueid);

    void updateTitle(String title);

    void updateOriginalTitle(String originaltitle);

    void updateShowTitle(String title);

    void updateAlbum(String album);

    void updateArtistList(List<String> artistList);

    void updateMediaType(String mediaType);

    void updateGenreList(List<String> genreList);

    void updatePVRChannel(String channel);

    void updateThumbnail(@Nullable RawType thumbnail);

    void updateFanart(@Nullable RawType fanart);

    void updateAudioStreamOptions(List<KodiAudioStream> audioStreamList);

    void updateAudioCodec(String codec);

    void updateAudioName(String name);

    void updateAudioIndex(int index);

    void updateAudioChannels(int channels);

    void updateAudioLanguage(String language);

    void updateVideoCodec(String codec);

    void updateVideoIndex(int index);

    void updateVideoWidth(int width);

    void updateVideoHeight(int height);

    void updateSubtitleOptions(List<KodiSubtitle> subtitleList);

    void updateSubtitleEnabled(boolean enabled);

    void updateSubtitleIndex(int index);

    void updateSubtitleName(String name);

    void updateSubtitleLanguage(String language);

    void updateCurrentTime(long currentTime);

    void updateCurrentTimePercentage(double currentTimePercentage);

    void updateDuration(long duration);

    void updateSystemProperties(@Nullable KodiSystemProperties systemProperties);

    void updateEpisode(int episode);

    void updateSeason(int season);

    void updateMediaFile(String mediafile);

    void updateRating(double rating);

    void updateUserRating(double rating);

    void updateMpaa(String mpaa);

    void updateCurrentProfile(String profile);
}
