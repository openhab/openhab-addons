/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
