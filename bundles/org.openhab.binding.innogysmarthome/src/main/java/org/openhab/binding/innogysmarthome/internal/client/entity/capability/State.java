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
package org.openhab.binding.innogysmarthome.internal.client.entity.capability;

import org.openhab.binding.innogysmarthome.internal.client.entity.state.BooleanState;
import org.openhab.binding.innogysmarthome.internal.client.entity.state.DateTimeState;
import org.openhab.binding.innogysmarthome.internal.client.entity.state.DoubleState;
import org.openhab.binding.innogysmarthome.internal.client.entity.state.IntegerState;
import org.openhab.binding.innogysmarthome.internal.client.entity.state.StringState;

import com.google.api.client.util.Key;

/**
 * Holds the Capability state.
 *
 * @author Oliver Kuhl - Initial contribution
 *
 */
public class State {

    @Key("absoluteEnergyConsumption")
    private DoubleState absoluteEnergyConsumptionState;

    @Key("activeChannel")
    private StringState activeChannelState;

    @Key("dimLevel")
    private IntegerState dimLevelState;

    @Key("energyConsumptionDayEuro")
    private DoubleState energyConsumptionDayEuroState;

    @Key("energyConsumptionDayKWh")
    private DoubleState energyConsumptionDayKWhState;

    @Key("energyConsumptionMonthEuro")
    private DoubleState energyConsumptionMonthEuroState;

    @Key("energyConsumptionMonthKWh")
    private DoubleState energyConsumptionMonthKWhState;

    @Key("energyPerDayInEuro")
    private DoubleState energyPerDayInEuroState;

    @Key("energyPerDayInKWh")
    private DoubleState energyPerDayInKWhState;

    @Key("energyPerMonthInEuro")
    private DoubleState energyPerMonthInEuroState;

    @Key("energyPerMonthInKWh")
    private DoubleState energyPerMonthInKWhState;

    @Key("frostWarning")
    private BooleanState frostWarningState;

    @Key("humidity")
    private DoubleState humidityState;

    @Key("isDay")
    private BooleanState isDayState;

    @Key("isOn")
    private BooleanState isOnState;

    @Key("isOpen")
    private BooleanState isOpenState;

    @Key("isSmokeAlarm")
    private BooleanState isSmokeAlarmState;

    @Key("lastKeyPressCounter")
    private IntegerState lastKeyPressCounterState;

    @Key("lastPressedButtonIndex")
    private IntegerState lastPressedButtonIndex;

    @Key("luminance")
    private DoubleState luminanceState;

    @Key("moldWarning")
    private BooleanState moldWarningState;

    @Key("motionDetectedCount")
    private IntegerState motionDetectedCountState;

    @Key("nextSunrise")
    private DateTimeState nextSunrise;

    @Key("nextSunset")
    private DateTimeState nextSunsetState;

    @Key("nextTimeEvent")
    private DateTimeState nextTimeEventState;

    @Key("onState")
    private BooleanState onState;

    @Key("operationMode")
    private StringState operationModeState;

    @Key("pointTemperature")
    private DoubleState pointTemperatureState;

    @Key("powerConsumptionWatt")
    private DoubleState powerConsumptionWattState;

    @Key("powerInWatt")
    private DoubleState powerInWattState;

    @Key("shutterLevel")
    private IntegerState shutterLevelState;

    @Key("supplyValueInCubicMetterPerDay")
    private DoubleState supplyValueInCubicMetterPerDayState;

    @Key("supplyValueInCubicMetterPerMonth")
    private DoubleState supplyValueInCubicMetterPerMonthState;

    @Key("supplyValueInCurrencyPerDay")
    private DoubleState supplyValueInCurrencyPerDayState;

    @Key("supplyValueInCurrencyPerMonth")
    private DoubleState supplyValueInCurrencyPerMonthState;

    @Key("supplyValueInLitrePerDay")
    private DoubleState supplyValueInLitrePerDayState;

    @Key("supplyValueInLitrePerMonth")
    private DoubleState supplyValueInLitrePerMonthState;

    @Key("temperature")
    private DoubleState temperatureState;

    @Key("totalEnergy")
    private DoubleState totalEnergyState;

    @Key("value")
    private BooleanState valueState;

    @Key("valvePosition")
    private BooleanState valvePositionState;

    @Key("windowReductionActive")
    private BooleanState windowReductionActiveState;

    private String name;
    private String value;
    private String lastChanged;

    /**
     * @param name
     * @param value
     * @param lastChanged
     */
    private void setGeneralState(String name, String value, String lastChanged) {
        this.name = name;
        this.value = value;
        this.lastChanged = lastChanged;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @return the lastChanged
     */
    public String getLastChanged() {
        return lastChanged;
    }

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
        setGeneralState("absoluteEnergyConsumptionState", state.getValue().toString(), state.getLastChanged());
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
        setGeneralState("activeChannelState", state.getValue().toString(), state.getLastChanged());
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
        setGeneralState("dimLevelState", state.getValue().toString(), state.getLastChanged());

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
        setGeneralState("energyConsumptionDayEuroState", state.getValue().toString(), state.getLastChanged());
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
        setGeneralState("isOnState", state.getValue().toString(), state.getLastChanged());
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
        setGeneralState("isOpenState", state.getValue().toString(), state.getLastChanged());
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
