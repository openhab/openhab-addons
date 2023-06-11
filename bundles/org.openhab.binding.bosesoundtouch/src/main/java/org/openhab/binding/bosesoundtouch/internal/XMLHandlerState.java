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
package org.openhab.binding.bosesoundtouch.internal;

/**
 * The {@link XMLHandlerState} class defines the XML States provided from Bose Soundtouch
 *
 * @author Christian Niessner - Initial contribution
 * @author Thomas Traunbauer - Initial contribution
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
    BassMin,
    BassMax,
    BassDefault,
    ContentItem,
    ContentItemItemName,
    ContentItemContainerArt,
    Group,
    GroupName,
    Components,
    Component,
    Info,
    InfoName,
    InfoType,
    InfoModuleType,
    InfoFirmwareVersion,
    Presets,
    Preset,
    MasterDeviceId,
    DeviceId,
    DeviceIp,
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
    Sources,
    BassCapabilities,
    BassAvailable,
}
