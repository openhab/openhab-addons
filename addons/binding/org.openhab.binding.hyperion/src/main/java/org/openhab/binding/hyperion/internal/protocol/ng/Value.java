/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hyperion.internal.protocol.ng;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Value} is a POJO for value information in the Hyperion.ng server.
 *
 * @author Daniel Walters - Initial contribution
 */
public class Value {

    @SerializedName("HEX")
    private List<String> hex = null;

    @SerializedName("HSL")
    private List<Double> hsl = null;

    @SerializedName("RGB")
    private List<Integer> rgb = null;

    public List<String> getHEX() {
        return hex;
    }

    public List<Double> getHSL() {
        return hsl;
    }

    public List<Integer> getRGB() {
        return rgb;
    }

    public void setHEX(List<String> hex) {
        this.hex = hex;
    }

    public void setHSL(List<Double> hsl) {
        this.hsl = hsl;
    }

    public void setRGB(List<Integer> rgb) {
        this.rgb = rgb;
    }

}
