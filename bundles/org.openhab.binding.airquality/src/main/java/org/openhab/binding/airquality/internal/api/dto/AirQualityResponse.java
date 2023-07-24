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
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link AirQualityResponse} is the Java class used to map the JSON
 * response to the aqicn.org request.
 *
 * @author Kuba Wolanin - Initial contribution
 */
@NonNullByDefault
public class AirQualityResponse extends ResponseRoot {

    private @Nullable AirQualityData data;

    public @Nullable AirQualityData getData() {
        return data;
    }

    private ResponseStatus getStatus() {
        AirQualityData localData = data;
        return status == ResponseStatus.OK && localData != null && localData.status == ResponseStatus.OK
                ? ResponseStatus.OK
                : ResponseStatus.ERROR;
    }

    public String getErrorMessage() {
        if (getStatus() != ResponseStatus.OK) {
            String localMsg = msg;
            if (localMsg != null) {
                return localMsg;
            } else {
                AirQualityData localData = data;
                if (localData != null) {
                    localMsg = localData.msg;
                    if (localMsg != null) {
                        return localMsg;
                    } else {
                        return "Unknown error";
                    }
                } else {
                    return "No data provided";
                }
            }
        }
        return "";
    }
}
