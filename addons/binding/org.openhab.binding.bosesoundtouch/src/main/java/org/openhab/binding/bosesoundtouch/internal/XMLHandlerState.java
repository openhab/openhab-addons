/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch.internal;

/**
 * The {@link XMLHandlerState} class defines the XML States provided from Bose Soundtouch
 *
 * @author Christian Niessner - Initial contribution
 * @author Thomas Traunbauer
 */
public enum XMLHandlerState {
    INIT,
    Msg,
    MsgHeader,
    MsgBody,
    Bass,
    BassActual,
    BassTarget,
    BassUpdated,
    ContentItem,
    ContentItemItemName,
    Info,
    InfoName,
    InfoType,
    Presets,
    Preset,
    NowPlaying,
    NowPlayingAlbum,
    NowPlayingArt,
    NowPlayingArtist,
    NowPlayingDescription,
    NowPlayingGenre,
    NowPlayingPlayStatus,
    NowPlayingRateEnabled,
    NowPlayingSkipEnabled,
    NowPlayingSkipPreviousEnabled,
    NowPlayingStationLocation,
    NowPlayingStationName,
    NowPlayingTrack,
    Unprocessed, // unprocessed / ignored data
    UnprocessedNoTextExpected, // unprocessed / ignored data
    Updates,
    Volume,
    VolumeActual,
    VolumeTarget,
    VolumeUpdated,
    VolumeMuteEnabled,
    Zone,
    ZoneMember,
    ZoneUpdated,
}
