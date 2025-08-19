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
package org.openhab.binding.ondilo.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link LastMeasure} DTO for representing Ondilo LastMeasures.
 *
 * @author MikeTheTux - Initial contribution
 */
public class LastMeasure {
    /*
     * Example JSON representation:
     * {
     * "data_type": "temperature",
     * "value": 12.5,
     * "value_time": "2025-07-13 04:33:39",
     * "is_valid": true,
     * "exclusion_reason": null
     * }
     */
    @SerializedName("data_type")
    public String dataType;

    public double value;

    @SerializedName("value_time")
    public String valueTime;

    @SerializedName("is_valid")
    public boolean isValid;

    @SerializedName("exclusion_reason")
    public String exclusionReason;
}
