/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.openweathermap.internal.dto.onecall;

import com.google.gson.annotations.SerializedName;

/**
 * Holds the data from the <code>rain</code> and <code>snow</code> object of the JSON response of the One Call APIs.
 *
 * @author Wolfgang Klimt - Initial contribution
 */
public class Precipitation {
    @SerializedName("1h")
    private double oneHour;

    public double get1h() {
        return oneHour;
    }
}
