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
package org.openhab.binding.boschindego.internal.dto.response;

import org.openhab.binding.boschindego.internal.dto.response.runtime.DeviceStateRuntimes;

import com.google.gson.annotations.SerializedName;

/**
 * Response after querying the device status.
 * 
 * @author Jacob Laursen - Initial contribution
 */
public class DeviceStateResponse {

    public int state;

    public int error;

    public boolean enabled;

    @SerializedName("map_update_available")
    public boolean mapUpdateAvailable;

    public int mowed;

    @SerializedName("mowmode")
    public long mowMode;

    public int xPos;

    public int yPos;

    /**
     * This is returned only for non-longpoll requests.
     */
    public DeviceStateRuntimes runtime;

    /**
     * This is returned only for longpoll requests.
     */
    public long charge;

    /**
     * This is returned only for longpoll requests.
     */
    public long operate;

    @SerializedName("mowed_ts")
    public long mowedTimestamp;

    @SerializedName("mapsvgcache_ts")
    public long mapSvgCacheTimestamp;

    @SerializedName("svg_xPos")
    public int svgXPos;

    @SerializedName("svg_yPos")
    public int svgYPos;

    @SerializedName("config_change")
    public boolean configChange;

    @SerializedName("mow_trig")
    public boolean mowTrigger;
}
