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
 * The {@link GoEStatusResponse} class represents a json response from the
 * charger.
 *
 * @author Samuel Brucksch - Initial contribution
 * @author Reinhard Plaim - move some properties to base DTO
 */
public class GoEStatusResponseDTO extends GoEStatusResponseBaseDTO {
    @SerializedName("version")
    public String version;

    @SerializedName("pha")
    public Integer phases;

    @SerializedName("ast")
    public Integer accessConfiguration;

    @SerializedName("alw")
    public Integer allowCharging;

    @SerializedName("tmp")
    public Integer temperature;

    @SerializedName("dwo")
    public Integer sessionChargeConsumptionLimit;

    @SerializedName("dws")
    public Long sessionChargeConsumption;

    @SerializedName("amx")
    public Integer maxCurrentTemporary;

    @SerializedName("nrg")
    public Integer[] energy;
}
