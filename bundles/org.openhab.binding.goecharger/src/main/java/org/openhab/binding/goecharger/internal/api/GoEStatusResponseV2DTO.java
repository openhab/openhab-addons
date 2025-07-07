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
package org.openhab.binding.goecharger.internal.api;

import static org.openhab.binding.goecharger.internal.api.GoEStatusV2ApiKeys.*;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link GoEStatusResponseV2DTO} class represents a json response from the
 * charger.
 *
 * @author Reinhard Plaim - Initial contribution
 */
public class GoEStatusResponseV2DTO extends GoEStatusResponseBaseDTO {

    @Deprecated
    @SerializedName("mod")
    public String version;

    @SerializedName(PSM)
    public Integer phases;

    @SerializedName(TRX)
    public Integer transaction;

    @SerializedName(ALW)
    public Boolean allowCharging;

    @SerializedName(TMA)
    public Double[] temperatures;

    @SerializedName(WH)
    public Double sessionChargeConsumption;

    @SerializedName(DWO)
    public Double sessionChargeConsumptionLimit;

    @SerializedName(FRC)
    public Integer forceState;

    @SerializedName(NRG)
    public Double[] energy;

    @SerializedName(AWP)
    public Double awattarMaxPrice;
}
