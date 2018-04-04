/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.toon.internal.api;

/**
 * The {@link ThermostatInfo} class defines the json object as received by the api.
 *
 * @author Jorg de Jong - Initial contribution
 */
public class ThermostatInfo {
    private Long currentSetpoint;
    private Long currentDisplayTemp;
    private Long currentTemp;
    private Long activeState;
    private Long programState;
    private Long nextState;
    private Long currentModulationLevel;
    private String burnerInfo;

    public Long getCurrentSetpoint() {
        return currentSetpoint;
    }

    public void setCurrentSetpoint(Long currentSetpoint) {
        this.currentSetpoint = currentSetpoint;
    }

    public Long getCurrentDisplayTemp() {
        return currentDisplayTemp;
    }

    public void setCurrentDisplayTemp(Long currentDisplayTemp) {
        this.currentDisplayTemp = currentDisplayTemp;
    }

    public Long getCurrentTemp() {
        return currentTemp;
    }

    public void setCurrentTemp(Long currentTemp) {
        this.currentTemp = currentTemp;
    }

    public Long getActiveState() {
        return activeState;
    }

    public void setActiveState(Long activeState) {
        this.activeState = activeState;
    }

    public Long getProgramState() {
        return programState;
    }

    public void setProgramState(Long programState) {
        this.programState = programState;
    }

    public Long getNextState() {
        return nextState;
    }

    public void setNextState(Long nextState) {
        this.nextState = nextState;
    }

    public Long getCurrentModulationLevel() {
        return currentModulationLevel;
    }

    public void setCurrentModulationLevel(Long currentModulationLevel) {
        this.currentModulationLevel = currentModulationLevel;
    }

    public String getBurnerInfo() {
        return burnerInfo;
    }

    public void setBurnerInfo(String burnerInfo) {
        this.burnerInfo = burnerInfo;
    }
}
