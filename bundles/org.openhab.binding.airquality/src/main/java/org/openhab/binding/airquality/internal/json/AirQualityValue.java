/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.airquality.internal.json;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Wrapper type around values reported by aqicn index values.
 *
 * @author Łukasz Dywicki - Initial contribution
 */
@NonNullByDefault
public class AirQualityValue {

    @SerializedName("v")
    private double value;

    public double getValue() {
        return value;
    }
}
