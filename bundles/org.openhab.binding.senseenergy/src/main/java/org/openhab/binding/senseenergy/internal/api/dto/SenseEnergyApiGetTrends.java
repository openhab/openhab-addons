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

import java.time.Instant;

import com.google.gson.annotations.SerializedName;

/**
 * {@link SenseEnergyApiGetTrends }
 *
 * @author Jeff James - Initial contribution
 */
public class SenseEnergyApiGetTrends {
    public int steps;
    public Instant start;
    public Instant end;
    public SenseEnergyApiGetTrendsTotals consumption;
    public SenseEnergyApiGetTrendsTotals production;
    @SerializedName("to_grid")
    public float toGridEnergy;
    @SerializedName("from_grid")
    public float fromGridEnergy;
    @SerializedName("to_grid_cost")
    public float toGridCost;
    @SerializedName("form_grid_cost")
    public float fromGridCost;
    @SerializedName("solar_powered")
    public float solarPowered;
    @SerializedName("net_production")
    public float netProduction;
    @SerializedName("production_pct")
    public float productionPercent;
}
