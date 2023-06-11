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
package org.openhab.binding.airvisualnode.internal.dto.airvisual;

/**
 * Settings data.
 *
 * @author Victor Antonovich - Initial contribution
 */
public class Settings {

    private String followedStation;
    private boolean isAqiUsa;
    private boolean isConcentrationShowed;
    private boolean isIndoor;
    private boolean isLcdOn;
    private boolean isNetworkTime;
    private boolean isTemperatureCelsius;
    private String language;
    private int lcdBrightness;
    private String nodeName;
    private PowerSaving powerSaving;
    private String speedUnit;
    private String timezone;

    public Settings(String followedStation, boolean isAqiUsa, boolean isConcentrationShowed, boolean isIndoor,
            boolean isLcdOn, boolean isNetworkTime, boolean isTemperatureCelsius, String language, int lcdBrightness,
            String nodeName, PowerSaving powerSaving, String speedUnit, String timezone) {
        this.followedStation = followedStation;
        this.isAqiUsa = isAqiUsa;
        this.isConcentrationShowed = isConcentrationShowed;
        this.isIndoor = isIndoor;
        this.isLcdOn = isLcdOn;
        this.isNetworkTime = isNetworkTime;
        this.isTemperatureCelsius = isTemperatureCelsius;
        this.language = language;
        this.lcdBrightness = lcdBrightness;
        this.nodeName = nodeName;
        this.powerSaving = powerSaving;
        this.speedUnit = speedUnit;
        this.timezone = timezone;
    }

    public String getFollowedStation() {
        return followedStation;
    }

    public void setFollowedStation(String followedStation) {
        this.followedStation = followedStation;
    }

    public boolean isIsAqiUsa() {
        return isAqiUsa;
    }

    public void setIsAqiUsa(boolean isAqiUsa) {
        this.isAqiUsa = isAqiUsa;
    }

    public boolean isIsConcentrationShowed() {
        return isConcentrationShowed;
    }

    public void setIsConcentrationShowed(boolean isConcentrationShowed) {
        this.isConcentrationShowed = isConcentrationShowed;
    }

    public boolean isIsIndoor() {
        return isIndoor;
    }

    public void setIsIndoor(boolean isIndoor) {
        this.isIndoor = isIndoor;
    }

    public boolean isIsLcdOn() {
        return isLcdOn;
    }

    public void setIsLcdOn(boolean isLcdOn) {
        this.isLcdOn = isLcdOn;
    }

    public boolean isIsNetworkTime() {
        return isNetworkTime;
    }

    public void setIsNetworkTime(boolean isNetworkTime) {
        this.isNetworkTime = isNetworkTime;
    }

    public boolean isIsTemperatureCelsius() {
        return isTemperatureCelsius;
    }

    public void setIsTemperatureCelsius(boolean isTemperatureCelsius) {
        this.isTemperatureCelsius = isTemperatureCelsius;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getLcdBrightness() {
        return lcdBrightness;
    }

    public void setLcdBrightness(int lcdBrightness) {
        this.lcdBrightness = lcdBrightness;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public PowerSaving getPowerSaving() {
        return powerSaving;
    }

    public void setPowerSaving(PowerSaving powerSaving) {
        this.powerSaving = powerSaving;
    }

    public String getSpeedUnit() {
        return speedUnit;
    }

    public void setSpeedUnit(String speedUnit) {
        this.speedUnit = speedUnit;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}
