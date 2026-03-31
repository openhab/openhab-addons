/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.linkplay.internal.client.http.dto;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * Model for the JSON returned by /EQGetBand.
 * 
 * @author Dan Cunningham - Initial contribution
 */
public class EQBandResponse {

    public String status;

    public String sourceName;

    @SerializedName("EQStat")
    public String eqStat;

    /** Preset/EQ name (e.g. "Rock") */
    @SerializedName("Name")
    public String name;

    @SerializedName("pluginURI")
    public String pluginUri;

    public String channelMode;

    @SerializedName("EQBand")
    public List<EQBandEntry> bands;

    /** Single 10-band EQ point */
    public static class EQBandEntry {
        public int index;
        public String paramName;
        public int value;
    }
}
