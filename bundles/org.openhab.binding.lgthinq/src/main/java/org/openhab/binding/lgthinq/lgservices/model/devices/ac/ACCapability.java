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
package org.openhab.binding.lgthinq.lgservices.model.devices.ac;

import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.model.AbstractCapability;

/**
 * The {@link ACCapability}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class ACCapability extends AbstractCapability<ACCapability> {

    private Map<String, String> opMod = Collections.emptyMap();
    private Map<String, String> fanSpeed = Collections.emptyMap();
    private boolean isJetModeAvailable;
    private boolean isEnergyMonitorAvailable;
    private boolean isFilterMonitorAvailable;
    private boolean isAutoDryModeAvailable;
    private boolean isEnergySavingAvailable;
    private boolean isAirCleanAvailable;
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

    public Map<String, String> getOpMode() {
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

    public boolean isEnergyMonitorAvailable() {
        return isEnergyMonitorAvailable;
    }

    public void setEnergyMonitorAvailable(boolean energyMonitorAvailable) {
        isEnergyMonitorAvailable = energyMonitorAvailable;
    }

    public boolean isFilterMonitorAvailable() {
        return isFilterMonitorAvailable;
    }

    public void setFilterMonitorAvailable(boolean filterMonitorAvailable) {
        isFilterMonitorAvailable = filterMonitorAvailable;
    }
}
