/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.nanoleaf.internal.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Represents the light panels controller information
 *
 * @author Martin Raepple - Initial contribution
 */
public class ControllerInfo {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("serialNo")
    @Expose
    private String serialNo;
    @SerializedName("manufacturer")
    @Expose
    private String manufacturer;
    @SerializedName("firmwareVersion")
    @Expose
    private String firmwareVersion;
    @SerializedName("model")
    @Expose
    private String model;
    @SerializedName("state")
    @Expose
    private State state;
    @SerializedName("effects")
    @Expose
    private Effects effects;
    @SerializedName("panelLayout")
    @Expose
    private PanelLayout panelLayout;
    @SerializedName("rhythm")
    @Expose
    private Rhythm rhythm;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Effects getEffects() {
        return effects;
    }

    public void setEffects(Effects effects) {
        this.effects = effects;
    }

    public PanelLayout getPanelLayout() {
        return panelLayout;
    }

    public void setPanelLayout(PanelLayout panelLayout) {
        this.panelLayout = panelLayout;
    }

    public Rhythm getRhythm() {
        return rhythm;
    }

    public void setRhythm(Rhythm rhythm) {
        this.rhythm = rhythm;
    }

}
