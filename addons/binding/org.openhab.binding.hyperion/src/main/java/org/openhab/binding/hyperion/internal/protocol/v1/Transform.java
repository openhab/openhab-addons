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
 * The {@link Transform} is a POJO for a transformation on the Hyperion server.
 *
 * @author Daniel Walters - Initial contribution
 */
public class Transform {

    @SerializedName("blacklevel")
    private List<Double> blacklevel = null;

    @SerializedName("gamma")
    private List<Double> gamma = null;

    @SerializedName("id")
    private String id;

    @SerializedName("luminanceGain")
    private Double luminanceGain;

    @SerializedName("luminanceMinimum")
    private Double luminanceMinimum;

    @SerializedName("saturationGain")
    private Double saturationGain;

    @SerializedName("saturationLGain")
    private Double saturationLGain;

    @SerializedName("threshold")
    private List<Double> threshold = null;

    @SerializedName("valueGain")
    private Double valueGain;

    @SerializedName("whitelevel")
    private List<Double> whitelevel = null;

    public List<Double> getBlacklevel() {
        return blacklevel;
    }

    public void setBlacklevel(List<Double> blacklevel) {
        this.blacklevel = blacklevel;
    }

    public List<Double> getGamma() {
        return gamma;
    }

    public void setGamma(List<Double> gamma) {
        this.gamma = gamma;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getLuminanceGain() {
        return luminanceGain;
    }

    public void setLuminanceGain(Double luminanceGain) {
        this.luminanceGain = luminanceGain;
    }

    public Double getLuminanceMinimum() {
        return luminanceMinimum;
    }

    public void setLuminanceMinimum(Double luminanceMinimum) {
        this.luminanceMinimum = luminanceMinimum;
    }

    public Double getSaturationGain() {
        return saturationGain;
    }

    public void setSaturationGain(Double saturationGain) {
        this.saturationGain = saturationGain;
    }

    public Double getSaturationLGain() {
        return saturationLGain;
    }

    public void setSaturationLGain(Double saturationLGain) {
        this.saturationLGain = saturationLGain;
    }

    public List<Double> getThreshold() {
        return threshold;
    }

    public void setThreshold(List<Double> threshold) {
        this.threshold = threshold;
    }

    public Double getValueGain() {
        return valueGain;
    }

    public void setValueGain(Double valueGain) {
        this.valueGain = valueGain;
    }

    public List<Double> getWhitelevel() {
        return whitelevel;
    }

    public void setWhitelevel(List<Double> whitelevel) {
        this.whitelevel = whitelevel;
    }

}
