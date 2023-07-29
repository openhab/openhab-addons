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
package org.openhab.binding.innogysmarthome.internal.client.entity.capability;

import org.openhab.binding.innogysmarthome.internal.client.entity.state.BooleanState;
import org.openhab.binding.innogysmarthome.internal.client.entity.state.DateTimeState;
import org.openhab.binding.innogysmarthome.internal.client.entity.state.DoubleState;
import org.openhab.binding.innogysmarthome.internal.client.entity.state.IntegerState;
import org.openhab.binding.innogysmarthome.internal.client.entity.state.StringState;

import com.google.gson.annotations.SerializedName;

/**
 * Holds the Capability state.
 *
 * @author Oliver Kuhl - Initial contribution
 *
 */
public class State {

    @SerializedName("absoluteEnergyConsumption")
    private DoubleState absoluteEnergyConsumptionState;

    @SerializedName("activeChannel")
    private StringState activeChannelState;

    @SerializedName("dimLevel")
    private IntegerState dimLevelState;

    @SerializedName("energyConsumptionDayEuro")
    private DoubleState energyConsumptionDayEuroState;

    @SerializedName("energyConsumptionDayKWh")
    private DoubleState energyConsumptionDayKWhState;

    @SerializedName("energyConsumptionMonthEuro")
    private DoubleState energyConsumptionMonthEuroState;

    @SerializedName("energyConsumptionMonthKWh")
    private DoubleState energyConsumptionMonthKWhState;

    @SerializedName("energyPerDayInEuro")
    private DoubleState energyPerDayInEuroState;

    @SerializedName("energyPerDayInKWh")
    private DoubleState energyPerDayInKWhState;

    @SerializedName("energyPerMonthInEuro")
    private DoubleState energyPerMonthInEuroState;

    @SerializedName("energyPerMonthInKWh")
    private DoubleState energyPerMonthInKWhState;

    @SerializedName("frostWarning")
    private BooleanState frostWarningState;

    @SerializedName("humidity")
    private DoubleState humidityState;

    @SerializedName("isDay")
    private BooleanState isDayState;

    @SerializedName("isOn")
    private BooleanState isOnState;

    @SerializedName("isOpen")
    private BooleanState isOpenState;

    @SerializedName("isSmokeAlarm")
    private BooleanState isSmokeAlarmState;

    @SerializedName("lastKeyPressCounter")
    private IntegerState lastKeyPressCounterState;

    @SerializedName("lastPressedButtonIndex")
    private IntegerState lastPressedButtonIndex;

    private StringState lastPressedButtonIndexState;

    @SerializedName("luminance")
    private DoubleState luminanceState;

    @SerializedName("moldWarning")
    private BooleanState moldWarningState;

    @SerializedName("motionDetectedCount")
    private IntegerState motionDetectedCountState;

    @SerializedName("nextSunrise")
    private DateTimeState nextSunrise;

    @SerializedName("nextSunset")
    private DateTimeState nextSunsetState;

    @SerializedName("nextTimeEvent")
    private DateTimeState nextTimeEventState;

    @SerializedName("onState")
    private BooleanState onState;

    @SerializedName("operationMode")
    private StringState operationModeState;

    @SerializedName("pointTemperature")
    private DoubleState pointTemperatureState;

    @SerializedName("powerConsumptionWatt")
    private DoubleState powerConsumptionWattState;

    @SerializedName("powerInWatt")
    private DoubleState powerInWattState;

    @SerializedName("shutterLevel")
    private IntegerState shutterLevelState;

    @SerializedName("supplyValueInCubicMetterPerDay")
    private DoubleState supplyValueInCubicMetterPerDayState;

    @SerializedName("supplyValueInCubicMetterPerMonth")
    private DoubleState supplyValueInCubicMetterPerMonthState;

    @SerializedName("supplyValueInCurrencyPerDay")
    private DoubleState supplyValueInCurrencyPerDayState;

    @SerializedName("supplyValueInCurrencyPerMonth")
    private DoubleState supplyValueInCurrencyPerMonthState;

    @SerializedName("supplyValueInLitrePerDay")
    private DoubleState supplyValueInLitrePerDayState;

    @SerializedName("supplyValueInLitrePerMonth")
    private DoubleState supplyValueInLitrePerMonthState;

    @SerializedName("temperature")
    private DoubleState temperatureState;

    @SerializedName("totalEnergy")
    private DoubleState totalEnergyState;

    @SerializedName("value")
    private BooleanState valueState;

    @SerializedName("valvePosition")
    private BooleanState valvePositionState;

    @SerializedName("windowReductionActive")
    private BooleanState windowReductionActiveState;

    /**
     * @return the absoluteEnergyConsumptionState
     */
    public DoubleState getAbsoluteEnergyConsumptionState() {
        return absoluteEnergyConsumptionState;
    }

    /**
     * @param state the absoluteEnergyConsumptionState to set
     */
    public void setAbsoluteEnergyConsumptionState(DoubleState state) {
        this.absoluteEnergyConsumptionState = state;
    }

    /**
     * @return the activeChannelState
     */
    public StringState getActiveChannelState() {
        return activeChannelState;
    }

    /**
     * @param state the activeChannelState to set
     */
    public void setActiveChannelState(StringState state) {
        this.activeChannelState = state;
    }

    /**
     * @return the dimLevelState
     */
    public IntegerState getDimLevelState() {
        return dimLevelState;
    }

    /**
     * @param state the dimLevelState to set
     */
    public void setDimLevelState(IntegerState state) {
        this.dimLevelState = state;
    }

    /**
     * @return the energyConsumptionDayEuroState
     */
    public DoubleState getEnergyConsumptionDayEuroState() {
        return energyConsumptionDayEuroState;
    }

    /**
     * @param state the energyConsumptionDayEuroState to set
     */
    public void setEnergyConsumptionDayEuroState(DoubleState state) {
        this.energyConsumptionDayEuroState = state;
    }

    /**
     * @return the energyConsumptionDayKWhState
     */
    public DoubleState getEnergyConsumptionDayKWhState() {
        return energyConsumptionDayKWhState;
    }

    /**
     * @param state the energyConsumptionDayKWhState to set
     */
    public void setEnergyConsumptionDayKWhState(DoubleState state) {
        this.energyConsumptionDayKWhState = state;
    }

    /**
     * @return the energyConsumptionMonthEuroState
     */
    public DoubleState getEnergyConsumptionMonthEuroState() {
        return energyConsumptionMonthEuroState;
    }

    /**
     * @param state the energyConsumptionMonthEuroState to set
     */
    public void setEnergyConsumptionMonthEuroState(DoubleState state) {
        this.energyConsumptionMonthEuroState = state;
    }

    /**
     * @return the energyConsumptionMonthKWhState
     */
    public DoubleState getEnergyConsumptionMonthKWhState() {
        return energyConsumptionMonthKWhState;
    }

    /**
     * @param state the energyConsumptionMonthKWhState to set
     */
    public void setEnergyConsumptionMonthKWhState(DoubleState state) {
        this.energyConsumptionMonthKWhState = state;
    }

    /**
     * @return the energyPerDayInEuroState
     */
    public DoubleState getEnergyPerDayInEuroState() {
        return energyPerDayInEuroState;
    }

    /**
     * @param state the energyPerDayInEuroState to set
     */
    public void setEnergyPerDayInEuroState(DoubleState state) {
        this.energyPerDayInEuroState = state;
    }

    /**
     * @return the energyPerDayInKWhState
     */
    public DoubleState getEnergyPerDayInKWhState() {
        return energyPerDayInKWhState;
    }

    /**
     * @param state the energyPerDayInKWhState to set
     */
    public void setEnergyPerDayInKWhState(DoubleState state) {
        this.energyPerDayInKWhState = state;
    }

    /**
     * @return the energyPerMonthInEuroState
     */
    public DoubleState getEnergyPerMonthInEuroState() {
        return energyPerMonthInEuroState;
    }

    /**
     * @param state the energyPerMonthInEuroState to set
     */
    public void setEnergyPerMonthInEuroState(DoubleState state) {
        this.energyPerMonthInEuroState = state;
    }

    /**
     * @return the energyPerMonthInKWhState
     */
    public DoubleState getEnergyPerMonthInKWhState() {
        return energyPerMonthInKWhState;
    }

    /**
     * @param state the energyPerMonthInKWhState to set
     */
    public void setEnergyPerMonthInKWhState(DoubleState state) {
        this.energyPerMonthInKWhState = state;
    }

    /**
     * @return the frostWarningState
     */
    public BooleanState getFrostWarningState() {
        return frostWarningState;
    }

    /**
     * @param state the frostWarningState to set
     */
    public void setFrostWarningState(BooleanState state) {
        this.frostWarningState = state;
    }

    /**
     * @return the humidityState
     */
    public DoubleState getHumidityState() {
        return humidityState;
    }

    /**
     * @param state the humidityState to set
     */
    public void setHumidityState(DoubleState state) {
        this.humidityState = state;
    }

    /**
     * @return the isDayState
     */
    public BooleanState getIsDayState() {
        return isDayState;
    }

    /**
     * @param state the isDayState to set
     */
    public void setIsDayState(BooleanState state) {
        this.isDayState = state;
    }

    /**
     * @return the isOnState
     */
    public BooleanState getIsOnState() {
        return isOnState;
    }

    /**
     * @param state the isOnState to set
     */
    public void setIsOnState(BooleanState state) {
        this.isOnState = state;
    }

    /**
     * @return the isOpenState
     */
    public BooleanState getIsOpenState() {
        return isOpenState;
    }

    /**
     * @param state the isOpenState to set
     */
    public void setIsOpenState(BooleanState state) {
        this.isOpenState = state;
    }

    /**
     * @return the isSmokeAlarmState
     */
    public BooleanState getIsSmokeAlarmState() {
        return isSmokeAlarmState;
    }

    /**
     * @param state the isSmokeAlarmState to set
     */
    public void setIsSmokeAlarmState(BooleanState state) {
        this.isSmokeAlarmState = state;
    }

    /**
     * @return the lastKeyPressCounterState
     */
    public IntegerState getLastKeyPressCounterState() {
        return lastKeyPressCounterState;
    }

    /**
     * @param state the lastKeyPressCounterState to set
     */
    public void setLastKeyPressCounterState(IntegerState state) {
        this.lastKeyPressCounterState = state;
    }

    /**
     * @return the lastPressedButtonIndex
     */
    public IntegerState getLastPressedButtonIndex() {
        return lastPressedButtonIndex;
    }

    /**
     * @param state the lastPressedButtonIndex to set
     */
    public void setLastPressedButtonIndex(IntegerState state) {
        this.lastPressedButtonIndex = state;
    }

    public StringState getLastKeyPressType() {
        if (lastPressedButtonIndexState == null) {
            lastPressedButtonIndexState = new StringState();
        }
        return lastPressedButtonIndexState;
    }

    public void setLastKeyPressType(StringState lastPressedButtonIndexState) {
        this.lastPressedButtonIndexState = lastPressedButtonIndexState;
    }

    /**
     * @return the luminanceState
     */
    public DoubleState getLuminanceState() {
        return luminanceState;
    }

    /**
     * @param state the luminanceState to set
     */
    public void setLuminanceState(DoubleState state) {
        this.luminanceState = state;
    }

    /**
     * @return the moldWarningState
     */
    public BooleanState getMoldWarningState() {
        return moldWarningState;
    }

    /**
     * @param state the moldWarningState to set
     */
    public void setMoldWarningState(BooleanState state) {
        this.moldWarningState = state;
    }

    /**
     * @return the motionDetectedCountState
     */
    public IntegerState getMotionDetectedCountState() {
        return motionDetectedCountState;
    }

    /**
     * @param state the motionDetectedCountState to set
     */
    public void setMotionDetectedCountState(IntegerState state) {
        this.motionDetectedCountState = state;
    }

    /**
     * @return the nextSunrise
     */
    public DateTimeState getNextSunrise() {
        return nextSunrise;
    }

    /**
     * @param state the nextSunrise to set
     */
    public void setNextSunrise(DateTimeState state) {
        this.nextSunrise = state;
    }

    /**
     * @return the nextSunsetState
     */
    public DateTimeState getNextSunsetState() {
        return nextSunsetState;
    }

    /**
     * @param state the nextSunsetState to set
     */
    public void setNextSunsetState(DateTimeState state) {
        this.nextSunsetState = state;
    }

    /**
     * @return the nextTimeEventState
     */
    public DateTimeState getNextTimeEventState() {
        return nextTimeEventState;
    }

    /**
     * @param state the nextTimeEventState to set
     */
    public void setNextTimeEventState(DateTimeState state) {
        this.nextTimeEventState = state;
    }

    /**
     * @return the onState
     */
    public BooleanState getOnState() {
        return onState;
    }

    /**
     * @param state the onState to set
     */
    public void setOnState(BooleanState state) {
        this.onState = state;
    }

    /**
     * @return the operationModeState
     */
    public StringState getOperationModeState() {
        return operationModeState;
    }

    /**
     * @param state the operationModeState to set
     */
    public void setOperationModeState(StringState state) {
        this.operationModeState = state;
    }

    /**
     * @return the pointTemperatureState
     */
    public DoubleState getPointTemperatureState() {
        return pointTemperatureState;
    }

    /**
     * @param state the pointTemperatureState to set
     */
    public void setPointTemperatureState(DoubleState state) {
        this.pointTemperatureState = state;
    }

    /**
     * @return the powerConsumptionWattState
     */
    public DoubleState getPowerConsumptionWattState() {
        return powerConsumptionWattState;
    }

    /**
     * @param state the powerConsumptionWattState to set
     */
    public void setPowerConsumptionWattState(DoubleState state) {
        this.powerConsumptionWattState = state;
    }

    /**
     * @return the powerInWattState
     */
    public DoubleState getPowerInWattState() {
        return powerInWattState;
    }

    /**
     * @param state the powerInWattState to set
     */
    public void setPowerInWattState(DoubleState state) {
        this.powerInWattState = state;
    }

    /**
     * @return the shutterLevelState
     */
    public IntegerState getShutterLevelState() {
        return shutterLevelState;
    }

    /**
     * @param state the shutterLevelState to set
     */
    public void setShutterLevelState(IntegerState state) {
        this.shutterLevelState = state;
    }

    /**
     * @return the supplyValueInCubicMetterPerDayState
     */
    public DoubleState getSupplyValueInCubicMetterPerDayState() {
        return supplyValueInCubicMetterPerDayState;
    }

    /**
     * @param state the supplyValueInCubicMetterPerDayState to set
     */
    public void setSupplyValueInCubicMetterPerDayState(DoubleState state) {
        this.supplyValueInCubicMetterPerDayState = state;
    }

    /**
     * @return the supplyValueInCubicMetterPerMonthState
     */
    public DoubleState getSupplyValueInCubicMetterPerMonthState() {
        return supplyValueInCubicMetterPerMonthState;
    }

    /**
     * @param state the supplyValueInCubicMetterPerMonthState to set
     */
    public void setSupplyValueInCubicMetterPerMonthState(DoubleState state) {
        this.supplyValueInCubicMetterPerMonthState = state;
    }

    /**
     * @return the supplyValueInCurrencyPerDayState
     */
    public DoubleState getSupplyValueInCurrencyPerDayState() {
        return supplyValueInCurrencyPerDayState;
    }

    /**
     * @param state the supplyValueInCurrencyPerDayState to set
     */
    public void setSupplyValueInCurrencyPerDayState(DoubleState state) {
        this.supplyValueInCurrencyPerDayState = state;
    }

    /**
     * @return the supplyValueInCurrencyPerMonthState
     */
    public DoubleState getSupplyValueInCurrencyPerMonthState() {
        return supplyValueInCurrencyPerMonthState;
    }

    /**
     * @param state the supplyValueInCurrencyPerMonthState to set
     */
    public void setSupplyValueInCurrencyPerMonthState(DoubleState state) {
        this.supplyValueInCurrencyPerMonthState = state;
    }

    /**
     * @return the supplyValueInLitrePerDayState
     */
    public DoubleState getSupplyValueInLitrePerDayState() {
        return supplyValueInLitrePerDayState;
    }

    /**
     * @param state the supplyValueInLitrePerDayState to set
     */
    public void setSupplyValueInLitrePerDayState(DoubleState state) {
        this.supplyValueInLitrePerDayState = state;
    }

    /**
     * @return the supplyValueInLitrePerMonthState
     */
    public DoubleState getSupplyValueInLitrePerMonthState() {
        return supplyValueInLitrePerMonthState;
    }

    /**
     * @param state the supplyValueInLitrePerMonthState to set
     */
    public void setSupplyValueInLitrePerMonthState(DoubleState state) {
        this.supplyValueInLitrePerMonthState = state;
    }

    /**
     * @return the temperatureState
     */
    public DoubleState getTemperatureState() {
        return temperatureState;
    }

    /**
     * @param state the temperatureState to set
     */
    public void setTemperatureState(DoubleState state) {
        this.temperatureState = state;
    }

    /**
     * @return the totalEnergyState
     */
    public DoubleState getTotalEnergyState() {
        return totalEnergyState;
    }

    /**
     * @param state the totalEnergyState to set
     */
    public void setTotalEnergyState(DoubleState state) {
        this.totalEnergyState = state;
    }

    /**
     * @return the valueState
     */
    public BooleanState getValueState() {
        return valueState;
    }

    /**
     * @param state the valueState to set
     */
    public void setValueState(BooleanState state) {
        this.valueState = state;
    }

    /**
     * @return the valvePositionState
     */
    public BooleanState getValvePositionState() {
        return valvePositionState;
    }

    /**
     * @param state the valvePositionState to set
     */
    public void setValvePositionState(BooleanState state) {
        this.valvePositionState = state;
    }

    /**
     * @return the windowReductionActiveState
     */
    public BooleanState getWindowReductionActiveState() {
        return windowReductionActiveState;
    }

    /**
     * @param state the windowReductionActiveState to set
     */
    public void setWindowReductionActiveState(BooleanState state) {
        this.windowReductionActiveState = state;
    }
}
