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
package org.openhab.binding.hyperion.internal.protocol.v1;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ActiveLedColor} is a POJO for an active LED color on the Hyperion server.
 *
 * @author Daniel Walters - Initial contribution
 */
public class ActiveLedColor {

    @SerializedName("HEX Value")
    private List<String> hexValue = null;

    @SerializedName("HSL Value")
    private List<Double> hslValue = null;

    @SerializedName("RGB Value")
    private List<Integer> rgbValue = null;

    public List<String> getHEXValue() {
        return hexValue;
    }

    public void setHEXValue(List<String> hEXValue) {
        this.hexValue = hEXValue;
    }

    public List<Double> getHSLValue() {
        return hslValue;
    }

    public void setHSLValue(List<Double> hSLValue) {
        this.hslValue = hSLValue;
    }

    public List<Integer> getRGBValue() {
        return rgbValue;
    }

    public void setRGBValue(List<Integer> rGBValue) {
        this.rgbValue = rGBValue;
    }
}
