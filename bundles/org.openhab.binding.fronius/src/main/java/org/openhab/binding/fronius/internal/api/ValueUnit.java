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
package org.openhab.binding.fronius.internal.api;

import org.openhab.binding.fronius.internal.math.KilowattConverter;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link InverterRealtimeResponse} is responsible for storing
 * a value
 *
 * @author Thomas Rokohl - Initial contribution
 */
public class ValueUnit {
    @SerializedName("Value")
    private double value;
    @SerializedName("Unit")
    private String unit;

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getUnit() {
        if (unit == null) {
            unit = "";
        }
        return unit;
    }

    public void setUnit(String unit) {
        this.setValue(KilowattConverter.convertTo(this.getValue(), this.getUnit(), unit));
        this.unit = unit;
    }
}
