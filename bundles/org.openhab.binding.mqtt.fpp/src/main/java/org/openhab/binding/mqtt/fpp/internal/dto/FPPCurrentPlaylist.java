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
 * The {@link FPPCurrentPlaylist} is responsible for storing
 * the FPP JSON Current Playlist data.
 *
 * @author Scott Hanson - Initial contribution
 */
public class FPPCurrentPlaylist {
    @SerializedName("playlistName")
    @Expose
    public String playlistName;

    @SerializedName("scheduledStartTimeStr")
    @Expose
    public String scheduledStartTimeStr;

    @SerializedName("scheduledEndTimeStr")
    @Expose
    public String scheduledEndTimeStr;

    @SerializedName("stopTypeStr")
    @Expose
    public String stopTypeStr;
}
