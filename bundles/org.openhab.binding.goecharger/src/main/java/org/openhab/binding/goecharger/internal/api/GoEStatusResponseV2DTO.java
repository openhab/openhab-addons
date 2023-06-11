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
package org.openhab.binding.goecharger.internal.api;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link GoEStatusResponseV2DTO} class represents a json response from the
 * charger.
 *
 * @author Reinhard Plaim - Initial contribution
 */
public class GoEStatusResponseV2DTO extends GoEStatusResponseBaseDTO {
    @SerializedName("mod")
    public String version;

    @SerializedName("psm")
    public Integer phases;

    @SerializedName("trx")
    public Integer transaction;

    @SerializedName("alw")
    public Boolean allowCharging;

    @SerializedName("tma")
    public Double[] temperatures;

    @SerializedName("wh")
    public Double sessionChargeConsumption;

    @SerializedName("dwo")
    public Double sessionChargeConsumptionLimit;

    @SerializedName("frc")
    public Integer forceState;

    @SerializedName("nrg")
    public Double[] energy;
}
