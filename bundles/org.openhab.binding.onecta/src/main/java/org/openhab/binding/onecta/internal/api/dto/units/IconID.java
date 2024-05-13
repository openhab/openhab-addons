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
package org.openhab.binding.onecta.internal.api.dto.units;

import com.google.gson.annotations.SerializedName;

/**
 * @author Alexander Drent - Initial contribution
 */
public class IconID {
    @SerializedName("settable")
    private boolean settable;
    @SerializedName("value")
    private Float value;
    @SerializedName("maxValue")
    private Float maxValue;
    @SerializedName("minValue")
    private Float minValue;
    @SerializedName("stepValue")
    private Float stepValue;
    @SerializedName("unit")
    private String unit;

    public boolean isSettable() {
        return settable;
    }

    public Float getValue() {
        return value;
    }

    public Float getMaxValue() {
        return maxValue;
    }

    public Float getMinValue() {
        return minValue;
    }

    public Float getStepValue() {
        return stepValue;
    }

    public String getUnit() {
        return unit;
    }
}
