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
package org.openhab.binding.innogysmarthome.internal.client.entity.capability;

/**
 * Defines the structure of a {@link Capability}. A capability is a specific functionality of a device like a
 * temperature sensor.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class Capability {

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
    private CapabilityConfig config;

    private CapabilityState capabilityState;

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
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return
     */
    public String getDeviceLink() {
        return device;
    }

    /**
     * @param deviceLink
     */
    public void setDeviceLink(String deviceLink) {
        this.device = deviceLink;
    }

    /**
     * @return the config
     */
    public CapabilityConfig getConfig() {
        return config;
    }

    /**
     * @param config the config to set
     */
    public void setConfig(CapabilityConfig config) {
        this.config = config;
    }

    /**
     * Returns the {@link CapabilityState}. Only available, if capability has a state. Better check with
     * {@link Capability#hasState()} first!
     *
     * @return the capabilityState or null
     */
    public CapabilityState getCapabilityState() {
        return capabilityState;
    }

    /**
     * @param capabilityState the capabilityState to set
     */
    public void setCapabilityState(CapabilityState capabilityState) {
        this.capabilityState = capabilityState;
    }

    /**
     * Returns, if the capability has a state. Not all capabilities have a state.
     *
     * @return
     */
    public boolean hasState() {
        return (capabilityState != null) && (capabilityState.getState() != null);
    }

    /**
     * Returns the name of the {@link Capability}.
     *
     * @return
     */
    public String getName() {
        return getConfig().getName();
    }

    /**
     * Returns, if the activity log is active for the {@link Capability}.
     *
     * @return boolean or null, if the {@link Capability} does not have this property.
     */
    public boolean getActivityLogActive() {
        return getConfig().getActivityLogActive();
    }

    /**
     * Returns the number of pushbuttons for the {@link Capability}.
     *
     * @return int or null, if the {@link Capability} does not have this property.
     */
    public int getPushButtons() {
        return getConfig().getPushButtons();
    }

    /**
     * Returns true, if the {@link Capability} is of type VariableActuator.
     *
     * @return
     */
    public boolean isTypeVariableActuator() {
        return TYPE_VARIABLEACTUATOR.equals(getType());
    }

    /**
     * Returns true, if the {@link Capability} is of type SwitchActuator.
     *
     * @return
     */
    public boolean isTypeSwitchActuator() {
        return TYPE_SWITCHACTUATOR.equals(getType());
    }

    /**
     * Returns true, if the {@link Capability} is of type ThermostatActuator.
     *
     * @return
     */
    public boolean isTypeThermostatActuator() {
        return TYPE_THERMOSTATACTUATOR.equals(getType());
    }

    /**
     * Returns true, if the {@link Capability} is of type TemperatureSensor.
     *
     * @return
     */
    public boolean isTypeTemperatureSensor() {
        return TYPE_TEMPERATURESENSOR.equals(getType());
    }

    /**
     * Returns true, if the {@link Capability} is of type HumiditySensor.
     *
     * @return
     */
    public boolean isTypeHumiditySensor() {
        return TYPE_HUMIDITYSENSOR.equals(getType());
    }

    /**
     * Returns true, if the {@link Capability} is of type WindowDoorSensor.
     *
     * @return
     */
    public boolean isTypeWindowDoorSensor() {
        return TYPE_WINDOWDOORSENSOR.equals(getType());
    }

    /**
     * Returns true, if the {@link Capability} is of type SmokeDetectorSensor.
     *
     * @return
     */
    public boolean isTypeSmokeDetectorSensor() {
        return TYPE_SMOKEDETECTORSENSOR.equals(getType());
    }

    /**
     * Returns true, if the {@link Capability} is of type AlarmActuator.
     *
     * @return
     */
    public boolean isTypeAlarmActuator() {
        return TYPE_ALARMACTUATOR.equals(getType());
    }

    /**
     * Returns true, if the {@link Capability} is of type MotionDetectionSensor.
     *
     * @return
     */
    public boolean isTypeMotionDetectionSensor() {
        return TYPE_MOTIONDETECTIONSENSOR.equals(getType());
    }

    /**
     * Returns true, if the {@link Capability} is of type LuminanceSensor.
     *
     * @return
     */
    public boolean isTypeLuminanceSensor() {
        return TYPE_LUMINANCESENSOR.equals(getType());
    }

    /**
     * Returns true, if the {@link Capability} is of type PushButtonSensor.
     *
     * @return
     */
    public boolean isTypePushButtonSensor() {
        return TYPE_PUSHBUTTONSENSOR.equals(getType());
    }

    /**
     * Returns true, if the {@link Capability} is of type DimmerActuator.
     *
     * @return
     */
    public boolean isTypeDimmerActuator() {
        return TYPE_DIMMERACTUATOR.equals(getType());
    }

    /**
     * Returns true, if the {@link Capability} is of type RollerShutterActuator.
     *
     * @return
     */
    public boolean isTypeRollerShutterActuator() {
        return TYPE_ROLLERSHUTTERACTUATOR.equals(getType());
    }

    /**
     * Returns true, if the {@link Capability} is of type EnergyConsumptionSensor.
     *
     * @return
     */
    public boolean isTypeEnergyConsumptionSensor() {
        return TYPE_ENERGYCONSUMPTIONSENSOR.equals(getType());
    }

    /**
     * Returns true, if the {@link Capability} is of type PowerConsumptionSensor.
     *
     * @return
     */
    public boolean isTypePowerConsumptionSensor() {
        return TYPE_POWERCONSUMPTIONSENSOR.equals(getType());
    }

    /**
     * Returns true, if the {@link Capability} is of type GenerationMeterEnergySensor.
     *
     * @return
     */
    public boolean isTypeGenerationMeterEnergySensor() {
        return TYPE_GENERATIONMETERENERGYSENSOR.equals(getType());
    }

    /**
     * Returns true, if the {@link Capability} is of type GenerationMeterPowerConsumptionSensor.
     *
     * @return
     */
    public boolean isTypeGenerationMeterPowerConsumptionSensor() {
        return TYPE_GENERATIONMETERPOWERCONSUMPTIONSENSOR.equals(getType());
    }

    /**
     * Returns true, if the {@link Capability} is of type TwoWayMeterEnergyConsumptionSensor.
     *
     * @return
     */
    public boolean isTypeTwoWayMeterEnergyConsumptionSensor() {
        return TYPE_TWOWAYMETERENERGYCONSUMPTIONSENSOR.equals(getType());
    }

    /**
     * Returns true, if the {@link Capability} is of type TwoWayMeterEnergyFeedSensor.
     *
     * @return
     */
    public boolean isTypeTwoWayMeterEnergyFeedSensor() {
        return TYPE_TWOWAYMETERENERGYFEEDSENSOR.equals(getType());
    }

    /**
     * Returns true, if the {@link Capability} is of type TwoWayMeterPowerConsumptionSensor.
     *
     * @return
     */
    public boolean isTypeTwoWayMeterPowerConsumptionSensor() {
        return TYPE_TWOWAYMETERPOWERCONSUMPTIONSENSOR.equals(getType());
    }
}
