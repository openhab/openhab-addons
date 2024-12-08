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
package org.openhab.binding.tesla.internal.protocol.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link GUIState} is a datastructure to capture
 * variables sent by the Tesla Vehicle
 *
 * @author Karel Goderis - Initial contribution
 */
public class GUIState {

    @SerializedName("gui_24_hour_time")
    public boolean gui24HourTime;
    @SerializedName("gui_charge_rate_units")
    public String guiChargeRateUnits;
    @SerializedName("gui_distance_units")
    public String guiDistanceUnits;
    @SerializedName("gui_range_display")
    public String guiRangeDisplay;
    @SerializedName("gui_temperature_units")
    public String guiTemperatureUnits;
    @SerializedName("show_range_units")
    public boolean showRangeUnits;
    @SerializedName("timestamp")

    public Long timestamp;

    public GUIState() {
    }
}
