/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.lgservices.model.devices.fridge;

import static org.openhab.binding.lgthinq.lgservices.LGServicesConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link FridgeCanonicalSnapshot}
 * This map the snapshot result from Washing Machine devices
 * This json payload come with path: snapshot->fridge, but this POJO expects
 * to map field below washerDryer
 *
 * @author Nemer Daud - Initial contribution
 * @author Arne Seime - Complementary sensors
 */
@NonNullByDefault
@JsonIgnoreProperties(ignoreUnknown = true)
public class FridgeCanonicalSnapshot extends AbstractFridgeSnapshot {

    private boolean online;
    private Double fridgeTemp = FRIDGE_TEMPERATURE_IGNORE_VALUE;
    private Double freezerTemp = FREEZER_TEMPERATURE_IGNORE_VALUE;
    private String tempUnit = RE_TEMP_UNIT_CELSIUS; // celsius as default

    private String doorStatus = "";
    private String waterFilterUsedMonth = "";
    private String freshAirFilterState = "";

    private String expressMode = "";
    private String expressCoolMode = "";
    private String ecoFriendlyMode = "";

    @JsonProperty("ecoFriendly")
    public String getEcoFriendlyMode() {
        return ecoFriendlyMode;
    }

    public void setEcoFriendlyMode(String ecoFriendlyMode) {
        this.ecoFriendlyMode = ecoFriendlyMode;
    }

    @JsonProperty("expressMode")
    public String getExpressMode() {
        return expressMode;
    }

    public void setExpressMode(String expressMode) {
        this.expressMode = expressMode;
    }

    @JsonProperty("atLeastOneDoorOpen")
    @JsonAlias("DoorOpenState")
    public String getDoorStatus() {
        return doorStatus;
    }

    public void setDoorStatus(String doorStatus) {
        this.doorStatus = doorStatus;
    }

    @Override
    @JsonAlias({ "TempUnit" })
    @JsonProperty("tempUnit")
    public String getTempUnit() {
        return tempUnit;
    }

    public void setTempUnit(String tempUnit) {
        this.tempUnit = tempUnit;
    }

    private String getStrTempWithUnit(Double temp) {
        return temp.intValue() + (RE_TEMP_UNIT_CELSIUS.equals(tempUnit) ? " " + RE_TEMP_UNIT_CELSIUS_SYMBOL
                : (RE_TEMP_UNIT_FAHRENHEIT).equals(tempUnit) ? " " + RE_TEMP_UNIT_FAHRENHEIT_SYMBOL : "");
    }

    @Override
    @JsonIgnore
    public String getFridgeStrTemp() {
        return getStrTempWithUnit(getFridgeTemp());
    }

    @Override
    @JsonIgnore
    public String getFreezerStrTemp() {
        return getStrTempWithUnit(getFreezerTemp());
    }

    @JsonAlias({ "TempRefrigerator" })
    @JsonProperty("fridgeTemp")
    public Double getFridgeTemp() {
        return fridgeTemp;
    }

    public void setFridgeTemp(Double fridgeTemp) {
        this.fridgeTemp = fridgeTemp;
    }

    @JsonAlias({ "TempFreezer" })
    @JsonProperty("freezerTemp")
    public Double getFreezerTemp() {
        return freezerTemp;
    }

    public void setFreezerTemp(Double freezerTemp) {
        this.freezerTemp = freezerTemp;
    }

    @Override
    public DevicePowerState getPowerStatus() {
        return isOnline() ? DevicePowerState.DV_POWER_ON : DevicePowerState.DV_POWER_OFF;
    }

    @Override
    public void setPowerStatus(DevicePowerState value) {
    }

    @JsonAlias({ "WaterFilterUsedMonth" })
    @JsonProperty("waterFilter")
    public String getWaterFilterUsedMonth() {
        return waterFilterUsedMonth;
    }

    public void setWaterFilterUsedMonth(String waterFilterUsedMonth) {
        this.waterFilterUsedMonth = waterFilterUsedMonth;
    }

    @JsonAlias({ "FreshAirFilter" })
    @JsonProperty("freshAirFilter")
    public String getFreshAirFilterState() {
        return freshAirFilterState;
    }

    public void setFreshAirFilterState(String freshAirFilterState) {
        this.freshAirFilterState = freshAirFilterState;
    }

    @Override
    public boolean isOnline() {
        return online;
    }

    @Override
    public void setOnline(boolean online) {
        this.online = online;
    }

    @JsonProperty("expressFridge")
    public String getExpressCoolMode() {
        return expressCoolMode;
    }

    public void setExpressCoolMode(String expressCoolMode) {
        this.expressCoolMode = expressCoolMode;
    }
}
