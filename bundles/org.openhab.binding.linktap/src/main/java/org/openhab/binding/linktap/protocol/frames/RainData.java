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
package org.openhab.binding.linktap.protocol.frames;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link DismissAlertReq} defines the payload to represent rainfall data.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class RainData extends TLGatewayFrame {

    public RainData() {
    }

    /**
     * Defines the past rainfall [0] and future rainfull [1] measurements in mm
     */
    @SerializedName("rain")
    @Expose
    public double[] rainfallData = new double[] { 0.0, 0.0 };

    public void setPastRainfall(final double pastRainMM) {
        rainfallData[0] = pastRainMM;
    }

    public double getPastRainfall() {
        return rainfallData[0];
    }

    public void setFutureRainfall(final double futureRainMM) {
        rainfallData[1] = futureRainMM;
    }

    public double getFutureRainfall() {
        return rainfallData[1];
    }

    public Collection<ValidationError> getValidationErrors() {
        final Collection<ValidationError> errors = super.getValidationErrors();

        if (rainfallData.length != 2) {
            errors.add(new ValidationError("rain", "has a invalid number of parameters"));
        }

        return errors;
    }
}
