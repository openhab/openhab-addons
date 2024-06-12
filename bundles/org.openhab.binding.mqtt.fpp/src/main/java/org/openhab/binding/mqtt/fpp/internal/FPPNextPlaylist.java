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
 * The {@link FPPNextPlaylist} is responsible for storing
 * the FPP JSON Next Playlist data.
 *
 * @author Scott Hanson - Initial contribution
 */
public class FPPNextPlaylist {
    /*
     * "nextPlaylist" :
     * {
     * "playlistName" : "No playlist scheduled.",
     * "scheduledStartTime" : 0,
     * "scheduledStartTimeStr" : ""
     * },
     */

    @SerializedName("playlistName")
    @Expose
    public String playlistName;

    @SerializedName("scheduledStartTimeStr")
    @Expose
    public String scheduledStartTimeStr;

    @SerializedName("scheduledStartTime")
    @Expose
    public int scheduledStartTime;
}
