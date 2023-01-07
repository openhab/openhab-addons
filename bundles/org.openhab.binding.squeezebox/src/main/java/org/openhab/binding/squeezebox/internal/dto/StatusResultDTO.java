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
package org.openhab.binding.squeezebox.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link StatusResultDTO} represents the result of a status request.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class StatusResultDTO {

    /**
     * Remote metadata information, including button definitions/redefinitions.
     */
    @SerializedName("remoteMeta")
    public RemoteMetaDTO remoteMeta;

    /**
     * These remaining fields are currently unused by the binding,
     * as they also are returned by the Command Line Interface (CLI).
     */
    @SerializedName("current_title")
    public String currentTitle;

    @SerializedName("digital_volume_control")
    public Integer digitalVolumeControl;

    @SerializedName("duration")
    public Double duration;

    @SerializedName("mixer volume")
    public Integer mixerVolume;

    @SerializedName("player_connected")
    public Integer playerConnected;

    @SerializedName("player_ip")
    public String playerIpAddress;

    @SerializedName("player_name")
    public String playerName;

    @SerializedName("playlist mode")
    public String playlistMode;

    @SerializedName("playlist repeat")
    public Integer playlistRepeat;

    @SerializedName("playlist shuffle")
    public Integer playlistShuffle;

    @SerializedName("playlist_cur_index")
    public String playListCurrentIndex;

    @SerializedName("playlist_timestamp")
    public String playlistTimestamp;

    @SerializedName("playlist_tracks")
    public Integer playlistTracks;

    @SerializedName("power")
    public String power;

    @SerializedName("rate")
    public String rate;

    @SerializedName("remote")
    public String remote;

    @SerializedName("repeating_stream")
    public Integer repeatingStream;

    @SerializedName("seq_no")
    public Integer sequenceNumber;

    @SerializedName("signalstrength")
    public Integer signalStrength;

    @SerializedName("time")
    public String time;
}
