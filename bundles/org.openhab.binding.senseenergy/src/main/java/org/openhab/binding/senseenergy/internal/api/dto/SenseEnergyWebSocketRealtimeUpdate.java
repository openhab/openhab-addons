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

package org.openhab.binding.senseenergy.internal.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SenseEnergyWebSocketRealtimeUpdate } dto object for web socket realtime updates. Fields which are not used
 * have been commented out in order to save memory and processor bandwidth in the gson conversion.
 *
 * @author Jeff James - Initial contribution
 */
public class SenseEnergyWebSocketRealtimeUpdate {
    public float[] voltage;
    // public long frame;
    public SenseEnergyWebSocketDevice[] devices;
    // public float defaultCost;
    // public float[] channels;
    public float hz;
    public float w;
    // public float c;
    @SerializedName("solar_w")
    public float solarW;
    @SerializedName("grid_w")
    public float gridW;
    // @SerializedName("solar_c")
    // public float solarC;
    // @SerializedName("solar_pct")
    // public int solarPct;
}
