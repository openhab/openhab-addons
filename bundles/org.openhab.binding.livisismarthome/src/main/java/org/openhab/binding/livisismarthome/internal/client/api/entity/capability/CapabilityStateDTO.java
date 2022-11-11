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

/**
 * Defines the {@link CapabilityStateDTO}, that holds the state of a {@link CapabilityDTO}, e.g. a temperature.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class CapabilityStateDTO {

    public static final String STATE_VALUE_OPERATION_MODE_AUTO = "Auto";
    public static final String STATE_VALUE_OPERATION_MODE_MANUAL = "Manu";

    /**
     * id of the {@link CapabilityDTO}
     */
    private String id;

    /**
     * class containing all states
     */
    private StateDTO state;

    public CapabilityStateDTO() {
        state = new StateDTO();
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * @return the state
     */
    public StateDTO getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(final StateDTO state) {
        this.state = state;
    }

    public Boolean getVariableActuatorState() {
        return getState().getValueState().getValue();
    }

    public void setVariableActuatorState(final Boolean on) {
        getState().getValueState().setValue(on);
    }

    public Boolean getSwitchActuatorState() {
        return getState().getOnState().getValue();
    }

    public void setSwitchActuatorState(final Boolean on) {
        getState().getOnState().setValue(on);
    }

    public Double getTemperatureSensorTemperatureState() {
        return getState().getTemperatureState().getValue();
    }

    public void setTemperatureSensorTemperatureState(final Double temperature) {
        getState().getTemperatureState().setValue(temperature);
    }

    public Boolean getTemperatureSensorFrostWarningState() {
        return getState().getFrostWarningState().getValue();
    }

    public void setTemperatureSensorFrostWarningState(final Boolean frostWarning) {
        getState().getFrostWarningState().setValue(frostWarning);
    }

    public Double getThermostatActuatorPointTemperatureState() {
        return getState().getPointTemperatureState().getValue();
    }

    public void setThermostatActuatorPointTemperatureState(final Double pointTemperature) {
        getState().getPointTemperatureState().setValue(pointTemperature);
    }

    public String getThermostatActuatorOperationModeState() {
        return getState().getOperationModeState().getValue();
    }

    public void setThermostatActuatorOperationModeState(final String operationMode) {
        if (STATE_VALUE_OPERATION_MODE_MANUAL.equals(operationMode)) {
            getState().getOperationModeState().setValue(STATE_VALUE_OPERATION_MODE_MANUAL);
        } else {
            getState().getOperationModeState().setValue(STATE_VALUE_OPERATION_MODE_AUTO);
        }
    }

    public Boolean getThermostatActuatorWindowReductionActiveState() {
        return getState().getWindowReductionActiveState().getValue();
    }

    public void setThermostatActuatorWindowReductionActiveState(final Boolean windowReductionActive) {
        getState().getWindowReductionActiveState().setValue(windowReductionActive);
    }

    public Double getHumiditySensorHumidityState() {
        return getState().getHumidityState().getValue();
    }

    public void setHumiditySensorHumidityState(final Double humidity) {
        getState().getHumidityState().setValue(humidity);
    }

    public Boolean getHumiditySensorMoldWarningState() {
        return getState().getMoldWarningState().getValue();
    }

    public void setHumiditySensorMoldWarningState(final Boolean moldWarning) {
        getState().getMoldWarningState().setValue(moldWarning);
    }

    public Boolean getWindowDoorSensorState() {
        return getState().getIsOpenState().getValue();
    }

    public void setWindowDoorSensorState(final Boolean open) {
        getState().getIsOpenState().setValue(open);
    }

    public Boolean getSmokeDetectorSensorState() {
        return getState().getIsSmokeAlarmState().getValue();
    }

    public void setSmokeDetectorSensorState(final Boolean on) {
        getState().getIsSmokeAlarmState().setValue(on);
    }

    public Boolean getAlarmActuatorState() {
        return getState().getOnState().getValue();
    }

    public void setAlarmActuatorState(final Boolean on) {
        getState().getOnState().setValue(on);
    }

    public Integer getMotionDetectionSensorState() {
        return getState().getMotionDetectedCountState().getValue();
    }

    public void setMotionDetectionSensorState(final Integer numberOfMotions) {
        getState().getMotionDetectedCountState().setValue(numberOfMotions);
    }

    public Double getLuminanceSensorState() {
        return getState().getLuminanceState().getValue();
    }

    public void setLuminanceSensorState(final Double luminance) {
        getState().getLuminanceState().setValue(luminance);
    }

    public Integer getPushButtonSensorCounterState() {
        return getState().getLastKeyPressCounterState().getValue();
    }

    public void setPushButtonSensorCounterState(final Integer numberOfPresses) {
        getState().getLastKeyPressCounterState().setValue(numberOfPresses);
    }

    public Integer getPushButtonSensorButtonIndexState() {
        return getState().getLastPressedButtonIndex().getValue();
    }

    public void setPushButtonSensorButtonIndexState(final Integer buttonIndex) {
        getState().getLastPressedButtonIndex().setValue(buttonIndex);
    }

    public String getPushButtonSensorButtonIndexType() {
        return getState().getLastKeyPressType().getValue();
    }

    public void setPushButtonSensorButtonIndexType(String lastKeyPressType) {
        getState().getLastKeyPressType().setValue(lastKeyPressType);
    }

    public Integer getDimmerActuatorState() {
        return getState().getDimLevelState().getValue();
    }

    public void setDimmerActuatorState(final Integer DimLevel) {
        getState().getDimLevelState().setValue(DimLevel);
    }

    public Integer getRollerShutterActuatorState() {
        return getState().getShutterLevelState().getValue();
    }

    public void setRollerShutterActuatorState(final Integer rollerShutterLevel) {
        getState().getShutterLevelState().setValue(rollerShutterLevel);
    }

    // ENERGY CONSUMPTION SENSOR
    public Double getEnergyConsumptionSensorEnergyConsumptionMonthKWhState() {
        return getState().getEnergyConsumptionMonthKWhState().getValue();
    }

    public void setEnergyConsumptionSensorEnergyConsumptionMonthKWhState(final Double state) {
        getState().getEnergyConsumptionMonthKWhState().setValue(state);
    }

    public Double getEnergyConsumptionSensorAbsoluteEnergyConsumptionState() {
        return getState().getAbsoluteEnergyConsumptionState().getValue();
    }

    public void setEnergyConsumptionSensorAbsoluteEnergyConsumptionState(final Double state) {
        getState().getAbsoluteEnergyConsumptionState().setValue(state);
    }

    public Double getEnergyConsumptionSensorEnergyConsumptionMonthEuroState() {
        return getState().getEnergyConsumptionMonthEuroState().getValue();
    }

    public void setEnergyConsumptionSensorEnergyConsumptionMonthEuroState(final Double state) {
        getState().getEnergyConsumptionMonthEuroState().setValue(state);
    }

    public Double getEnergyConsumptionSensorEnergyConsumptionDayEuroState() {
        return getState().getEnergyConsumptionDayEuroState().getValue();
    }

    public void setEnergyConsumptionSensorEnergyConsumptionDayEuroState(final Double state) {
        getState().getEnergyConsumptionDayEuroState().setValue(state);
    }

    public Double getEnergyConsumptionSensorEnergyConsumptionDayKWhState() {
        return getState().getEnergyConsumptionDayKWhState().getValue();
    }

    public void setEnergyConsumptionSensorEnergyConsumptionDayKWhState(final Double state) {
        getState().getEnergyConsumptionDayKWhState().setValue(state);
    }

    // POWER CONSUMPTION SENSOR
    public Double getPowerConsumptionSensorPowerConsumptionWattState() {
        return getState().getPowerConsumptionWattState().getValue();
    }

    public void setPowerConsumptionSensorPowerConsumptionWattState(final Double state) {
        getState().getPowerConsumptionWattState().setValue(state);
    }

    // GENERATION METER ENGERY SENSOR
    public Double getGenerationMeterEnergySensorEnergyPerMonthInKWhState() {
        return getState().getEnergyPerMonthInKWhState().getValue();
    }

    public void setGenerationMeterEnergySensorEnergyPerMonthInKWhState(final Double state) {
        getState().getEnergyPerMonthInKWhState().setValue(state);
    }

    public Double getGenerationMeterEnergySensorTotalEnergyState() {
        return getState().getTotalEnergyState().getValue();
    }

    public void setGenerationMeterEnergySensorTotalEnergyState(final Double state) {
        getState().getTotalEnergyState().setValue(state);
    }

    public Double getGenerationMeterEnergySensorEnergyPerMonthInEuroState() {
        return getState().getEnergyPerMonthInEuroState().getValue();
    }

    public void setGenerationMeterEnergySensorEnergyPerMonthInEuroState(final Double state) {
        getState().getEnergyPerMonthInEuroState().setValue(state);
    }

    public Double getGenerationMeterEnergySensorEnergyPerDayInEuroState() {
        return getState().getEnergyPerDayInEuroState().getValue();
    }

    public void setGenerationMeterEnergySensorEnergyPerDayInEuroState(final Double state) {
        getState().getEnergyPerDayInEuroState().setValue(state);
    }

    public Double getGenerationMeterEnergySensorEnergyPerDayInKWhState() {
        return getState().getEnergyPerDayInKWhState().getValue();
    }

    public void setGenerationMeterEnergySensorEnergyPerDayInKWhState(final Double state) {
        getState().getEnergyPerDayInKWhState().setValue(state);
    }

    // GENERATION METER POWER CONSUMPTION SENSOR
    public Double getGenerationMeterPowerConsumptionSensorPowerInWattState() {
        return getState().getPowerInWattState().getValue();
    }

    public void setGenerationMeterPowerConsumptionSensorPowerInWattState(final Double state) {
        getState().getPowerInWattState().setValue(state);
    }

    // TWO WAY METER ENERGY CONSUMPTION SENSOR
    public Double getTwoWayMeterEnergyConsumptionSensorEnergyPerMonthInKWhState() {
        return getState().getEnergyPerMonthInKWhState().getValue();
    }

    public void setTwoWayMeterEnergyConsumptionSensorEnergyPerMonthInKWhState(final Double state) {
        getState().getEnergyPerMonthInKWhState().setValue(state);
    }

    public Double getTwoWayMeterEnergyConsumptionSensorTotalEnergyState() {
        return getState().getTotalEnergyState().getValue();
    }

    public void setTwoWayMeterEnergyConsumptionSensorTotalEnergyState(final Double state) {
        getState().getTotalEnergyState().setValue(state);
    }

    public Double getTwoWayMeterEnergyConsumptionSensorEnergyPerMonthInEuroState() {
        return getState().getEnergyPerMonthInEuroState().getValue();
    }

    public void setTwoWayMeterEnergyConsumptionSensorEnergyPerMonthInEuroState(final Double state) {
        getState().getEnergyPerMonthInEuroState().setValue(state);
    }

    public Double getTwoWayMeterEnergyConsumptionSensorEnergyPerDayInEuroState() {
        return getState().getEnergyPerDayInEuroState().getValue();
    }

    public void setTwoWayMeterEnergyConsumptionSensorEnergyPerDayInEuroState(final Double state) {
        getState().getEnergyPerDayInEuroState().setValue(state);
    }

    public Double getTwoWayMeterEnergyConsumptionSensorEnergyPerDayInKWhState() {
        return getState().getEnergyPerDayInKWhState().getValue();
    }

    public void setTwoWayMeterEnergyConsumptionSensorEnergyPerDayInKWhState(final Double state) {
        getState().getEnergyPerDayInKWhState().setValue(state);
    }

    // TWO WAY METER ENERGY FEED SENSOR
    public Double getTwoWayMeterEnergyFeedSensorEnergyPerMonthInKWhState() {
        return getState().getEnergyPerMonthInKWhState().getValue();
    }

    public void setTwoWayMeterEnergyFeedSensorEnergyPerMonthInKWhState(final Double state) {
        getState().getEnergyPerMonthInKWhState().setValue(state);
    }

    public Double getTwoWayMeterEnergyFeedSensorTotalEnergyState() {
        return getState().getTotalEnergyState().getValue();
    }

    public void setTwoWayMeterEnergyFeedSensorTotalEnergyState(final Double state) {
        getState().getTotalEnergyState().setValue(state);
    }

    public Double getTwoWayMeterEnergyFeedSensorEnergyPerMonthInEuroState() {
        return getState().getEnergyPerMonthInEuroState().getValue();
    }

    public void setTwoWayMeterEnergyFeedSensorEnergyPerMonthInEuroState(final Double state) {
        getState().getEnergyPerMonthInEuroState().setValue(state);
    }

    public Double getTwoWayMeterEnergyFeedSensorEnergyPerDayInEuroState() {
        return getState().getEnergyPerDayInEuroState().getValue();
    }

    public void setTwoWayMeterEnergyFeedSensorEnergyPerDayInEuroState(final Double state) {
        getState().getEnergyPerDayInEuroState().setValue(state);
    }

    public Double getTwoWayMeterEnergyFeedSensorEnergyPerDayInKWhState() {
        return getState().getEnergyPerDayInKWhState().getValue();
    }

    public void setTwoWayMeterEnergyFeedSensorEnergyPerDayInKWhState(final Double state) {
        getState().getEnergyPerDayInKWhState().setValue(state);
    }

    // TWO WAY METER POWER CONSUMPTION SENSOR
    public Double getTwoWayMeterPowerConsumptionSensorPowerInWattState() {
        return getState().getPowerInWattState().getValue();
    }

    public void setTwoWayMeterPowerConsumptionSensorPowerInWattState(final Double state) {
        getState().getPowerInWattState().setValue(state);
    }
}
