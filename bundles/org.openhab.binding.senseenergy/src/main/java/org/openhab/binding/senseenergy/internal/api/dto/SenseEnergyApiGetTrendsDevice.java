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
 * {@link SenseEnergyApiGetTrendsDevice }
 *
 * @author Jeff James - Initial contribution
 */
public class SenseEnergyApiGetTrendsDevice {
    public String id;
    public String name;
    public String icon;
    SenseEnergyApiDeviceTags tags;
    @SerializedName("history")
    public float[] historyEnergy;
    @SerializedName("avgw")
    public float averagePower;
    @SerializedName("total_kwh")
    public float totalEnergy;
    @SerializedName("total_cost")
    public float totalCost;
    @SerializedName("pct")
    public float percent;
    @SerializedName("cost_history")
    public float[] historyCost;
}
