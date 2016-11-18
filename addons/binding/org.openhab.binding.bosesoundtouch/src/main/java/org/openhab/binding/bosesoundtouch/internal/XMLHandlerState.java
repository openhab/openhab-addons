package org.openhab.binding.bosesoundtouch.internal;

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
