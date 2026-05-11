/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.peblar.internal;

import com.google.gson.annotations.SerializedName;

/**
 * @author Hilbrand Bouwkamp - Initial contribution
 */
class PeblarMeterDTO {

    @SerializedName("CurrentPhase1")
    public Long currentPhase1;

    @SerializedName("CurrentPhase2")
    public Long currentPhase2;

    @SerializedName("CurrentPhase3")
    public Long currentPhase3;

    @SerializedName("VoltagePhase1")
    public Integer voltagePhase1;

    @SerializedName("VoltagePhase2")
    public Integer voltagePhase2;

    @SerializedName("VoltagePhase3")
    public Integer voltagePhase3;

    @SerializedName("PowerPhase1")
    public Long powerPhase1;

    @SerializedName("PowerPhase2")
    public Long powerPhase2;

    @SerializedName("PowerPhase3")
    public Long powerPhase3;

    @SerializedName("PowerTotal")
    public Long powerTotal;

    /** Lifetime energy in watt-hours */
    @SerializedName("EnergyTotal")
    public Long energyTotal;

    /** Session energy in watt-hours */
    @SerializedName("EnergySession")
    public Long energySession;
}
