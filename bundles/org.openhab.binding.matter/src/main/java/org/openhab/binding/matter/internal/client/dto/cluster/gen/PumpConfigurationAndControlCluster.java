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

// AUTO-GENERATED, DO NOT EDIT!

package org.openhab.binding.matter.internal.client.dto.cluster.gen;

import java.math.BigInteger;

import org.eclipse.jdt.annotation.NonNull;

/**
 * PumpConfigurationAndControl
 *
 * @author Dan Cunningham - Initial contribution
 */
public class PumpConfigurationAndControlCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0200;
    public static final String CLUSTER_NAME = "PumpConfigurationAndControl";
    public static final String CLUSTER_PREFIX = "pumpConfigurationAndControl";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_MAX_PRESSURE = "maxPressure";
    public static final String ATTRIBUTE_MAX_SPEED = "maxSpeed";
    public static final String ATTRIBUTE_MAX_FLOW = "maxFlow";
    public static final String ATTRIBUTE_MIN_CONST_PRESSURE = "minConstPressure";
    public static final String ATTRIBUTE_MAX_CONST_PRESSURE = "maxConstPressure";
    public static final String ATTRIBUTE_MIN_COMP_PRESSURE = "minCompPressure";
    public static final String ATTRIBUTE_MAX_COMP_PRESSURE = "maxCompPressure";
    public static final String ATTRIBUTE_MIN_CONST_SPEED = "minConstSpeed";
    public static final String ATTRIBUTE_MAX_CONST_SPEED = "maxConstSpeed";
    public static final String ATTRIBUTE_MIN_CONST_FLOW = "minConstFlow";
    public static final String ATTRIBUTE_MAX_CONST_FLOW = "maxConstFlow";
    public static final String ATTRIBUTE_MIN_CONST_TEMP = "minConstTemp";
    public static final String ATTRIBUTE_MAX_CONST_TEMP = "maxConstTemp";
    public static final String ATTRIBUTE_PUMP_STATUS = "pumpStatus";
    public static final String ATTRIBUTE_EFFECTIVE_OPERATION_MODE = "effectiveOperationMode";
    public static final String ATTRIBUTE_EFFECTIVE_CONTROL_MODE = "effectiveControlMode";
    public static final String ATTRIBUTE_CAPACITY = "capacity";
    public static final String ATTRIBUTE_SPEED = "speed";
    public static final String ATTRIBUTE_LIFETIME_RUNNING_HOURS = "lifetimeRunningHours";
    public static final String ATTRIBUTE_POWER = "power";
    public static final String ATTRIBUTE_LIFETIME_ENERGY_CONSUMED = "lifetimeEnergyConsumed";
    public static final String ATTRIBUTE_OPERATION_MODE = "operationMode";
    public static final String ATTRIBUTE_CONTROL_MODE = "controlMode";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * This attribute specifies the maximum pressure the pump can achieve. It is a physical limit, and does not apply to
     * any specific control mode or operation mode.
     * Valid range is -3,276.7 kPa to 3,276.7 kPa (steps of 0.1 kPa). Null if the value is invalid.
     */
    public Integer maxPressure; // 0 int16 R V
    /**
     * This attribute specifies the maximum speed the pump can achieve. It is a physical limit, and does not apply to
     * any specific control mode or operation mode.
     * Valid range is 0 to 65,534 RPM (steps of 1 RPM). Null if the value is invalid.
     */
    public Integer maxSpeed; // 1 uint16 R V
    /**
     * This attribute specifies the maximum flow the pump can achieve. It is a physical limit, and does not apply to any
     * specific control mode or operation mode.
     * Valid range is 0 m/h to 6,553.4 m/h (steps of 0.1 m/h). Null if the value is invalid.
     */
    public Integer maxFlow; // 2 uint16 R V
    /**
     * This attribute specifies the minimum pressure the pump can achieve when it is working with the ControlMode
     * attribute set to ConstantPressure.
     * Valid range is –3,276.7 kPa to 3,276.7 kPa (steps of 0.1 kPa). Null if the value is invalid.
     */
    public Integer minConstPressure; // 3 int16 R V
    /**
     * This attribute specifies the maximum pressure the pump can achieve when it is working with the ControlMode
     * attribute set to ConstantPressure.
     * Valid range is –3,276.7 kPa to 3,276.7 kPa (steps of 0.1 kPa). Null if the value is invalid.
     */
    public Integer maxConstPressure; // 4 int16 R V
    /**
     * This attribute specifies the minimum compensated pressure the pump can achieve when it is working with the
     * ControlMode attribute set to ProportionalPressure.
     * Valid range is –3,276.7 kPa to 3,276.7 kPa (steps of 0.1 kPa). Null if the value is invalid.
     */
    public Integer minCompPressure; // 5 int16 R V
    /**
     * This attribute specifies the maximum compensated pressure the pump can achieve when it is working with the
     * ControlMode attribute set to ProportionalPressure.
     * Valid range is –3,276.7 kPa to 3,276.7 kPa (steps of 0.1 kPa). Null if the value is invalid.
     */
    public Integer maxCompPressure; // 6 int16 R V
    /**
     * This attribute specifies the minimum speed the pump can achieve when it is working with the ControlMode attribute
     * set to ConstantSpeed.
     * Valid range is 0 to 65,534 RPM (steps of 1 RPM). Null if the value is invalid.
     */
    public Integer minConstSpeed; // 7 uint16 R V
    /**
     * This attribute specifies the maximum speed the pump can achieve when it is working with the ControlMode attribute
     * set to ConstantSpeed.
     * Valid range is 0 to 65,534 RPM (steps of 1 RPM). Null if the value is invalid.
     */
    public Integer maxConstSpeed; // 8 uint16 R V
    /**
     * This attribute specifies the minimum flow the pump can achieve when it is working with the ControlMode attribute
     * set to ConstantFlow.
     * Valid range is 0 m/h to 6,553.4 m/h (steps of 0.1 m/h). Null if the value is invalid.
     */
    public Integer minConstFlow; // 9 uint16 R V
    /**
     * This attribute specifies the maximum flow the pump can achieve when it is working with the ControlMode attribute
     * set to ConstantFlow.
     * Valid range is 0 m/h to 6,553.4 m/h (steps of 0.1 m/h). Null if the value is invalid.
     */
    public Integer maxConstFlow; // 10 uint16 R V
    /**
     * This attribute specifies the minimum temperature the pump can maintain in the system when it is working with the
     * ControlMode attribute set to ConstantTemperature.
     * Valid range is –273.15 °C to 327.67 °C (steps of 0.01 °C). Null if the value is invalid.
     */
    public Integer minConstTemp; // 11 int16 R V
    /**
     * This attribute specifies the maximum temperature the pump can maintain in the system when it is working with the
     * ControlMode attribute set to ConstantTemperature.
     * MaxConstTemp shall be greater than or equal to MinConstTemp Valid range is –273.15 °C to 327.67 °C (steps of 0.01
     * °C). Null if the value is invalid.
     */
    public Integer maxConstTemp; // 12 int16 R V
    /**
     * This attribute specifies the activity status of the pump functions as listed in PumpStatusBitmap. Where a pump
     * controller function is active, the corresponding bit shall be set to 1. Where a pump controller function is not
     * active, the corresponding bit shall be set to 0.
     */
    public PumpStatusBitmap pumpStatus; // 16 PumpStatusBitmap R V
    /**
     * This attribute specifies current effective operation mode of the pump as defined in OperationModeEnum.
     * The value of the EffectiveOperationMode attribute is the same as the OperationMode attribute, unless one of the
     * following points are true:
     * • The pump is physically set to run with the local settings
     * • The LocalOverride bit in the PumpStatus attribute is set,
     * See OperationMode and ControlMode attributes for a detailed description of the operation and control of the pump.
     */
    public OperationModeEnum effectiveOperationMode; // 17 OperationModeEnum R V
    /**
     * This attribute specifies the current effective control mode of the pump as defined in ControlModeEnum.
     * This attribute contains the control mode that currently applies to the pump. It will have the value of the
     * ControlMode attribute, unless one of the following points are true:
     * • The ControlMode attribute is set to Automatic. In this case, the value of the EffectiveControlMode shall match
     * the behavior of the pump.
     * • A remote sensor is used as the sensor for regulation of the pump. In this case, EffectiveControlMode will
     * display ConstantPressure, ConstantFlow or ConstantTemperature if the remote sensor is a pressure sensor, a flow
     * sensor or a temperature sensor respectively, regardless of the value of the ControlMode attribute.
     * In case the ControlMode attribute is not included on the device and no remote sensors are connected, the value of
     * the EffectiveControlMode shall match the vendor-specific behavior of the pump.
     * See OperationMode and ControlMode attributes for detailed a description of the operation and control of the pump.
     */
    public ControlModeEnum effectiveControlMode; // 18 ControlModeEnum R V
    /**
     * This attribute specifies the actual capacity of the pump as a percentage of the effective maximum setpoint value.
     * It is updated dynamically as the speed of the pump changes.
     * If the value is not available (the measurement or estimation of the speed is done in the pump), this attribute
     * will indicate the null value.
     * Valid range is 0 % to 163.835% (0.005 % granularity). Although this attribute is a signed value, values of
     * capacity less than zero have no physical meaning.
     */
    public Integer capacity; // 19 int16 R V
    /**
     * This attribute specifies the actual speed of the pump measured in RPM. It is updated dynamically as the speed of
     * the pump changes.
     * If the value is not available (the measurement or estimation of the speed is done in the pump), this attribute
     * will indicate the null value.
     * Valid range is 0 to 65,534 RPM.
     */
    public Integer speed; // 20 uint16 R V
    /**
     * This attribute specifies the accumulated number of hours that the pump has been powered and the motor has been
     * running. It is updated dynamically as it increases. It is preserved over power cycles of the pump. If
     * LifeTimeRunningHours rises above maximum value it “rolls over” and starts at 0 (zero).
     * This attribute is writeable, in order to allow setting to an appropriate value after maintenance. If the value is
     * not available, this attribute will indicate the null value.
     * Valid range is 0 to 16,777,214 hrs.
     */
    public Integer lifetimeRunningHours; // 21 uint24 RW VM
    /**
     * This attribute specifies the actual power consumption of the pump in Watts. The value of this attribute is
     * updated dynamically as the power consumption of the pump changes.
     * This attribute is read only. If the value is not available (the measurement of power consumption is not done in
     * the pump), this attribute will indicate the null value.
     * Valid range is 0 to 16,777,214 Watts.
     */
    public Integer power; // 22 uint24 R V
    /**
     * This attribute specifies the accumulated energy consumption of the pump through the entire lifetime of the pump
     * in kWh. The value of the LifetimeEnergyConsumed attribute is updated dynamically as the energy consumption of the
     * pump increases. If LifetimeEnergyConsumed rises above maximum value it “rolls over” and starts at 0 (zero).
     * This attribute is writeable, in order to allow setting to an appropriate value after maintenance.
     * Valid range is 0 kWh to 4,294,967,294 kWh.
     * Null if the value is unknown.
     */
    public Integer lifetimeEnergyConsumed; // 23 uint32 RW VM
    /**
     * This attribute specifies the operation mode of the pump as defined in OperationModeEnum.
     * The actual operating mode of the pump is a result of the setting of the attributes OperationMode, ControlMode and
     * the optional connection of a remote sensor. The operation and control is prioritized as shown in the scheme
     * below:
     * ### Priority Scheme of Pump Operation and Control
     * If this attribute is Maximum, Minimum or Local, the OperationMode attribute decides how the pump is operated.
     * If this attribute is Normal and a remote sensor is connected to the pump, the type of the remote sensor decides
     * the control mode of the pump. A connected remote pressure sensor will make the pump run in control mode Constant
     * pressure and vice versa for flow and temperature type sensors. This is regardless of the setting of the
     * ControlMode attribute.
     * If this attribute is Normal and no remote sensor is connected, the control mode of the pump is decided by the
     * ControlMode attribute.
     * OperationMode may be changed at any time, even when the pump is running. The behavior of the pump at the point of
     * changing the value of this attribute is vendor-specific.
     * In the case a device does not support a specific operation mode, the write interaction to this attribute with an
     * unsupported operation mode value shall be ignored and a response containing the status of CONSTRAINT_ERROR shall
     * be returned.
     */
    public OperationModeEnum operationMode; // 32 OperationModeEnum RW VM
    /**
     * This attribute specifies the control mode of the pump as defined in ControlModeEnum.
     * See the OperationMode attribute for a detailed description of the operation and control of the pump.
     * ControlMode may be changed at any time, even when the pump is running. The behavior of the pump at the point of
     * changing is vendor-specific.
     * In the case a device does not support a specific control mode, the write interaction to this attribute with an
     * unsupported control mode value shall be ignored and a response containing the status of CONSTRAINT_ERROR shall be
     * returned.
     */
    public ControlModeEnum controlMode; // 33 ControlModeEnum RW VM

    // Structs
    public static class SupplyVoltageLow {
        public SupplyVoltageLow() {
        }
    }

    public static class SupplyVoltageHigh {
        public SupplyVoltageHigh() {
        }
    }

    public static class PowerMissingPhase {
        public PowerMissingPhase() {
        }
    }

    public static class SystemPressureLow {
        public SystemPressureLow() {
        }
    }

    public static class SystemPressureHigh {
        public SystemPressureHigh() {
        }
    }

    public static class DryRunning {
        public DryRunning() {
        }
    }

    public static class MotorTemperatureHigh {
        public MotorTemperatureHigh() {
        }
    }

    public static class PumpMotorFatalFailure {
        public PumpMotorFatalFailure() {
        }
    }

    public static class ElectronicTemperatureHigh {
        public ElectronicTemperatureHigh() {
        }
    }

    public static class PumpBlocked {
        public PumpBlocked() {
        }
    }

    public static class SensorFailure {
        public SensorFailure() {
        }
    }

    public static class ElectronicNonFatalFailure {
        public ElectronicNonFatalFailure() {
        }
    }

    public static class ElectronicFatalFailure {
        public ElectronicFatalFailure() {
        }
    }

    public static class GeneralFault {
        public GeneralFault() {
        }
    }

    public static class Leakage {
        public Leakage() {
        }
    }

    public static class AirDetection {
        public AirDetection() {
        }
    }

    public static class TurbineOperation {
        public TurbineOperation() {
        }
    }

    // Enums
    public enum OperationModeEnum implements MatterEnum {
        NORMAL(0, "Normal"),
        MINIMUM(1, "Minimum"),
        MAXIMUM(2, "Maximum"),
        LOCAL(3, "Local");

        public final Integer value;
        public final String label;

        private OperationModeEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum ControlModeEnum implements MatterEnum {
        CONSTANT_SPEED(0, "Constant Speed"),
        CONSTANT_PRESSURE(1, "Constant Pressure"),
        PROPORTIONAL_PRESSURE(2, "Proportional Pressure"),
        CONSTANT_FLOW(3, "Constant Flow"),
        CONSTANT_TEMPERATURE(5, "Constant Temperature"),
        AUTOMATIC(7, "Automatic");

        public final Integer value;
        public final String label;

        private ControlModeEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    // Bitmaps
    public static class PumpStatusBitmap {
        /**
         * A fault related to the system or pump device is detected.
         * If this bit is set, it may correspond to an event in the range 2-16, see Events.
         */
        public boolean deviceFault;
        /**
         * A fault related to the supply to the pump is detected.
         * If this bit is set, it may correspond to an event in the range 0-1 or 13, see Events.
         */
        public boolean supplyFault;
        public boolean speedLow;
        public boolean speedHigh;
        /**
         * Device control is overridden by hardware, such as an external STOP button or via a local HMI.
         * While this bit is set, the EffectiveOperationMode is adjusted to Local. Any request changing OperationMode
         * shall generate a FAILURE error status until LocalOverride is cleared on the physical device. When
         * LocalOverride is cleared, the device shall return to the operation mode set in OperationMode.
         */
        public boolean localOverride;
        public boolean running;
        /**
         * A remote pressure sensor is used as the sensor for the regulation of the pump.
         * If this bit is set, EffectiveControlMode is ConstantPressure and the setpoint for the pump is interpreted as
         * a percentage of the range of the remote sensor ([MinMeasuredValue – MaxMeasuredValue]).
         */
        public boolean remotePressure;
        /**
         * A remote flow sensor is used as the sensor for the regulation of the pump.
         * If this bit is set, EffectiveControlMode is ConstantFlow, and the setpoint for the pump is interpreted as a
         * percentage of the range of the remote sensor ([MinMeasuredValue – MaxMeasuredValue]).
         */
        public boolean remoteFlow;
        /**
         * A remote temperature sensor is used as the sensor for the regulation of the pump.
         * If this bit is set, EffectiveControlMode is ConstantTemperature, and the setpoint for the pump is interpreted
         * as a percentage of the range of the remote sensor ([MinMeasuredValue – MaxMeasuredValue])
         */
        public boolean remoteTemperature;

        public PumpStatusBitmap(boolean deviceFault, boolean supplyFault, boolean speedLow, boolean speedHigh,
                boolean localOverride, boolean running, boolean remotePressure, boolean remoteFlow,
                boolean remoteTemperature) {
            this.deviceFault = deviceFault;
            this.supplyFault = supplyFault;
            this.speedLow = speedLow;
            this.speedHigh = speedHigh;
            this.localOverride = localOverride;
            this.running = running;
            this.remotePressure = remotePressure;
            this.remoteFlow = remoteFlow;
            this.remoteTemperature = remoteTemperature;
        }
    }

    public static class FeatureMap {
        /**
         * 
         * Supports operating in constant pressure mode
         */
        public boolean constantPressure;
        /**
         * 
         * Supports operating in compensated pressure mode
         */
        public boolean compensatedPressure;
        /**
         * 
         * Supports operating in constant flow mode
         */
        public boolean constantFlow;
        /**
         * 
         * Supports operating in constant speed mode
         */
        public boolean constantSpeed;
        /**
         * 
         * Supports operating in constant temperature mode
         */
        public boolean constantTemperature;
        /**
         * 
         * Supports operating in automatic mode
         */
        public boolean automatic;
        /**
         * 
         * Supports operating using local settings
         */
        public boolean localOperation;

        public FeatureMap(boolean constantPressure, boolean compensatedPressure, boolean constantFlow,
                boolean constantSpeed, boolean constantTemperature, boolean automatic, boolean localOperation) {
            this.constantPressure = constantPressure;
            this.compensatedPressure = compensatedPressure;
            this.constantFlow = constantFlow;
            this.constantSpeed = constantSpeed;
            this.constantTemperature = constantTemperature;
            this.automatic = automatic;
            this.localOperation = localOperation;
        }
    }

    public PumpConfigurationAndControlCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 512, "PumpConfigurationAndControl");
    }

    protected PumpConfigurationAndControlCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "maxPressure : " + maxPressure + "\n";
        str += "maxSpeed : " + maxSpeed + "\n";
        str += "maxFlow : " + maxFlow + "\n";
        str += "minConstPressure : " + minConstPressure + "\n";
        str += "maxConstPressure : " + maxConstPressure + "\n";
        str += "minCompPressure : " + minCompPressure + "\n";
        str += "maxCompPressure : " + maxCompPressure + "\n";
        str += "minConstSpeed : " + minConstSpeed + "\n";
        str += "maxConstSpeed : " + maxConstSpeed + "\n";
        str += "minConstFlow : " + minConstFlow + "\n";
        str += "maxConstFlow : " + maxConstFlow + "\n";
        str += "minConstTemp : " + minConstTemp + "\n";
        str += "maxConstTemp : " + maxConstTemp + "\n";
        str += "pumpStatus : " + pumpStatus + "\n";
        str += "effectiveOperationMode : " + effectiveOperationMode + "\n";
        str += "effectiveControlMode : " + effectiveControlMode + "\n";
        str += "capacity : " + capacity + "\n";
        str += "speed : " + speed + "\n";
        str += "lifetimeRunningHours : " + lifetimeRunningHours + "\n";
        str += "power : " + power + "\n";
        str += "lifetimeEnergyConsumed : " + lifetimeEnergyConsumed + "\n";
        str += "operationMode : " + operationMode + "\n";
        str += "controlMode : " + controlMode + "\n";
        return str;
    }
}
