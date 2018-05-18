/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
        return unit;
    }

    public void setUnit(String unit) {
        this.setValue(KilowattConverter.convertTo(this.getValue(), this.getUnit(), unit));
        this.unit = unit;
    }

}
