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
package org.openhab.binding.lgthinq.lgservices.model.ac;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.model.Capability;

/**
 * The {@link ACCapability}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class ACCapability extends Capability {

    private Map<String, String> opMod = Collections.emptyMap();
    private Map<String, String> fanSpeed = Collections.emptyMap();

    private List<String> supportedOpMode = Collections.emptyList();
    private List<String> supportedFanSpeed = Collections.emptyList();
    private boolean isJetModeAvailable;
    private String coolJetModeCommandOn = "";
    private String coolJetModeCommandOff = "";

    public String getCoolJetModeCommandOff() {
        return coolJetModeCommandOff;
    }

    public void setCoolJetModeCommandOff(String coolJetModeCommandOff) {
        this.coolJetModeCommandOff = coolJetModeCommandOff;
    }

    public String getCoolJetModeCommandOn() {
        return coolJetModeCommandOn;
    }

    public void setCoolJetModeCommandOn(String coolJetModeCommandOn) {
        this.coolJetModeCommandOn = coolJetModeCommandOn;
    }

    public Map<String, String> getOpMod() {
        return opMod;
    }

    public void setOpMod(Map<String, String> opMod) {
        this.opMod = opMod;
    }

    public Map<String, String> getFanSpeed() {
        return fanSpeed;
    }

    public void setFanSpeed(Map<String, String> fanSpeed) {
        this.fanSpeed = fanSpeed;
    }

    public List<String> getSupportedOpMode() {
        return supportedOpMode;
    }

    public void setSupportedOpMode(List<String> supportedOpMode) {
        this.supportedOpMode = supportedOpMode;
    }

    public List<String> getSupportedFanSpeed() {
        return supportedFanSpeed;
    }

    public void setSupportedFanSpeed(List<String> supportedFanSpeed) {
        this.supportedFanSpeed = supportedFanSpeed;
    }

    public void setJetModeAvailable(boolean jetModeAvailable) {
        this.isJetModeAvailable = jetModeAvailable;
    }

    public boolean isJetModeAvailable() {
        return this.isJetModeAvailable;
    }
}
