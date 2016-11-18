/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch.internal;

/**
 * @author Christian Niessner - Initial contribution
 * @author Thomas Traunbauer
 */
public enum XMLHandlerState {
    INIT,
    Msg,
    MsgHeader,
    MsgBody,
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
    NowPlayingPlayStatus,
    NowPlayingStationLocation,
    NowPlayingStationName,
    NowPlayingTrack,
    Unprocessed, // unprocessed / ignored data
    UnprocessedNoTextExpected, // unprocessed / ignored data
    Updates,
    Volume,
    VolumeActual,
    VolumeMuteEnabled,
    Zone,
    ZoneMember,
    ZoneUpdated
}
