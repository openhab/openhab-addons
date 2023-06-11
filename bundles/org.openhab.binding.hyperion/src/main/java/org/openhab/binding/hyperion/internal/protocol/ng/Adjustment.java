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
package org.openhab.binding.hyperion.internal.protocol.ng;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link Adjustment} is a POJO for an adjustment on the Hyperion.ng server.
 *
 * @author Daniel Walters - Initial contribution
 */
public class Adjustment {

    @SerializedName("backlightColored")
    private Boolean backlightColored;

    @SerializedName("backlightThreshold")
    private Integer backlightThreshold;

    @SerializedName("black")
    private List<Integer> black = null;

    @SerializedName("blue")
    private List<Integer> blue = null;

    @SerializedName("brightness")
    private Integer brightness;

    @SerializedName("brightnessCompensation")
    private Integer brightnessCompensation;

    @SerializedName("cyan")
    private List<Integer> cyan = null;

    @SerializedName("gammaBlue")
    @Expose
    private Double gammaBlue;

    @SerializedName("gammaGreen")
    private Double gammaGreen;

    @SerializedName("gammaRed")
    private Double gammaRed;

    @SerializedName("green")
    private List<Integer> green = null;

    @SerializedName("id")
    private String id;

    @SerializedName("magenta")
    private List<Integer> magenta = null;

    @SerializedName("red")
    private List<Integer> red = null;

    @SerializedName("white")
    private List<Integer> white = null;

    @SerializedName("yellow")
    private List<Integer> yellow = null;

    public Boolean getBacklightColored() {
        return backlightColored;
    }

    public void setBacklightColored(Boolean backlightColored) {
        this.backlightColored = backlightColored;
    }

    public Integer getBacklightThreshold() {
        return backlightThreshold;
    }

    public void setBacklightThreshold(Integer backlightThreshold) {
        this.backlightThreshold = backlightThreshold;
    }

    public List<Integer> getBlack() {
        return black;
    }

    public void setBlack(List<Integer> black) {
        this.black = black;
    }

    public List<Integer> getBlue() {
        return blue;
    }

    public void setBlue(List<Integer> blue) {
        this.blue = blue;
    }

    public Integer getBrightness() {
        return brightness;
    }

    public void setBrightness(Integer brightness) {
        this.brightness = brightness;
    }

    public Integer getBrightnessCompensation() {
        return brightnessCompensation;
    }

    public void setBrightnessCompensation(Integer brightnessCompensation) {
        this.brightnessCompensation = brightnessCompensation;
    }

    public List<Integer> getCyan() {
        return cyan;
    }

    public void setCyan(List<Integer> cyan) {
        this.cyan = cyan;
    }

    public Double getGammaBlue() {
        return gammaBlue;
    }

    public void setGammaBlue(Double gammaBlue) {
        this.gammaBlue = gammaBlue;
    }

    public Double getGammaGreen() {
        return gammaGreen;
    }

    public void setGammaGreen(Double gammaGreen) {
        this.gammaGreen = gammaGreen;
    }

    public Double getGammaRed() {
        return gammaRed;
    }

    public void setGammaRed(Double gammaRed) {
        this.gammaRed = gammaRed;
    }

    public List<Integer> getGreen() {
        return green;
    }

    public void setGreen(List<Integer> green) {
        this.green = green;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Integer> getMagenta() {
        return magenta;
    }

    public void setMagenta(List<Integer> magenta) {
        this.magenta = magenta;
    }

    public List<Integer> getRed() {
        return red;
    }

    public void setRed(List<Integer> red) {
        this.red = red;
    }

    public List<Integer> getWhite() {
        return white;
    }

    public void setWhite(List<Integer> white) {
        this.white = white;
    }

    public List<Integer> getYellow() {
        return yellow;
    }

    public void setYellow(List<Integer> yellow) {
        this.yellow = yellow;
    }
}
