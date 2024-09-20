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
 * The {@link FPPPlaylist} is responsible for storing
 * the FPP JSON Status data.
 *
 * @author Scott Hanson - Initial contribution
 */
public class FPPPlaylist {
    /*
     * 
     * "current_playlist" :
     * {
     * "count" : "0",
     * "description" : "",
     * "index" : "0",
     * "playlist" : "",
     * "type" : ""
     * }
     * 
     */

    @SerializedName("playlist")
    @Expose
    public String playlist;

    @SerializedName("description")
    @Expose
    public String description;

    @SerializedName("count")
    @Expose
    public int count;

    @SerializedName("index")
    @Expose
    public int index;
}
