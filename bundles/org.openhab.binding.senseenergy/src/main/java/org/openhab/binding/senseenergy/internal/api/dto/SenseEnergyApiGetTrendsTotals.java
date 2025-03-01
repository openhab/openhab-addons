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
 * {@link SenseEnergyApiGetTrendsTotals }
 *
 * @author Jeff James - Initial contribution
 */
public class SenseEnergyApiGetTrendsTotals {
    @SerializedName("total")
    public float totalPower;
    @SerializedName("totals")
    public float[] totalsPower;
    public SenseEnergyApiGetTrendsDevice[] devices;
    @SerializedName("total_cost")
    public float totalCost;
    @SerializedName("total_costs")
    public float[] totalCosts;
}
