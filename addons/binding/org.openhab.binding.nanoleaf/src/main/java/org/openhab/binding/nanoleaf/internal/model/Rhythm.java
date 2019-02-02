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
 * Represents rhythm module settings
 *
 * @author Martin Raepple - Initial contribution
 */
public class Rhythm {

    @SerializedName("rhythmConnected")
    @Expose
    private Boolean rhythmConnected;
    @SerializedName("rhythmActive")
    @Expose
    private Boolean rhythmActive;
    @SerializedName("rhythmId")
    @Expose
    private Integer rhythmId;
    @SerializedName("hardwareVersion")
    @Expose
    private String hardwareVersion;
    @SerializedName("firmwareVersion")
    @Expose
    private String firmwareVersion;
    @SerializedName("auxAvailable")
    @Expose
    private Boolean auxAvailable;
    @SerializedName("rhythmMode")
    @Expose
    private Integer rhythmMode;
    @SerializedName("rhythmPos")
    @Expose
    private RhythmPos rhythmPos;

    public Boolean getRhythmConnected() {
        return rhythmConnected;
    }

    public void setRhythmConnected(Boolean rhythmConnected) {
        this.rhythmConnected = rhythmConnected;
    }

    public Boolean getRhythmActive() {
        return rhythmActive;
    }

    public void setRhythmActive(Boolean rhythmActive) {
        this.rhythmActive = rhythmActive;
    }

    public Integer getRhythmId() {
        return rhythmId;
    }

    public void setRhythmId(Integer rhythmId) {
        this.rhythmId = rhythmId;
    }

    public String getHardwareVersion() {
        return hardwareVersion;
    }

    public void setHardwareVersion(String hardwareVersion) {
        this.hardwareVersion = hardwareVersion;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public Boolean getAuxAvailable() {
        return auxAvailable;
    }

    public void setAuxAvailable(Boolean auxAvailable) {
        this.auxAvailable = auxAvailable;
    }

    public Integer getRhythmMode() {
        return rhythmMode;
    }

    public void setRhythmMode(Integer rhythmMode) {
        this.rhythmMode = rhythmMode;
    }

    public RhythmPos getRhythmPos() {
        return rhythmPos;
    }

    public void setRhythmPos(RhythmPos rhythmPos) {
        this.rhythmPos = rhythmPos;
    }

}
