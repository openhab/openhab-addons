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
package org.openhab.binding.airquality.internal.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link AirQualityResponse} is the Java class used to map the JSON
 * response to the aqicn.org request.
 *
 * @author Kuba Wolanin - Initial contribution
 */
@NonNullByDefault
public class AirQualityResponse {

    public static enum ResponseStatus {
        NONE,
        @SerializedName("error")
        ERROR,
        @SerializedName("ok")
        OK;
    }

    private ResponseStatus status = ResponseStatus.NONE;
    private @NonNullByDefault({}) AirQualityData data;

    public ResponseStatus getStatus() {
        return status;
    }

    public AirQualityData getData() {
        return data;
    }
}
