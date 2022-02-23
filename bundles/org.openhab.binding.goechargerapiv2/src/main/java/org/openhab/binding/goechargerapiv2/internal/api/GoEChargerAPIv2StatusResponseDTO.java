/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.goechargerapiv2.internal.api;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link GoEChargerAPIv2StatusResponseDTO} class represents a json response from the
 * charger.
 *
 * @author Reinhard Plaim - Initial contribution
 */
public class GoEChargerAPIv2StatusResponseDTO {
    @SerializedName("mod")
    public String version;

    @SerializedName("car")
    public Integer pwmSignal;

    @SerializedName("amp")
    public Integer maxCurrent;

    @SerializedName("psm")
    public Integer chargingPhases;

    @SerializedName("nrg")
    public Integer[] energy;

    @SerializedName("err")
    public Integer errorCode;

    @SerializedName("alw")
    public Boolean allowCharging;

    @SerializedName("cbl")
    public Integer cableEncoding;

    @SerializedName("tma")
    public Double[] temperature;

    @SerializedName("wh")
    public Long sessionChargeConsumption;

    @SerializedName("dwo")
    public Integer sessionChargeConsumptionLimit;

    @SerializedName("eto")
    public Long totalChargeConsumption;

    @SerializedName("fwv")
    public String firmware;
}
