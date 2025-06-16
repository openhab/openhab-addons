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
 * The {@link GoEStatusResponseBaseDTO} class represents a json response from the
 * charger.
 *
 * @author Reinhard Plaim - Initial contribution
 */
public class GoEStatusResponseBaseDTO {
    @SerializedName(CAR)
    public Integer pwmSignal;

    @SerializedName(AMP)
    public Integer maxCurrent;

    @SerializedName(ERR)
    public Integer errorCode;

    @SerializedName(CBL)
    public Integer cableEncoding;

    @SerializedName(ETO)
    public Long totalChargeConsumption;

    @SerializedName(FWV)
    public String firmware;
}
