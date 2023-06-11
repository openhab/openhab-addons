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
package org.openhab.binding.livisismarthome.internal.client.api.entity.capability;

/**
 * Defines the structure of a {@link CapabilityDTO}. A capability is a specific functionality of a device like a
 * temperature sensor.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class CapabilityDTO {

    /** capability types */
    public static final String TYPE_SWITCHACTUATOR = "SwitchActuator";
    public static final String TYPE_VARIABLEACTUATOR = "BooleanStateActuator";
    public static final String TYPE_THERMOSTATACTUATOR = "ThermostatActuator";
    public static final String TYPE_TEMPERATURESENSOR = "TemperatureSensor";
    public static final String TYPE_HUMIDITYSENSOR = "HumiditySensor";
    public static final String TYPE_WINDOWDOORSENSOR = "WindowDoorSensor";
    public static final String TYPE_SMOKEDETECTORSENSOR = "SmokeDetectorSensor";
    public static final String TYPE_ALARMACTUATOR = "AlarmActuator";
    public static final String TYPE_MOTIONDETECTIONSENSOR = "MotionDetectionSensor";
    public static final String TYPE_LUMINANCESENSOR = "LuminanceSensor";
    public static final String TYPE_PUSHBUTTONSENSOR = "PushButtonSensor";
    public static final String TYPE_DIMMERACTUATOR = "DimmerActuator";
    public static final String TYPE_ROLLERSHUTTERACTUATOR = "RollerShutterActuator";
    public static final String TYPE_ENERGYCONSUMPTIONSENSOR = "EnergyConsumptionSensor";
    public static final String TYPE_POWERCONSUMPTIONSENSOR = "PowerConsumptionSensor";
    public static final String TYPE_GENERATIONMETERENERGYSENSOR = "GenerationMeterEnergySensor";
    public static final String TYPE_GENERATIONMETERPOWERCONSUMPTIONSENSOR = "GenerationMeterPowerConsumptionSensor";
    public static final String TYPE_TWOWAYMETERENERGYCONSUMPTIONSENSOR = "TwoWayMeterEnergyConsumptionSensor";
    public static final String TYPE_TWOWAYMETERENERGYFEEDSENSOR = "TwoWayMeterEnergyFeedSensor";
    public static final String TYPE_TWOWAYMETERPOWERCONSUMPTIONSENSOR = "TwoWayMeterPowerConsumptionSensor";

    /**
     * Unique id for the Capability.
     */
    private String id;

    /**
     * Type of the capability – must be unique per device, since the device links to the capability via the type.
     */
    private String type;

    /**
     * Contains the link to the parent device, which offers the capability.
     */
    private String device;

    /**
     * This represents a container of all configuration properties.
     */
    private CapabilityConfigDTO config;

    private CapabilityStateDTO capabilityState;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDeviceLink() {
        return device;
    }

    public void setDeviceLink(String deviceLink) {
        this.device = deviceLink;
    }

    public CapabilityConfigDTO getConfig() {
        return config;
    }

    public void setConfig(CapabilityConfigDTO config) {
        this.config = config;
    }

    /**
     * Returns the {@link CapabilityStateDTO}. Only available, if capability has a state. Better check with
     * {@link CapabilityDTO#hasState()} first!
     *
     * @return the capabilityState or null
     */
    public CapabilityStateDTO getCapabilityState() {
        return capabilityState;
    }

    /**
     * @param capabilityState the capabilityState to set
     */
    public void setCapabilityState(CapabilityStateDTO capabilityState) {
        this.capabilityState = capabilityState;
    }

    /**
     * Returns, if the capability has a state. Not all capabilities have a state.
     *
     * @return true if the capability has a state, otherwise false
     */
    public boolean hasState() {
        return (capabilityState != null) && (capabilityState.getState() != null);
    }

    /**
     * Returns the name of the {@link CapabilityDTO}.
     *
     * @return capability name
     */
    public String getName() {
        return getConfig().getName();
    }

    /**
     * Returns, if the activity log is active for the {@link CapabilityDTO}.
     *
     * @return boolean or null, if the {@link CapabilityDTO} does not have this property.
     */
    public boolean getActivityLogActive() {
        return getConfig().getActivityLogActive();
    }

    /**
     * Returns the number of pushbuttons for the {@link CapabilityDTO}.
     *
     * @return int or null, if the {@link CapabilityDTO} does not have this property.
     */
    public int getPushButtons() {
        return getConfig().getPushButtons();
    }

    /**
     * Returns true, if the {@link CapabilityDTO} is of type VariableActuator.
     *
     * @return true if it is a VariableActuator, otherwise false
     */
    public boolean isTypeVariableActuator() {
        return TYPE_VARIABLEACTUATOR.equals(getType());
    }

    /**
     * Returns true, if the {@link CapabilityDTO} is of type SwitchActuator.
     *
     * @return true if it is a SwitchActuator, otherwise false
     */
    public boolean isTypeSwitchActuator() {
        return TYPE_SWITCHACTUATOR.equals(getType());
    }

    /**
     * Returns true, if the {@link CapabilityDTO} is of type ThermostatActuator.
     *
     * @return true if it is a SwitchActuator, otherwise false
     */
    public boolean isTypeThermostatActuator() {
        return TYPE_THERMOSTATACTUATOR.equals(getType());
    }

    /**
     * Returns true, if the {@link CapabilityDTO} is of type TemperatureSensor.
     *
     * @return true if it is a TemperatureSensor, otherwise false
     */
    public boolean isTypeTemperatureSensor() {
        return TYPE_TEMPERATURESENSOR.equals(getType());
    }

    /**
     * Returns true, if the {@link CapabilityDTO} is of type HumiditySensor.
     *
     * @return true if it is a HumiditySensor, otherwise false
     */
    public boolean isTypeHumiditySensor() {
        return TYPE_HUMIDITYSENSOR.equals(getType());
    }

    /**
     * Returns true, if the {@link CapabilityDTO} is of type WindowDoorSensor.
     *
     * @return true if it is a WindowDoorSensor, otherwise false
     */
    public boolean isTypeWindowDoorSensor() {
        return TYPE_WINDOWDOORSENSOR.equals(getType());
    }

    /**
     * Returns true, if the {@link CapabilityDTO} is of type SmokeDetectorSensor.
     *
     * @return true if it is a SmokeDetector, otherwise false
     */
    public boolean isTypeSmokeDetectorSensor() {
        return TYPE_SMOKEDETECTORSENSOR.equals(getType());
    }

    /**
     * Returns true, if the {@link CapabilityDTO} is of type AlarmActuator.
     *
     * @return true if it is an AlarmActuator, otherwise false
     */
    public boolean isTypeAlarmActuator() {
        return TYPE_ALARMACTUATOR.equals(getType());
    }

    /**
     * Returns true, if the {@link CapabilityDTO} is of type MotionDetectionSensor.
     *
     * @return true if it is a MotionDetectionSensor, otherwise false
     */
    public boolean isTypeMotionDetectionSensor() {
        return TYPE_MOTIONDETECTIONSENSOR.equals(getType());
    }

    /**
     * Returns true, if the {@link CapabilityDTO} is of type LuminanceSensor.
     *
     * @return true if it is a LuminanceSensor, otherwise false
     */
    public boolean isTypeLuminanceSensor() {
        return TYPE_LUMINANCESENSOR.equals(getType());
    }

    /**
     * Returns true, if the {@link CapabilityDTO} is of type PushButtonSensor.
     *
     * @return true if it is a PushButtonSensor, otherwise false
     */
    public boolean isTypePushButtonSensor() {
        return TYPE_PUSHBUTTONSENSOR.equals(getType());
    }

    /**
     * Returns true, if the {@link CapabilityDTO} is of type DimmerActuator.
     *
     * @return true if it is a DimmerActuator, otherwise false
     */
    public boolean isTypeDimmerActuator() {
        return TYPE_DIMMERACTUATOR.equals(getType());
    }

    /**
     * Returns true, if the {@link CapabilityDTO} is of type RollerShutterActuator.
     *
     * @return true if it is a RollerShutterActuator, otherwise false
     */
    public boolean isTypeRollerShutterActuator() {
        return TYPE_ROLLERSHUTTERACTUATOR.equals(getType());
    }

    /**
     * Returns true, if the {@link CapabilityDTO} is of type EnergyConsumptionSensor.
     *
     * @return true if it is an EnergyConsumptionSensor, otherwise false
     */
    public boolean isTypeEnergyConsumptionSensor() {
        return TYPE_ENERGYCONSUMPTIONSENSOR.equals(getType());
    }

    /**
     * Returns true, if the {@link CapabilityDTO} is of type PowerConsumptionSensor.
     *
     * @return true if it is a PowerConsumptionSensor, otherwise false
     */
    public boolean isTypePowerConsumptionSensor() {
        return TYPE_POWERCONSUMPTIONSENSOR.equals(getType());
    }

    /**
     * Returns true, if the {@link CapabilityDTO} is of type GenerationMeterEnergySensor.
     *
     * @return true if it is a GenerationMeterEnergySensor, otherwise false
     */
    public boolean isTypeGenerationMeterEnergySensor() {
        return TYPE_GENERATIONMETERENERGYSENSOR.equals(getType());
    }

    /**
     * Returns true, if the {@link CapabilityDTO} is of type GenerationMeterPowerConsumptionSensor.
     *
     * @return true if it is a GenerationMeterPowerConsumptionSensor, otherwise false
     */
    public boolean isTypeGenerationMeterPowerConsumptionSensor() {
        return TYPE_GENERATIONMETERPOWERCONSUMPTIONSENSOR.equals(getType());
    }

    /**
     * Returns true, if the {@link CapabilityDTO} is of type TwoWayMeterEnergyConsumptionSensor.
     *
     * @return true if it is a TwoWayMeterEnergyConsumptionSensor, otherwise false
     */
    public boolean isTypeTwoWayMeterEnergyConsumptionSensor() {
        return TYPE_TWOWAYMETERENERGYCONSUMPTIONSENSOR.equals(getType());
    }

    /**
     * Returns true, if the {@link CapabilityDTO} is of type TwoWayMeterEnergyFeedSensor.
     *
     * @return true if it is a TwoWayMeterEnergyFeedSensor, otherwise false
     */
    public boolean isTypeTwoWayMeterEnergyFeedSensor() {
        return TYPE_TWOWAYMETERENERGYFEEDSENSOR.equals(getType());
    }

    /**
     * Returns true, if the {@link CapabilityDTO} is of type TwoWayMeterPowerConsumptionSensor.
     *
     * @return true if it is a TwoWayMeterPowerConsumptionSensor, otherwise false
     */
    public boolean isTypeTwoWayMeterPowerConsumptionSensor() {
        return TYPE_TWOWAYMETERPOWERCONSUMPTIONSENSOR.equals(getType());
    }
}
