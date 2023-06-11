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
 * The {@link GoEStatusResponseBaseDTO} class represents a json response from the
 * charger.
 *
 * @author Reinhard Plaim - Initial contribution
 */
public class GoEStatusResponseBaseDTO {
    @SerializedName("car")
    public Integer pwmSignal;

    @SerializedName("amp")
    public Integer maxCurrent;

    @SerializedName("err")
    public Integer errorCode;

    @SerializedName("cbl")
    public Integer cableEncoding;

    @SerializedName("eto")
    public Long totalChargeConsumption;

    @SerializedName("fwv")
    public String firmware;
}
