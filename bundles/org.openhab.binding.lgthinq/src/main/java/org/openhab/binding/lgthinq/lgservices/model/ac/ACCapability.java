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
import org.openhab.binding.lgthinq.lgservices.model.AbstractCapability;

/**
 * The {@link ACCapability}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class ACCapability extends AbstractCapability {

    private Map<String, String> opMod = Collections.emptyMap();
    private Map<String, String> fanSpeed = Collections.emptyMap();

    private List<String> supportedOpMode = Collections.emptyList();
    private List<String> supportedFanSpeed = Collections.emptyList();
    private boolean isJetModeAvailable;
    private boolean isAutoDryModeAvailable;
    private boolean isEnergySavingAvailable;
    private boolean isAirCleanAvailable;

    private boolean isFanSpeedAvailable;
    private String coolJetModeCommandOn = "";
    private String coolJetModeCommandOff = "";

    private String autoDryModeCommandOn = "";
    private String autoDryModeCommandOff = "";

    private String energySavingModeCommandOn = "";
    private String energySavingModeCommandOff = "";

    private String airCleanModeCommandOn = "";
    private String airCleanModeCommandOff = "";

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

    public boolean isAutoDryModeAvailable() {
        return isAutoDryModeAvailable;
    }

    public void setAutoDryModeAvailable(boolean autoDryModeAvailable) {
        isAutoDryModeAvailable = autoDryModeAvailable;
    }

    public boolean isEnergySavingAvailable() {
        return isEnergySavingAvailable;
    }

    public void setEnergySavingAvailable(boolean energySavingAvailable) {
        isEnergySavingAvailable = energySavingAvailable;
    }

    public boolean isFanSpeedAvailable() {
        return isFanSpeedAvailable;
    }

    public void setFanSpeedAvailable(boolean fanSpeedAvailable) {
        isFanSpeedAvailable = fanSpeedAvailable;
    }

    public boolean isAirCleanAvailable() {
        return isAirCleanAvailable;
    }

    public void setAirCleanAvailable(boolean airCleanAvailable) {
        isAirCleanAvailable = airCleanAvailable;
    }

    public boolean isJetModeAvailable() {
        return this.isJetModeAvailable;
    }

    public String getAutoDryModeCommandOn() {
        return autoDryModeCommandOn;
    }

    public void setAutoDryModeCommandOn(String autoDryModeCommandOn) {
        this.autoDryModeCommandOn = autoDryModeCommandOn;
    }

    public String getAutoDryModeCommandOff() {
        return autoDryModeCommandOff;
    }

    public void setAutoDryModeCommandOff(String autoDryModeCommandOff) {
        this.autoDryModeCommandOff = autoDryModeCommandOff;
    }

    public String getEnergySavingModeCommandOn() {
        return energySavingModeCommandOn;
    }

    public void setEnergySavingModeCommandOn(String energySavingModeCommandOn) {
        this.energySavingModeCommandOn = energySavingModeCommandOn;
    }

    public String getEnergySavingModeCommandOff() {
        return energySavingModeCommandOff;
    }

    public void setEnergySavingModeCommandOff(String energySavingModeCommandOff) {
        this.energySavingModeCommandOff = energySavingModeCommandOff;
    }

    public String getAirCleanModeCommandOn() {
        return airCleanModeCommandOn;
    }

    public void setAirCleanModeCommandOn(String airCleanModeCommandOn) {
        this.airCleanModeCommandOn = airCleanModeCommandOn;
    }

    public String getAirCleanModeCommandOff() {
        return airCleanModeCommandOff;
    }

    public void setAirCleanModeCommandOff(String airCleanModeCommandOff) {
        this.airCleanModeCommandOff = airCleanModeCommandOff;
    }
}
