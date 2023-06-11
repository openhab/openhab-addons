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
package org.openhab.binding.somneo.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents the sensor state from the API.
 *
 * @author Michael Myrcik - Initial contribution
 */
@NonNullByDefault
public class SensorData {

    @SerializedName("mslux")
    private float currentIlluminance;

    @SerializedName("mstmp")
    private float currentTemperature;

    @SerializedName("msrhu")
    private float currentHumidity;

    @SerializedName("mssnd")
    private int currentNoise;

    public State getCurrentIlluminance() {
        return new QuantityType<>(currentIlluminance, Units.LUX);
    }

    public State getCurrentTemperature() {
        return new QuantityType<>(currentTemperature, SIUnits.CELSIUS);
    }

    public State getCurrentHumidity() {
        return new QuantityType<>(currentHumidity, Units.PERCENT);
    }

    public State getCurrentNoise() {
        return new QuantityType<>(currentNoise, Units.DECIBEL);
    }
}
