/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.fpp.internal;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link FPPStstus} is responsible for storing
 * the FPP JSON Status data.
 *
 * @author Scott Hanson - Initial contribution
 */
public class FPPStatus {
    /*
     * {
     * "MQTT" :
     * {
     * "configured" : true,
     * "connected" : true
     * },
     * "bridging" : false,
     * "current_playlist" :
     * {
     * "count" : "0",
     * "description" : "",
     * "index" : "0",
     * "playlist" : "",
     * "type" : ""
     * },
     * "current_sequence" : "",
     * "current_song" : "",
     * "dateStr" : "Sun Jan  7",
     * "fppd" : "running",
     * "mode" : 2,
     * "mode_name" : "player",
     * "multisync" : true,
     * "next_playlist" :
     * {
     * "playlist" : "No playlist scheduled.",
     * "start_time" : ""
     * },
     * "repeat_mode" : "0",
     * "scheduler" :
     * {
     * "enabled" : 1,
     * "nextPlaylist" :
     * {
     * "playlistName" : "No playlist scheduled.",
     * "scheduledStartTime" : 0,
     * "scheduledStartTimeStr" : ""
     * },
     * "status" : "idle"
     * },
     * "seconds_played" : "0",
     * "seconds_remaining" : "0",
     * "sensors" :
     * [
     * {
     * "formatted" : "55.0",
     * "label" : "CPU: ",
     * "postfix" : "",
     * "prefix" : "",
     * "value" : 55.017000000000003,
     * "valueType" : "Temperature"
     * }
     * ],
     * "status" : 0,
     * "status_name" : "idle",
     * "time" : "Sun Jan 07 19:15:25 EST 2024",
     * "timeStr" : "07:15 PM",
     * "timeStrFull" : "07:15:25 PM",
     * "time_elapsed" : "00:00",
     * "time_remaining" : "00:00",
     * "uptime" : "2 days, 01:30:04",
     * "uptimeDays" : 2.0625462962962962,
     * "uptimeHours" : 1.5011111111111111,
     * "uptimeMinutes" : 30.066666666666666,
     * "uptimeSeconds" : 4,
     * "uptimeStr" : "2 days, 1 hours, 30 minutes, 4 seconds",
     * "uptimeTotalSeconds" : 178204,
     * "uuid" : "M1-10000000fd93cfe5",
     * "volume" : 48
     * }
     * 
     */
    @SerializedName("status")
    @Expose
    public int status;

    @SerializedName("status_name")
    @Expose
    public String status_name;

    @SerializedName("mode_name")
    @Expose
    public String mode_name;

    @SerializedName("current_sequence")
    @Expose
    public String current_sequence;

    @SerializedName("current_song")
    @Expose
    public String current_song;

    @SerializedName("time_elapsed")
    @Expose
    public String time_elapsed;

    @SerializedName("uptimeTotalSeconds")
    @Expose
    public long uptimeTotalSeconds;

    @SerializedName("seconds_played")
    @Expose
    public int seconds_played;

    @SerializedName("seconds_remaining")
    @Expose
    public int seconds_remaining;

    @SerializedName("volume")
    @Expose
    public int volume;

    @SerializedName("uuid")
    @Expose
    public String uuid;

    @SerializedName("multisync")
    @Expose
    public boolean multisync;

    @SerializedName("current_playlist")
    @Expose
    public FPPPlaylist current_playlist;

    @SerializedName("bridging")
    @Expose
    public boolean bridging;

    @SerializedName("scheduler")
    @Expose
    public FPPScheduler scheduler;
}
