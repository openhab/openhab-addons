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
package org.openhab.binding.livisismarthome.internal.client.api.entity.capability;

import org.openhab.binding.livisismarthome.internal.client.api.entity.state.BooleanStateDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.state.DateTimeStateDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.state.DoubleStateDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.state.IntegerStateDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.state.StringStateDTO;

import com.google.gson.annotations.SerializedName;

/**
 * Holds the Capability state.
 *
 * @author Oliver Kuhl - Initial contribution
 *
 */
public class StateDTO {

    @SerializedName("absoluteEnergyConsumption")
    private DoubleStateDTO absoluteEnergyConsumptionState;

    @SerializedName("activeChannel")
    private StringStateDTO activeChannelState;

    @SerializedName("dimLevel")
    private IntegerStateDTO dimLevelState;

    @SerializedName("energyConsumptionDayEuro")
    private DoubleStateDTO energyConsumptionDayEuroState;

    @SerializedName("energyConsumptionDayKWh")
    private DoubleStateDTO energyConsumptionDayKWhState;

    @SerializedName("energyConsumptionMonthEuro")
    private DoubleStateDTO energyConsumptionMonthEuroState;

    @SerializedName("energyConsumptionMonthKWh")
    private DoubleStateDTO energyConsumptionMonthKWhState;

    @SerializedName("energyPerDayInEuro")
    private DoubleStateDTO energyPerDayInEuroState;

    @SerializedName("energyPerDayInKWh")
    private DoubleStateDTO energyPerDayInKWhState;

    @SerializedName("energyPerMonthInEuro")
    private DoubleStateDTO energyPerMonthInEuroState;

    @SerializedName("energyPerMonthInKWh")
    private DoubleStateDTO energyPerMonthInKWhState;

    @SerializedName("frostWarning")
    private BooleanStateDTO frostWarningState;

    @SerializedName("humidity")
    private DoubleStateDTO humidityState;

    @SerializedName("isDay")
    private BooleanStateDTO isDayState;

    @SerializedName("isOn")
    private BooleanStateDTO isOnState;

    @SerializedName("isOpen")
    private BooleanStateDTO isOpenState;

    @SerializedName("isSmokeAlarm")
    private BooleanStateDTO isSmokeAlarmState;

    @SerializedName("lastKeyPressCounter")
    private IntegerStateDTO lastKeyPressCounterState;

    @SerializedName("lastPressedButtonIndex")
    private IntegerStateDTO lastPressedButtonIndex;

    private StringStateDTO lastPressedButtonIndexState;

    @SerializedName("luminance")
    private DoubleStateDTO luminanceState;

    @SerializedName("moldWarning")
    private BooleanStateDTO moldWarningState;

    @SerializedName("motionDetectedCount")
    private IntegerStateDTO motionDetectedCountState;

    @SerializedName("nextSunrise")
    private DateTimeStateDTO nextSunrise;

    @SerializedName("nextSunset")
    private DateTimeStateDTO nextSunsetState;

    @SerializedName("nextTimeEvent")
    private DateTimeStateDTO nextTimeEventState;

    @SerializedName("onState")
    private BooleanStateDTO onState;

    @SerializedName("operationMode")
    private StringStateDTO operationModeState;

    @SerializedName("pointTemperature")
    private DoubleStateDTO pointTemperatureState;

    @SerializedName("powerConsumptionWatt")
    private DoubleStateDTO powerConsumptionWattState;

    @SerializedName("powerInWatt")
    private DoubleStateDTO powerInWattState;

    @SerializedName("shutterLevel")
    private IntegerStateDTO shutterLevelState;

    @SerializedName("supplyValueInCubicMetterPerDay")
    private DoubleStateDTO supplyValueInCubicMetterPerDayState;

    @SerializedName("supplyValueInCubicMetterPerMonth")
    private DoubleStateDTO supplyValueInCubicMetterPerMonthState;

    @SerializedName("supplyValueInCurrencyPerDay")
    private DoubleStateDTO supplyValueInCurrencyPerDayState;

    @SerializedName("supplyValueInCurrencyPerMonth")
    private DoubleStateDTO supplyValueInCurrencyPerMonthState;

    @SerializedName("supplyValueInLitrePerDay")
    private DoubleStateDTO supplyValueInLitrePerDayState;

    @SerializedName("supplyValueInLitrePerMonth")
    private DoubleStateDTO supplyValueInLitrePerMonthState;

    @SerializedName("temperature")
    private DoubleStateDTO temperatureState;

    @SerializedName("totalEnergy")
    private DoubleStateDTO totalEnergyState;

    @SerializedName("value")
    private BooleanStateDTO valueState;

    @SerializedName("valvePosition")
    private BooleanStateDTO valvePositionState;

    @SerializedName("windowReductionActive")
    private BooleanStateDTO windowReductionActiveState;

    public StateDTO() {
        absoluteEnergyConsumptionState = new DoubleStateDTO();
        activeChannelState = new StringStateDTO();
        dimLevelState = new IntegerStateDTO();
        energyConsumptionDayEuroState = new DoubleStateDTO();
        energyConsumptionDayKWhState = new DoubleStateDTO();
        energyConsumptionMonthEuroState = new DoubleStateDTO();
        energyConsumptionMonthKWhState = new DoubleStateDTO();
        energyPerDayInEuroState = new DoubleStateDTO();
        energyPerDayInKWhState = new DoubleStateDTO();
        energyPerMonthInEuroState = new DoubleStateDTO();
        energyPerMonthInKWhState = new DoubleStateDTO();
        frostWarningState = new BooleanStateDTO();
        humidityState = new DoubleStateDTO();
        isDayState = new BooleanStateDTO();
        isOnState = new BooleanStateDTO();
        isOpenState = new BooleanStateDTO();
        isSmokeAlarmState = new BooleanStateDTO();
        lastKeyPressCounterState = new IntegerStateDTO();
        lastPressedButtonIndex = new IntegerStateDTO();
        lastPressedButtonIndexState = new StringStateDTO();
        luminanceState = new DoubleStateDTO();
        moldWarningState = new BooleanStateDTO();
        motionDetectedCountState = new IntegerStateDTO();
        nextSunrise = new DateTimeStateDTO();
        nextSunsetState = new DateTimeStateDTO();
        nextTimeEventState = new DateTimeStateDTO();
        onState = new BooleanStateDTO();
        operationModeState = new StringStateDTO();
        pointTemperatureState = new DoubleStateDTO();
        powerConsumptionWattState = new DoubleStateDTO();
        powerInWattState = new DoubleStateDTO();
        shutterLevelState = new IntegerStateDTO();
        supplyValueInCubicMetterPerDayState = new DoubleStateDTO();
        supplyValueInCubicMetterPerMonthState = new DoubleStateDTO();
        supplyValueInCurrencyPerDayState = new DoubleStateDTO();
        supplyValueInCurrencyPerMonthState = new DoubleStateDTO();
        supplyValueInLitrePerDayState = new DoubleStateDTO();
        supplyValueInLitrePerMonthState = new DoubleStateDTO();
        temperatureState = new DoubleStateDTO();
        totalEnergyState = new DoubleStateDTO();
        valueState = new BooleanStateDTO();
        valvePositionState = new BooleanStateDTO();
        windowReductionActiveState = new BooleanStateDTO();
    }

    /**
     * @return the absoluteEnergyConsumptionState
     */
    public DoubleStateDTO getAbsoluteEnergyConsumptionState() {
        return absoluteEnergyConsumptionState;
    }

    /**
     * @param state the absoluteEnergyConsumptionState to set
     */
    public void setAbsoluteEnergyConsumptionState(DoubleStateDTO state) {
        this.absoluteEnergyConsumptionState = state;
    }

    /**
     * @return the activeChannelState
     */
    public StringStateDTO getActiveChannelState() {
        return activeChannelState;
    }

    /**
     * @param state the activeChannelState to set
     */
    public void setActiveChannelState(StringStateDTO state) {
        this.activeChannelState = state;
    }

    /**
     * @return the dimLevelState
     */
    public IntegerStateDTO getDimLevelState() {
        return dimLevelState;
    }

    /**
     * @param state the dimLevelState to set
     */
    public void setDimLevelState(IntegerStateDTO state) {
        this.dimLevelState = state;
    }

    /**
     * @return the energyConsumptionDayEuroState
     */
    public DoubleStateDTO getEnergyConsumptionDayEuroState() {
        return energyConsumptionDayEuroState;
    }

    /**
     * @param state the energyConsumptionDayEuroState to set
     */
    public void setEnergyConsumptionDayEuroState(DoubleStateDTO state) {
        this.energyConsumptionDayEuroState = state;
    }

    /**
     * @return the energyConsumptionDayKWhState
     */
    public DoubleStateDTO getEnergyConsumptionDayKWhState() {
        return energyConsumptionDayKWhState;
    }

    /**
     * @param state the energyConsumptionDayKWhState to set
     */
    public void setEnergyConsumptionDayKWhState(DoubleStateDTO state) {
        this.energyConsumptionDayKWhState = state;
    }

    /**
     * @return the energyConsumptionMonthEuroState
     */
    public DoubleStateDTO getEnergyConsumptionMonthEuroState() {
        return energyConsumptionMonthEuroState;
    }

    /**
     * @param state the energyConsumptionMonthEuroState to set
     */
    public void setEnergyConsumptionMonthEuroState(DoubleStateDTO state) {
        this.energyConsumptionMonthEuroState = state;
    }

    /**
     * @return the energyConsumptionMonthKWhState
     */
    public DoubleStateDTO getEnergyConsumptionMonthKWhState() {
        return energyConsumptionMonthKWhState;
    }

    /**
     * @param state the energyConsumptionMonthKWhState to set
     */
    public void setEnergyConsumptionMonthKWhState(DoubleStateDTO state) {
        this.energyConsumptionMonthKWhState = state;
    }

    /**
     * @return the energyPerDayInEuroState
     */
    public DoubleStateDTO getEnergyPerDayInEuroState() {
        return energyPerDayInEuroState;
    }

    /**
     * @param state the energyPerDayInEuroState to set
     */
    public void setEnergyPerDayInEuroState(DoubleStateDTO state) {
        this.energyPerDayInEuroState = state;
    }

    /**
     * @return the energyPerDayInKWhState
     */
    public DoubleStateDTO getEnergyPerDayInKWhState() {
        return energyPerDayInKWhState;
    }

    /**
     * @param state the energyPerDayInKWhState to set
     */
    public void setEnergyPerDayInKWhState(DoubleStateDTO state) {
        this.energyPerDayInKWhState = state;
    }

    /**
     * @return the energyPerMonthInEuroState
     */
    public DoubleStateDTO getEnergyPerMonthInEuroState() {
        return energyPerMonthInEuroState;
    }

    /**
     * @param state the energyPerMonthInEuroState to set
     */
    public void setEnergyPerMonthInEuroState(DoubleStateDTO state) {
        this.energyPerMonthInEuroState = state;
    }

    /**
     * @return the energyPerMonthInKWhState
     */
    public DoubleStateDTO getEnergyPerMonthInKWhState() {
        return energyPerMonthInKWhState;
    }

    /**
     * @param state the energyPerMonthInKWhState to set
     */
    public void setEnergyPerMonthInKWhState(DoubleStateDTO state) {
        this.energyPerMonthInKWhState = state;
    }

    /**
     * @return the frostWarningState
     */
    public BooleanStateDTO getFrostWarningState() {
        return frostWarningState;
    }

    /**
     * @param state the frostWarningState to set
     */
    public void setFrostWarningState(BooleanStateDTO state) {
        this.frostWarningState = state;
    }

    /**
     * @return the humidityState
     */
    public DoubleStateDTO getHumidityState() {
        return humidityState;
    }

    /**
     * @param state the humidityState to set
     */
    public void setHumidityState(DoubleStateDTO state) {
        this.humidityState = state;
    }

    /**
     * @return the isDayState
     */
    public BooleanStateDTO getIsDayState() {
        return isDayState;
    }

    /**
     * @param state the isDayState to set
     */
    public void setIsDayState(BooleanStateDTO state) {
        this.isDayState = state;
    }

    /**
     * @return the isOnState
     */
    public BooleanStateDTO getIsOnState() {
        return isOnState;
    }

    /**
     * @param state the isOnState to set
     */
    public void setIsOnState(BooleanStateDTO state) {
        this.isOnState = state;
    }

    /**
     * @return the isOpenState
     */
    public BooleanStateDTO getIsOpenState() {
        return isOpenState;
    }

    /**
     * @param state the isOpenState to set
     */
    public void setIsOpenState(BooleanStateDTO state) {
        this.isOpenState = state;
    }

    /**
     * @return the isSmokeAlarmState
     */
    public BooleanStateDTO getIsSmokeAlarmState() {
        return isSmokeAlarmState;
    }

    /**
     * @param state the isSmokeAlarmState to set
     */
    public void setIsSmokeAlarmState(BooleanStateDTO state) {
        this.isSmokeAlarmState = state;
    }

    /**
     * @return the lastKeyPressCounterState
     */
    public IntegerStateDTO getLastKeyPressCounterState() {
        return lastKeyPressCounterState;
    }

    /**
     * @param state the lastKeyPressCounterState to set
     */
    public void setLastKeyPressCounterState(IntegerStateDTO state) {
        this.lastKeyPressCounterState = state;
    }

    /**
     * @return the lastPressedButtonIndex
     */
    public IntegerStateDTO getLastPressedButtonIndex() {
        return lastPressedButtonIndex;
    }

    /**
     * @param state the lastPressedButtonIndex to set
     */
    public void setLastPressedButtonIndex(IntegerStateDTO state) {
        this.lastPressedButtonIndex = state;
    }

    public StringStateDTO getLastKeyPressType() {
        if (lastPressedButtonIndexState == null) {
            lastPressedButtonIndexState = new StringStateDTO();
        }
        return lastPressedButtonIndexState;
    }

    public void setLastKeyPressType(StringStateDTO lastPressedButtonIndexState) {
        this.lastPressedButtonIndexState = lastPressedButtonIndexState;
    }

    /**
     * @return the luminanceState
     */
    public DoubleStateDTO getLuminanceState() {
        return luminanceState;
    }

    /**
     * @param state the luminanceState to set
     */
    public void setLuminanceState(DoubleStateDTO state) {
        this.luminanceState = state;
    }

    /**
     * @return the moldWarningState
     */
    public BooleanStateDTO getMoldWarningState() {
        return moldWarningState;
    }

    /**
     * @param state the moldWarningState to set
     */
    public void setMoldWarningState(BooleanStateDTO state) {
        this.moldWarningState = state;
    }

    /**
     * @return the motionDetectedCountState
     */
    public IntegerStateDTO getMotionDetectedCountState() {
        return motionDetectedCountState;
    }

    /**
     * @param state the motionDetectedCountState to set
     */
    public void setMotionDetectedCountState(IntegerStateDTO state) {
        this.motionDetectedCountState = state;
    }

    /**
     * @return the nextSunrise
     */
    public DateTimeStateDTO getNextSunrise() {
        return nextSunrise;
    }

    /**
     * @param state the nextSunrise to set
     */
    public void setNextSunrise(DateTimeStateDTO state) {
        this.nextSunrise = state;
    }

    /**
     * @return the nextSunsetState
     */
    public DateTimeStateDTO getNextSunsetState() {
        return nextSunsetState;
    }

    /**
     * @param state the nextSunsetState to set
     */
    public void setNextSunsetState(DateTimeStateDTO state) {
        this.nextSunsetState = state;
    }

    /**
     * @return the nextTimeEventState
     */
    public DateTimeStateDTO getNextTimeEventState() {
        return nextTimeEventState;
    }

    /**
     * @param state the nextTimeEventState to set
     */
    public void setNextTimeEventState(DateTimeStateDTO state) {
        this.nextTimeEventState = state;
    }

    /**
     * @return the onState
     */
    public BooleanStateDTO getOnState() {
        return onState;
    }

    /**
     * @param state the onState to set
     */
    public void setOnState(BooleanStateDTO state) {
        this.onState = state;
    }

    /**
     * @return the operationModeState
     */
    public StringStateDTO getOperationModeState() {
        return operationModeState;
    }

    /**
     * @param state the operationModeState to set
     */
    public void setOperationModeState(StringStateDTO state) {
        this.operationModeState = state;
    }

    /**
     * @return the pointTemperatureState
     */
    public DoubleStateDTO getPointTemperatureState() {
        return pointTemperatureState;
    }

    /**
     * @param state the pointTemperatureState to set
     */
    public void setPointTemperatureState(DoubleStateDTO state) {
        this.pointTemperatureState = state;
    }

    /**
     * @return the powerConsumptionWattState
     */
    public DoubleStateDTO getPowerConsumptionWattState() {
        return powerConsumptionWattState;
    }

    /**
     * @param state the powerConsumptionWattState to set
     */
    public void setPowerConsumptionWattState(DoubleStateDTO state) {
        this.powerConsumptionWattState = state;
    }

    /**
     * @return the powerInWattState
     */
    public DoubleStateDTO getPowerInWattState() {
        return powerInWattState;
    }

    /**
     * @param state the powerInWattState to set
     */
    public void setPowerInWattState(DoubleStateDTO state) {
        this.powerInWattState = state;
    }

    /**
     * @return the shutterLevelState
     */
    public IntegerStateDTO getShutterLevelState() {
        return shutterLevelState;
    }

    /**
     * @param state the shutterLevelState to set
     */
    public void setShutterLevelState(IntegerStateDTO state) {
        this.shutterLevelState = state;
    }

    /**
     * @return the supplyValueInCubicMetterPerDayState
     */
    public DoubleStateDTO getSupplyValueInCubicMetterPerDayState() {
        return supplyValueInCubicMetterPerDayState;
    }

    /**
     * @param state the supplyValueInCubicMetterPerDayState to set
     */
    public void setSupplyValueInCubicMetterPerDayState(DoubleStateDTO state) {
        this.supplyValueInCubicMetterPerDayState = state;
    }

    /**
     * @return the supplyValueInCubicMetterPerMonthState
     */
    public DoubleStateDTO getSupplyValueInCubicMetterPerMonthState() {
        return supplyValueInCubicMetterPerMonthState;
    }

    /**
     * @param state the supplyValueInCubicMetterPerMonthState to set
     */
    public void setSupplyValueInCubicMetterPerMonthState(DoubleStateDTO state) {
        this.supplyValueInCubicMetterPerMonthState = state;
    }

    /**
     * @return the supplyValueInCurrencyPerDayState
     */
    public DoubleStateDTO getSupplyValueInCurrencyPerDayState() {
        return supplyValueInCurrencyPerDayState;
    }

    /**
     * @param state the supplyValueInCurrencyPerDayState to set
     */
    public void setSupplyValueInCurrencyPerDayState(DoubleStateDTO state) {
        this.supplyValueInCurrencyPerDayState = state;
    }

    /**
     * @return the supplyValueInCurrencyPerMonthState
     */
    public DoubleStateDTO getSupplyValueInCurrencyPerMonthState() {
        return supplyValueInCurrencyPerMonthState;
    }

    /**
     * @param state the supplyValueInCurrencyPerMonthState to set
     */
    public void setSupplyValueInCurrencyPerMonthState(DoubleStateDTO state) {
        this.supplyValueInCurrencyPerMonthState = state;
    }

    /**
     * @return the supplyValueInLitrePerDayState
     */
    public DoubleStateDTO getSupplyValueInLitrePerDayState() {
        return supplyValueInLitrePerDayState;
    }

    /**
     * @param state the supplyValueInLitrePerDayState to set
     */
    public void setSupplyValueInLitrePerDayState(DoubleStateDTO state) {
        this.supplyValueInLitrePerDayState = state;
    }

    /**
     * @return the supplyValueInLitrePerMonthState
     */
    public DoubleStateDTO getSupplyValueInLitrePerMonthState() {
        return supplyValueInLitrePerMonthState;
    }

    /**
     * @param state the supplyValueInLitrePerMonthState to set
     */
    public void setSupplyValueInLitrePerMonthState(DoubleStateDTO state) {
        this.supplyValueInLitrePerMonthState = state;
    }

    /**
     * @return the temperatureState
     */
    public DoubleStateDTO getTemperatureState() {
        return temperatureState;
    }

    /**
     * @param state the temperatureState to set
     */
    public void setTemperatureState(DoubleStateDTO state) {
        this.temperatureState = state;
    }

    /**
     * @return the totalEnergyState
     */
    public DoubleStateDTO getTotalEnergyState() {
        return totalEnergyState;
    }

    /**
     * @param state the totalEnergyState to set
     */
    public void setTotalEnergyState(DoubleStateDTO state) {
        this.totalEnergyState = state;
    }

    /**
     * @return the valueState
     */
    public BooleanStateDTO getValueState() {
        return valueState;
    }

    /**
     * @param state the valueState to set
     */
    public void setValueState(BooleanStateDTO state) {
        this.valueState = state;
    }

    /**
     * @return the valvePositionState
     */
    public BooleanStateDTO getValvePositionState() {
        return valvePositionState;
    }

    /**
     * @param state the valvePositionState to set
     */
    public void setValvePositionState(BooleanStateDTO state) {
        this.valvePositionState = state;
    }

    /**
     * @return the windowReductionActiveState
     */
    public BooleanStateDTO getWindowReductionActiveState() {
        return windowReductionActiveState;
    }

    /**
     * @param state the windowReductionActiveState to set
     */
    public void setWindowReductionActiveState(BooleanStateDTO state) {
        this.windowReductionActiveState = state;
    }
}
