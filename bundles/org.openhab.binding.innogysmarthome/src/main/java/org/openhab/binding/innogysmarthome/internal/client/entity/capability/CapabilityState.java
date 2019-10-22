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

import com.google.api.client.util.Key;

/**
 * Defines the {@link CapabilityState}, that holds the state of a {@link Capability}, e.g. a temperature.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class CapabilityState /* extends EntityState */ {
    public static final String STATE_NAME_VARIABLE_ACTUATOR = "Value";
    public static final String STATE_NAME_SWITCH_ACTUATOR = "OnState";
    public static final String STATE_NAME_TEMPERATURE_SENSOR_TEMPERATURE = "Temperature";
    public static final String STATE_NAME_TEMPERATURE_SENSOR_FROST_WARNING = "FrostWarning";
    public static final String STATE_NAME_THERMOSTAT_ACTUATOR_POINT_TEMPERATURE = "PointTemperature";
    public static final String STATE_NAME_THERMOSTAT_ACTUATOR_OPERATION_MODE = "OperationMode";
    public static final String STATE_NAME_THERMOSTAT_ACTUATOR_WINDOW_REDUCTION_ACTIVE = "WindowReductionActive";
    public static final String STATE_NAME_HUMIDITY_SENSOR_HUMIDITY = "Humidity";
    public static final String STATE_NAME_HUMIDITY_SENSOR_MOLD_WARNING = "MoldWarning";
    public static final String STATE_NAME_WINDOW_DOOR_SENSOR = "IsOpen";
    public static final String STATE_NAME_SMOKE_DETECTOR_SENSOR = "IsSmokeAlarm";
    public static final String STATE_NAME_ALARM_ACTUATOR = "OnState";
    public static final String STATE_NAME_MOTION_DETECTION_SENSOR = "MotionDetectedCount";
    public static final String STATE_NAME_LUMINANCE_SENSOR = "Luminance";
    public static final String STATE_NAME_PUSH_BUTTON_SENSOR_COUNTER = "LastKeyPressCounter";
    public static final String STATE_NAME_PUSH_BUTTON_SENSOR_BUTTON_INDEX = "LastPressedButtonIndex";

    // ENERGY CONSUMPTION SENSOR
    public static final String STATE_NAME_ENERGY_CONSUMPTION_SENSOR_ENERGY_CONSUMPTION_MONTH_KWH = "EnergyConsumptionMonthKWh";
    public static final String STATE_NAME_ENERGY_CONSUMPTION_SENSOR_ABSOLUTE_ENERGY_CONSUMPTION = "AbsoluteEnergyConsumption";
    public static final String STATE_NAME_ENERGY_CONSUMPTION_SENSOR_ENERGY_CONSUMPTION_MONTH_EURO = "EnergyConsumptionMonthEuro";
    public static final String STATE_NAME_ENERGY_CONSUMPTION_SENSOR_ENERGY_CONSUMPTION_DAY_EURO = "EnergyConsumptionDayEuro";
    public static final String STATE_NAME_ENERGY_CONSUMPTION_SENSOR_ENERGY_CONSUMPTION_DAY_KWH = "EnergyConsumptionDayKWh";
    // POWER CONSUMPTION SENSOR
    public static final String STATE_NAME_POWER_CONSUMPTION_SENSOR_POWER_CONSUMPTION_WATT = "PowerConsumptionWatt";
    // GENERATION METER ENERGY SENSOR
    public static final String STATE_NAME_GENERATION_METER_ENERGY_SENSOR_ENERGY_PER_MONTH_IN_KWH = "EnergyPerMonthInKWh";
    public static final String STATE_NAME_GENERATION_METER_ENERGY_SENSOR_TOTAL_ENERGY = "TotalEnergy";
    public static final String STATE_NAME_GENERATION_METER_ENERGY_SENSOR_ENERGY_PER_MONTH_IN_EURO = "EnergyPerMonthInEuro";
    public static final String STATE_NAME_GENERATION_METER_ENERGY_SENSOR_ENERGY_PER_DAY_IN_EURO = "EnergyPerDayInEuro";
    public static final String STATE_NAME_GENERATION_METER_ENERGY_SENSOR_ENERGY_PER_DAY_IN_KWH = "EnergyPerDayInKWh";
    // GENERATION METER POWER CONSUMPTION SENSOR
    public static final String STATE_NAME_GENERATION_METER_POWER_CONSUMPTION_SENSOR_POWER_IN_WATT = "PowerInWatt";
    // TWO WAY METER ENERGY CONSUMPTION SENSOR
    public static final String STATE_NAME_TWO_WAY_METER_ENERGY_CONSUMPTION_SENSOR_ENERGY_PER_MONTH_IN_KWH = "EnergyPerMonthInKWh";
    public static final String STATE_NAME_TWO_WAY_METER_ENERGY_CONSUMPTION_SENSOR_TOTAL_ENERGY = "TotalEnergy";
    public static final String STATE_NAME_TWO_WAY_METER_ENERGY_CONSUMPTION_SENSOR_ENERGY_PER_MONTH_IN_EURO = "EnergyPerMonthInEuro";
    public static final String STATE_NAME_TWO_WAY_METER_ENERGY_CONSUMPTION_SENSOR_ENERGY_PER_DAY_IN_EURO = "EnergyPerDayInEuro";
    public static final String STATE_NAME_TWO_WAY_METER_ENERGY_CONSUMPTION_SENSOR_ENERGY_PER_DAY_IN_KWH = "EnergyPerDayInKWh";
    // TWO WAY METER ENERGY FEED SENSOR
    public static final String STATE_NAME_TWO_WAY_METER_ENERGY_FEED_SENSOR_ENERGY_PER_MONTH_IN_KWH = "EnergyPerMonthInKWh";
    public static final String STATE_NAME_TWO_WAY_METER_ENERGY_FEED_SENSOR_TOTAL_ENERGY = "TotalEnergy";
    public static final String STATE_NAME_TWO_WAY_METER_ENERGY_FEED_SENSOR_ENERGY_PER_MONTH_IN_EURO = "EnergyPerMonthInEuro";
    public static final String STATE_NAME_TWO_WAY_METER_ENERGY_FEED_SENSOR_ENERGY_PER_DAY_IN_EURO = "EnergyPerDayInEuro";
    public static final String STATE_NAME_TWO_WAY_METER_ENERGY_FEED_SENSOR_ENERGY_PER_DAY_IN_KWH = "EnergyPerDayInKWh";
    // TWO WAY METER POWER CONSUMPTION SENSOR
    public static final String STATE_NAME_TWO_WAY_METER_POWER_CONSUMPTION_SENSOR_POWER_IN_WATT = "PowerInWatt";

    public static final String STATE_VALUE_OPERATION_MODE_AUTO = "Auto";
    public static final String STATE_VALUE_OPERATION_MODE_MANUAL = "Manu";

    /**
     * id of the {@link Capability}
     */
    @Key("id")
    private String id;

    /**
     * class containing all states
     */
    @Key("state")
    private State state;

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the state
     */
    public State getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(State state) {
        this.state = state;
    }

    public Boolean getVariableActuatorState() {
        return getState().getValueState().getValue();
    }

    public void setVariableActuatorState(boolean on) {
        getState().getValueState().setValue(on);
    }

    public Boolean getSwitchActuatorState() {
        return getState().getOnState().getValue();
    }

    public void setSwitchActuatorState(boolean on) {
        getState().getOnState().setValue(on);
    }

    public Double getTemperatureSensorTemperatureState() {
        return getState().getTemperatureState().getValue();
    }

    public void setTemperatureSensorTemperatureState(double temperature) {
        getState().getTemperatureState().setValue(temperature);
    }

    public Boolean getTemperatureSensorFrostWarningState() {
        return getState().getFrostWarningState().getValue();
    }

    public void setTemperatureSensorFrostWarningState(boolean frostWarning) {
        getState().getFrostWarningState().setValue(frostWarning);
    }

    public Double getThermostatActuatorPointTemperatureState() {
        return getState().getPointTemperatureState().getValue();
    }

    public void setThermostatActuatorPointTemperatureState(double pointTemperature) {
        getState().getPointTemperatureState().setValue(pointTemperature);
    }

    public String getThermostatActuatorOperationModeState() {
        return getState().getOperationModeState().getValue();
    }

    public void setThermostatActuatorOperationModeState(String operationMode) {
        if (operationMode.equals(STATE_VALUE_OPERATION_MODE_MANUAL)) {
            getState().getOperationModeState().setValue(STATE_VALUE_OPERATION_MODE_MANUAL);
        } else {
            getState().getOperationModeState().setValue(STATE_VALUE_OPERATION_MODE_AUTO);
        }
    }

    public Boolean getThermostatActuatorWindowReductionActiveState() {
        return getState().getWindowReductionActiveState().getValue();
    }

    public void setThermostatActuatorWindowReductionActiveState(boolean windowReductionActive) {
        getState().getWindowReductionActiveState().setValue(windowReductionActive);
    }

    public Double getHumiditySensorHumidityState() {
        return getState().getHumidityState().getValue();
    }

    public void setHumiditySensorHumidityState(double humidity) {
        getState().getHumidityState().setValue(humidity);
    }

    public Boolean getHumiditySensorMoldWarningState() {
        return getState().getMoldWarningState().getValue();
    }

    public void setHumiditySensorMoldWarningState(boolean moldWarning) {
        getState().getMoldWarningState().setValue(moldWarning);
    }

    public Boolean getWindowDoorSensorState() {
        return getState().getIsOpenState().getValue();
    }

    public void setWindowDoorSensorState(boolean open) {
        getState().getIsOpenState().setValue(open);
    }

    public Boolean getSmokeDetectorSensorState() {
        return getState().getIsSmokeAlarmState().getValue();
    }

    public void setSmokeDetectorSensorState(boolean on) {
        getState().getIsSmokeAlarmState().setValue(on);
    }

    public Boolean getAlarmActuatorState() {
        return getState().getOnState().getValue();
    }

    public void setAlarmActuatorState(boolean on) {
        getState().getOnState().setValue(on);
    }

    public Integer getMotionDetectionSensorState() {
        return getState().getMotionDetectedCountState().getValue();
    }

    public void setMotionDetectionSensorState(Integer numberOfMotions) {
        getState().getMotionDetectedCountState().setValue(numberOfMotions);
    }

    public Double getLuminanceSensorState() {
        return getState().getLuminanceState().getValue();
    }

    public void setLuminanceSensorState(double luminance) {
        getState().getLuminanceState().setValue(luminance);
    }

    public Integer getPushButtonSensorCounterState() {
        return getState().getLastKeyPressCounterState().getValue();
    }

    public void setPushButtonSensorCounterState(Integer numberOfPresses) {
        getState().getLastKeyPressCounterState().setValue(numberOfPresses);
    }

    public Integer getPushButtonSensorButtonIndexState() {
        return getState().getLastPressedButtonIndex().getValue();
    }

    public void setPushButtonSensorButtonIndexState(Integer buttonIndex) {
        getState().getLastPressedButtonIndex().setValue(buttonIndex);
    }

    public Integer getDimmerActuatorState() {
        return getState().getDimLevelState().getValue();
    }

    public void setDimmerActuatorState(Integer DimLevel) {
        getState().getDimLevelState().setValue(DimLevel);
    }

    public Integer getRollerShutterActuatorState() {
        return getState().getShutterLevelState().getValue();
    }

    public void setRollerShutterActuatorState(Integer rollerShutterLevel) {
        getState().getShutterLevelState().setValue(rollerShutterLevel);
    }

    // ENERGY CONSUMPTION SENSOR
    public Double getEnergyConsumptionSensorEnergyConsumptionMonthKWhState() {
        return getState().getEnergyConsumptionMonthKWhState().getValue();
    }

    public void setEnergyConsumptionSensorEnergyConsumptionMonthKWhState(double state) {
        getState().getEnergyConsumptionMonthKWhState().setValue(state);
    }

    public Double getEnergyConsumptionSensorAbsoluteEnergyConsumptionState() {
        return getState().getAbsoluteEnergyConsumptionState().getValue();
    }

    public void setEnergyConsumptionSensorAbsoluteEnergyConsumptionState(double state) {
        getState().getAbsoluteEnergyConsumptionState().setValue(state);
    }

    public Double getEnergyConsumptionSensorEnergyConsumptionMonthEuroState() {
        return getState().getEnergyConsumptionMonthEuroState().getValue();
    }

    public void setEnergyConsumptionSensorEnergyConsumptionMonthEuroState(double state) {
        getState().getEnergyConsumptionMonthEuroState().setValue(state);
    }

    public Double getEnergyConsumptionSensorEnergyConsumptionDayEuroState() {
        return getState().getEnergyConsumptionDayEuroState().getValue();
    }

    public void setEnergyConsumptionSensorEnergyConsumptionDayEuroState(double state) {
        getState().getEnergyConsumptionDayEuroState().setValue(state);
    }

    public Double getEnergyConsumptionSensorEnergyConsumptionDayKWhState() {
        return getState().getEnergyConsumptionDayKWhState().getValue();
    }

    public void setEnergyConsumptionSensorEnergyConsumptionDayKWhState(double state) {
        getState().getEnergyConsumptionDayKWhState().setValue(state);
    }

    // POWER CONSUMPTION SENSOR
    public Double getPowerConsumptionSensorPowerConsumptionWattState() {
        return getState().getPowerConsumptionWattState().getValue();
    }

    public void setPowerConsumptionSensorPowerConsumptionWattState(double state) {
        getState().getPowerConsumptionWattState().setValue(state);
    }

    // GENERATION METER ENGERY SENSOR
    public Double getGenerationMeterEnergySensorEnergyPerMonthInKWhState() {
        return getState().getEnergyPerMonthInKWhState().getValue();
    }

    public void setGenerationMeterEnergySensorEnergyPerMonthInKWhState(double state) {
        getState().getEnergyPerMonthInKWhState().setValue(state);
    }

    public Double getGenerationMeterEnergySensorTotalEnergyState() {
        return getState().getTotalEnergyState().getValue();
    }

    public void setGenerationMeterEnergySensorTotalEnergyState(double state) {
        getState().getTotalEnergyState().setValue(state);
    }

    public Double getGenerationMeterEnergySensorEnergyPerMonthInEuroState() {
        return getState().getEnergyPerMonthInEuroState().getValue();
    }

    public void setGenerationMeterEnergySensorEnergyPerMonthInEuroState(double state) {
        getState().getEnergyPerMonthInEuroState().setValue(state);
    }

    public Double getGenerationMeterEnergySensorEnergyPerDayInEuroState() {
        return getState().getEnergyPerDayInEuroState().getValue();
    }

    public void setGenerationMeterEnergySensorEnergyPerDayInEuroState(double state) {
        getState().getEnergyPerDayInEuroState().setValue(state);
    }

    public Double getGenerationMeterEnergySensorEnergyPerDayInKWhState() {
        return getState().getEnergyPerDayInKWhState().getValue();
    }

    public void setGenerationMeterEnergySensorEnergyPerDayInKWhState(double state) {
        getState().getEnergyPerDayInKWhState().setValue(state);
    }

    // GENERATION METER POWER CONSUMPTION SENSOR
    public Double getGenerationMeterPowerConsumptionSensorPowerInWattState() {
        return getState().getPowerInWattState().getValue();
    }

    public void setGenerationMeterPowerConsumptionSensorPowerInWattState(double state) {
        getState().getPowerInWattState().setValue(state);
    }

    // TWO WAY METER ENERGY CONSUMPTION SENSOR
    public Double getTwoWayMeterEnergyConsumptionSensorEnergyPerMonthInKWhState() {
        return getState().getEnergyPerMonthInKWhState().getValue();
    }

    public void setTwoWayMeterEnergyConsumptionSensorEnergyPerMonthInKWhState(double state) {
        getState().getEnergyPerMonthInKWhState().setValue(state);
    }

    public Double getTwoWayMeterEnergyConsumptionSensorTotalEnergyState() {
        return getState().getTotalEnergyState().getValue();
    }

    public void setTwoWayMeterEnergyConsumptionSensorTotalEnergyState(double state) {
        getState().getTotalEnergyState().setValue(state);
    }

    public Double getTwoWayMeterEnergyConsumptionSensorEnergyPerMonthInEuroState() {
        return getState().getEnergyPerMonthInEuroState().getValue();
    }

    public void setTwoWayMeterEnergyConsumptionSensorEnergyPerMonthInEuroState(double state) {
        getState().getEnergyPerMonthInEuroState().setValue(state);
    }

    public Double getTwoWayMeterEnergyConsumptionSensorEnergyPerDayInEuroState() {
        return getState().getEnergyPerDayInEuroState().getValue();
    }

    public void setTwoWayMeterEnergyConsumptionSensorEnergyPerDayInEuroState(double state) {
        getState().getEnergyPerDayInEuroState().setValue(state);
    }

    public Double getTwoWayMeterEnergyConsumptionSensorEnergyPerDayInKWhState() {
        return getState().getEnergyPerDayInKWhState().getValue();
    }

    public void setTwoWayMeterEnergyConsumptionSensorEnergyPerDayInKWhState(double state) {
        getState().getEnergyPerDayInKWhState().setValue(state);
    }

    // TWO WAY METER ENERGY FEED SENSOR
    public Double getTwoWayMeterEnergyFeedSensorEnergyPerMonthInKWhState() {
        return getState().getEnergyPerMonthInKWhState().getValue();
    }

    public void setTwoWayMeterEnergyFeedSensorEnergyPerMonthInKWhState(double state) {
        getState().getEnergyPerMonthInKWhState().setValue(state);
    }

    public Double getTwoWayMeterEnergyFeedSensorTotalEnergyState() {
        return getState().getTotalEnergyState().getValue();
    }

    public void setTwoWayMeterEnergyFeedSensorTotalEnergyState(double state) {
        getState().getTotalEnergyState().setValue(state);
    }

    public Double getTwoWayMeterEnergyFeedSensorEnergyPerMonthInEuroState() {
        return getState().getEnergyPerMonthInEuroState().getValue();
    }

    public void setTwoWayMeterEnergyFeedSensorEnergyPerMonthInEuroState(double state) {
        getState().getEnergyPerMonthInEuroState().setValue(state);
    }

    public Double getTwoWayMeterEnergyFeedSensorEnergyPerDayInEuroState() {
        return getState().getEnergyPerDayInEuroState().getValue();
    }

    public void setTwoWayMeterEnergyFeedSensorEnergyPerDayInEuroState(double state) {
        getState().getEnergyPerDayInEuroState().setValue(state);
    }

    public Double getTwoWayMeterEnergyFeedSensorEnergyPerDayInKWhState() {
        return getState().getEnergyPerDayInKWhState().getValue();
    }

    public void setTwoWayMeterEnergyFeedSensorEnergyPerDayInKWhState(double state) {
        getState().getEnergyPerDayInKWhState().setValue(state);
    }

    // TWO WAY METER POWER CONSUMPTION SENSOR
    public Double getTwoWayMeterPowerConsumptionSensorPowerInWattState() {
        return getState().getPowerInWattState().getValue();
    }

    public void setTwoWayMeterPowerConsumptionSensorPowerInWattState(double state) {
        getState().getPowerInWattState().setValue(state);
    }

}
