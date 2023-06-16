/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.growatt.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link GrottValues} is a DTO containing inverter value fields received from the Grott application.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class GrottValues {
    public @Nullable Integer pvstatus;
    public @Nullable Integer pvpowerin;
    public @Nullable Integer pv1voltage;
    public @Nullable Integer pv1current;
    public @Nullable Integer pv1watt;
    public @Nullable Integer pv2voltage;
    public @Nullable Integer pv2current;
    public @Nullable Integer pv2watt;
    public @Nullable Integer pvpowerout;
    public @Nullable @SerializedName("pvfrequentie") Integer pvfrequency;
    public @Nullable Integer pvgridvoltage;
    public @Nullable Integer pvgridcurrent;
    public @Nullable Integer pvgridpower;
    public @Nullable Integer pvgridvoltage2;
    public @Nullable Integer pvgridcurrent2;
    public @Nullable Integer pvgridpower2;
    public @Nullable Integer pvgridvoltage3;
    public @Nullable Integer pvgridcurrent3;
    public @Nullable Integer pvgridpower3;
    public @Nullable Integer pvenergytoday;
    public @Nullable Integer pvenergytotal;
    public @Nullable Integer totworktime;
    public @Nullable Integer epv1today;
    public @Nullable Integer epv1total;
    public @Nullable Integer epv2today;
    public @Nullable Integer epv2total;
    public @Nullable Integer epvtotal;
    public @Nullable Integer pvtemperature;
    public @Nullable Integer pvipmtemperature;
    public @Nullable @SerializedName("pvboottemperature") Integer pvboosttemperature;
    public @Nullable Integer temp4;
    public @Nullable Integer Vac_RS;
    public @Nullable Integer Vac_ST;
    public @Nullable Integer Vac_TR;
    public @Nullable Integer uwBatVolt_DSP;
    public @Nullable Integer pbusvolt;
    public @Nullable Integer nbusvolt;
    public @Nullable Integer eacCharToday;
    public @Nullable Integer eacCharTotal;
    public @Nullable Integer ebatDischarToday;
    public @Nullable Integer ebatDischarTotal;
    public @Nullable Integer eacDischarToday;
    public @Nullable Integer eacDischarTotal;
    public @Nullable Integer ACCharCurr;
    public @Nullable Integer ACDischarWatt;
    public @Nullable Integer ACDischarVA;
    public @Nullable Integer BatDischarWatt;
    public @Nullable Integer BatDischarVA;
    public @Nullable Integer BatWatt;
}
