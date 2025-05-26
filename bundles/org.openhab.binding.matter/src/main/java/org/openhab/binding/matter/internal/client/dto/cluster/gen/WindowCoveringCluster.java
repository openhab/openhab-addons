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
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;

/**
 * WindowCovering
 *
 * @author Dan Cunningham - Initial contribution
 */
public class WindowCoveringCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0102;
    public static final String CLUSTER_NAME = "WindowCovering";
    public static final String CLUSTER_PREFIX = "windowCovering";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_TYPE = "type";
    public static final String ATTRIBUTE_PHYSICAL_CLOSED_LIMIT_LIFT = "physicalClosedLimitLift";
    public static final String ATTRIBUTE_PHYSICAL_CLOSED_LIMIT_TILT = "physicalClosedLimitTilt";
    public static final String ATTRIBUTE_CURRENT_POSITION_LIFT = "currentPositionLift";
    public static final String ATTRIBUTE_CURRENT_POSITION_TILT = "currentPositionTilt";
    public static final String ATTRIBUTE_NUMBER_OF_ACTUATIONS_LIFT = "numberOfActuationsLift";
    public static final String ATTRIBUTE_NUMBER_OF_ACTUATIONS_TILT = "numberOfActuationsTilt";
    public static final String ATTRIBUTE_CONFIG_STATUS = "configStatus";
    public static final String ATTRIBUTE_CURRENT_POSITION_LIFT_PERCENTAGE = "currentPositionLiftPercentage";
    public static final String ATTRIBUTE_CURRENT_POSITION_TILT_PERCENTAGE = "currentPositionTiltPercentage";
    public static final String ATTRIBUTE_OPERATIONAL_STATUS = "operationalStatus";
    public static final String ATTRIBUTE_TARGET_POSITION_LIFT_PERCENT100THS = "targetPositionLiftPercent100ths";
    public static final String ATTRIBUTE_TARGET_POSITION_TILT_PERCENT100THS = "targetPositionTiltPercent100ths";
    public static final String ATTRIBUTE_END_PRODUCT_TYPE = "endProductType";
    public static final String ATTRIBUTE_CURRENT_POSITION_LIFT_PERCENT100THS = "currentPositionLiftPercent100ths";
    public static final String ATTRIBUTE_CURRENT_POSITION_TILT_PERCENT100THS = "currentPositionTiltPercent100ths";
    public static final String ATTRIBUTE_INSTALLED_OPEN_LIMIT_LIFT = "installedOpenLimitLift";
    public static final String ATTRIBUTE_INSTALLED_CLOSED_LIMIT_LIFT = "installedClosedLimitLift";
    public static final String ATTRIBUTE_INSTALLED_OPEN_LIMIT_TILT = "installedOpenLimitTilt";
    public static final String ATTRIBUTE_INSTALLED_CLOSED_LIMIT_TILT = "installedClosedLimitTilt";
    public static final String ATTRIBUTE_MODE = "mode";
    public static final String ATTRIBUTE_SAFETY_STATUS = "safetyStatus";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * This attribute shall identify the type of window covering.
     */
    public TypeEnum type; // 0 TypeEnum R V
    /**
     * Indicates the maximum possible encoder position possible (Unit cm, centimeters) to position the height of the
     * window covering lift.
     */
    public Integer physicalClosedLimitLift; // 1 uint16 R V
    /**
     * Indicates the maximum possible encoder position possible (Unit 0.1°, tenths of a degree) to position the angle of
     * the window covering tilt.
     */
    public Integer physicalClosedLimitTilt; // 2 uint16 R V
    /**
     * Indicates the actual lift position (Unit cm, centimeters) of the window covering from the fully-open position.
     */
    public Integer currentPositionLift; // 3 uint16 R V
    /**
     * Indicates the actual tilt position (Unit 0.1°, tenths of a degree) of the window covering from the fully-open
     * position.
     */
    public Integer currentPositionTilt; // 4 uint16 R V
    /**
     * Indicates the total number of lift/slide actuations applied to the window covering since the device was
     * installed.
     */
    public Integer numberOfActuationsLift; // 5 uint16 R V
    /**
     * Indicates the total number of tilt actuations applied to the window covering since the device was installed.
     */
    public Integer numberOfActuationsTilt; // 6 uint16 R V
    /**
     * This attribute specifies the configuration and status information of the window covering.
     * To change settings, devices shall write to the Mode attribute. The behavior causing the setting or clearing of
     * each bit is vendor specific.
     */
    public ConfigStatusBitmap configStatus; // 7 ConfigStatusBitmap R V
    /**
     * Indicates the actual position as a percentage from 0% to 100% with 1% default step. This attribute is equal to
     * CurrentPositionLiftPercent100ths attribute divided by 100.
     */
    public Integer currentPositionLiftPercentage; // 8 percent R V
    /**
     * Indicates the actual position as a percentage from 0% to 100% with 1% default step. This attribute is equal to
     * CurrentPositionTiltPercent100ths attribute divided by 100.
     */
    public Integer currentPositionTiltPercentage; // 9 percent R V
    /**
     * Indicates the currently ongoing operations and applies to all type of devices.
     */
    public OperationalStatusBitmap operationalStatus; // 10 OperationalStatusBitmap R V
    /**
     * Indicates the position where the window covering lift will go or is moving to as a percentage (Unit 0.01%).
     */
    public Integer targetPositionLiftPercent100ths; // 11 percent100ths R V
    /**
     * Indicates the position where the window covering tilt will go or is moving to as a percentage (Unit 0.01%).
     */
    public Integer targetPositionTiltPercent100ths; // 12 percent100ths R V
    /**
     * This attribute SHOULD provide more detail about the product type than can be determined from the main category
     * indicated by the Type attribute.
     * The table below helps to match the EndProductType attribute with the Type attribute.
     */
    public EndProductTypeEnum endProductType; // 13 EndProductTypeEnum R V
    /**
     * Indicates the actual position as a percentage with a minimal step of 0.01%. E.g Max 10000 equals 100.00%.
     */
    public Integer currentPositionLiftPercent100ths; // 14 percent100ths R V
    /**
     * Indicates the actual position as a percentage with a minimal step of 0.01%. E.g Max 10000 equals 100.00%.
     */
    public Integer currentPositionTiltPercent100ths; // 15 percent100ths R V
    /**
     * Indicates the open limit for lifting the window covering whether position (in centimeters) is encoded or timed.
     */
    public Integer installedOpenLimitLift; // 16 uint16 R V
    /**
     * Indicates the closed limit for lifting the window covering whether position (in centimeters) is encoded or timed.
     */
    public Integer installedClosedLimitLift; // 17 uint16 R V
    /**
     * Indicates the open limit for tilting the window covering whether position (in tenth of a degree) is encoded or
     * timed.
     */
    public Integer installedOpenLimitTilt; // 18 uint16 R V
    /**
     * Indicates the closed limit for tilting the window covering whether position (in tenth of a degree) is encoded or
     * timed.
     */
    public Integer installedClosedLimitTilt; // 19 uint16 R V
    /**
     * The Mode attribute allows configuration of the window covering, such as: reversing the motor direction, placing
     * the window covering into calibration mode, placing the motor into maintenance mode, disabling the network, and
     * disabling status LEDs.
     * In the case a device does not support or implement a specific mode, e.g. the device has a specific installation
     * method and reversal is not relevant or the device does not include a maintenance mode, any write interaction to
     * the Mode attribute, with an unsupported mode bit or any out of bounds bits set, must be ignored and a response
     * containing the status of CONSTRAINT_ERROR will be returned.
     */
    public ModeBitmap mode; // 23 ModeBitmap RW VM
    /**
     * The SafetyStatus attribute reflects the state of the safety sensors and the common issues preventing movements.
     * By default for nominal operation all flags are cleared (0). A device might support none, one or several bit flags
     * from this attribute (all optional).
     */
    public SafetyStatusBitmap safetyStatus; // 26 SafetyStatusBitmap R V

    // Enums
    public enum TypeEnum implements MatterEnum {
        ROLLERSHADE(0, "Rollershade"),
        ROLLERSHADE2MOTOR(1, "Rollershade 2 Motor"),
        ROLLERSHADE_EXTERIOR(2, "Rollershade Exterior"),
        ROLLERSHADE_EXTERIOR2MOTOR(3, "Rollershade Exterior 2 Motor"),
        DRAPERY(4, "Drapery"),
        AWNING(5, "Awning"),
        SHUTTER(6, "Shutter"),
        TILT_BLIND_TILT_ONLY(7, "Tilt Blind Tilt Only"),
        TILT_BLIND_LIFT(8, "Tilt Blind Lift"),
        PROJECTOR_SCREEN(9, "Projector Screen"),
        UNKNOWN(255, "Unknown");

        public final Integer value;
        public final String label;

        private TypeEnum(Integer value, String label) {
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

    public enum EndProductTypeEnum implements MatterEnum {
        ROLLER_SHADE(0, "Roller Shade"),
        ROMAN_SHADE(1, "Roman Shade"),
        BALLOON_SHADE(2, "Balloon Shade"),
        WOVEN_WOOD(3, "Woven Wood"),
        PLEATED_SHADE(4, "Pleated Shade"),
        CELLULAR_SHADE(5, "Cellular Shade"),
        LAYERED_SHADE(6, "Layered Shade"),
        LAYERED_SHADE2D(7, "Layered Shade 2 D"),
        SHEER_SHADE(8, "Sheer Shade"),
        TILT_ONLY_INTERIOR_BLIND(9, "Tilt Only Interior Blind"),
        INTERIOR_BLIND(10, "Interior Blind"),
        VERTICAL_BLIND_STRIP_CURTAIN(11, "Vertical Blind Strip Curtain"),
        INTERIOR_VENETIAN_BLIND(12, "Interior Venetian Blind"),
        EXTERIOR_VENETIAN_BLIND(13, "Exterior Venetian Blind"),
        LATERAL_LEFT_CURTAIN(14, "Lateral Left Curtain"),
        LATERAL_RIGHT_CURTAIN(15, "Lateral Right Curtain"),
        CENTRAL_CURTAIN(16, "Central Curtain"),
        ROLLER_SHUTTER(17, "Roller Shutter"),
        EXTERIOR_VERTICAL_SCREEN(18, "Exterior Vertical Screen"),
        AWNING_TERRACE_PATIO(19, "Awning Terrace Patio"),
        AWNING_VERTICAL_SCREEN(20, "Awning Vertical Screen"),
        TILT_ONLY_PERGOLA(21, "Tilt Only Pergola"),
        SWINGING_SHUTTER(22, "Swinging Shutter"),
        SLIDING_SHUTTER(23, "Sliding Shutter"),
        UNKNOWN(255, "Unknown");

        public final Integer value;
        public final String label;

        private EndProductTypeEnum(Integer value, String label) {
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

    /**
     * Values for OperationalStatus attribute fields.
     */
    public enum MovementStatus implements MatterEnum {
        STOPPED(0, "Stopped"),
        OPENING(1, "Opening"),
        CLOSING(2, "Closing");

        public final Integer value;
        public final String label;

        private MovementStatus(Integer value, String label) {
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
    public static class ConfigStatusBitmap {
        /**
         * Device is operational.
         * This bit shall indicate whether the window covering is operational for regular use:
         * • 0 &#x3D; Not Operational
         * • 1 &#x3D; Operational
         */
        public boolean operational;
        public boolean onlineReserved;
        /**
         * The lift movement is reversed.
         * This bit shall indicate whether the lift movement is reversed:
         * • 0 &#x3D; Lift movement is normal
         * • 1 &#x3D; Lift movement is reversed
         */
        public boolean liftMovementReversed;
        /**
         * Supports the PositionAwareLift feature (PA_LF).
         * This bit shall indicate whether the window covering supports the PositionAwareLift feature:
         * • 0 &#x3D; Lift control is not position aware
         * • 1 &#x3D; Lift control is position aware (PA_LF)
         */
        public boolean liftPositionAware;
        /**
         * Supports the PositionAwareTilt feature (PA_TL).
         * This bit shall indicate whether the window covering supports the PositionAwareTilt feature:
         * • 0 &#x3D; Tilt control is not position aware
         * • 1 &#x3D; Tilt control is position aware (PA_TL)
         */
        public boolean tiltPositionAware;
        /**
         * Uses an encoder for lift.
         * This bit shall indicate whether a position aware controlled window covering is employing an encoder for
         * positioning the height of the window covering:
         * • 0 &#x3D; Timer Controlled
         * • 1 &#x3D; Encoder Controlled
         */
        public boolean liftEncoderControlled;
        /**
         * Uses an encoder for tilt.
         * This bit shall indicate whether a position aware controlled window covering is employing an encoder for
         * tilting the window covering:
         * • 0 &#x3D; Timer Controlled
         * • 1 &#x3D; Encoder Controlled
         */
        public boolean tiltEncoderControlled;

        public ConfigStatusBitmap(boolean operational, boolean onlineReserved, boolean liftMovementReversed,
                boolean liftPositionAware, boolean tiltPositionAware, boolean liftEncoderControlled,
                boolean tiltEncoderControlled) {
            this.operational = operational;
            this.onlineReserved = onlineReserved;
            this.liftMovementReversed = liftMovementReversed;
            this.liftPositionAware = liftPositionAware;
            this.tiltPositionAware = tiltPositionAware;
            this.liftEncoderControlled = liftEncoderControlled;
            this.tiltEncoderControlled = tiltEncoderControlled;
        }
    }

    public static class ModeBitmap {
        /**
         * Reverse the lift direction.
         * This bit shall control the motor direction:
         * • 0 &#x3D; Lift movement is normal
         * • 1 &#x3D; Lift movement is reversed
         */
        public boolean motorDirectionReversed;
        /**
         * Perform a calibration.
         * This bit shall set the window covering into calibration mode:
         * • 0 &#x3D; Normal mode
         * • 1 &#x3D; Calibration mode
         */
        public boolean calibrationMode;
        /**
         * Freeze all motions for maintenance.
         * This bit shall set the window covering into maintenance mode:
         * • 0 &#x3D; Normal mode
         * • 1 &#x3D; Maintenance mode
         */
        public boolean maintenanceMode;
        /**
         * Control the LEDs feedback.
         * This bit shall control feedback LEDs:
         * • 0 &#x3D; LEDs are off
         * • 1 &#x3D; LEDs will display feedback
         */
        public boolean ledFeedback;

        public ModeBitmap(boolean motorDirectionReversed, boolean calibrationMode, boolean maintenanceMode,
                boolean ledFeedback) {
            this.motorDirectionReversed = motorDirectionReversed;
            this.calibrationMode = calibrationMode;
            this.maintenanceMode = maintenanceMode;
            this.ledFeedback = ledFeedback;
        }
    }

    /**
     * The OperationalStatusBitmap is using several internal operational state fields (composed of 2 bits) following
     * this definition:
     * • 00b &#x3D; Currently not moving
     * • 01b &#x3D; Currently opening (e.g. moving from closed to open).
     * • 10b &#x3D; Currently closing (e.g. moving from open to closed).
     * • 11b &#x3D; Reserved
     */
    public static class OperationalStatusBitmap {
        /**
         * Global operational state.
         * These bits shall indicate in which direction the covering is currently moving or if it has stopped. Global
         * operational state shall always reflect the overall motion of the device.
         */
        public short global;
        /**
         * Lift operational state.
         * These bits shall indicate in which direction the covering’s lift is currently moving or if it has stopped.
         */
        public short lift;
        /**
         * Tilt operational state.
         * These bits shall indicate in which direction the covering’s tilt is currently moving or if it has stopped.
         */
        public short tilt;

        public OperationalStatusBitmap(short global, short lift, short tilt) {
            this.global = global;
            this.lift = lift;
            this.tilt = tilt;
        }
    }

    public static class SafetyStatusBitmap {
        public boolean remoteLockout;
        public boolean tamperDetection;
        public boolean failedCommunication;
        public boolean positionFailure;
        public boolean thermalProtection;
        public boolean obstacleDetected;
        public boolean power;
        public boolean stopInput;
        public boolean motorJammed;
        public boolean hardwareFailure;
        public boolean manualOperation;
        public boolean protection;

        public SafetyStatusBitmap(boolean remoteLockout, boolean tamperDetection, boolean failedCommunication,
                boolean positionFailure, boolean thermalProtection, boolean obstacleDetected, boolean power,
                boolean stopInput, boolean motorJammed, boolean hardwareFailure, boolean manualOperation,
                boolean protection) {
            this.remoteLockout = remoteLockout;
            this.tamperDetection = tamperDetection;
            this.failedCommunication = failedCommunication;
            this.positionFailure = positionFailure;
            this.thermalProtection = thermalProtection;
            this.obstacleDetected = obstacleDetected;
            this.power = power;
            this.stopInput = stopInput;
            this.motorJammed = motorJammed;
            this.hardwareFailure = hardwareFailure;
            this.manualOperation = manualOperation;
            this.protection = protection;
        }
    }

    public static class FeatureMap {
        /**
         * 
         * The Lift feature applies to window coverings that lift up and down (e.g. for a roller shade, Up and Down is
         * lift Open and Close) or slide left to right (e.g. for a sliding curtain, Left and Right is lift Open and
         * Close).
         */
        public boolean lift;
        /**
         * 
         * The Tilt feature applies to window coverings with vertical or horizontal strips.
         */
        public boolean tilt;
        /**
         * 
         * Position aware lift control is supported.
         */
        public boolean positionAwareLift;
        /**
         * 
         * The percentage attributes shall indicate the position as a percentage between the InstalledOpenLimits and
         * InstalledClosedLimits attributes of the window covering starting at the open (0.00%).
         * As a general rule, absolute positioning (in centimeters or tenth of a degrees) SHOULD NOT be supported for
         * new implementations.
         */
        public boolean absolutePosition;
        /**
         * 
         * Position aware tilt control is supported.
         */
        public boolean positionAwareTilt;

        public FeatureMap(boolean lift, boolean tilt, boolean positionAwareLift, boolean absolutePosition,
                boolean positionAwareTilt) {
            this.lift = lift;
            this.tilt = tilt;
            this.positionAwareLift = positionAwareLift;
            this.absolutePosition = absolutePosition;
            this.positionAwareTilt = positionAwareTilt;
        }
    }

    public WindowCoveringCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 258, "WindowCovering");
    }

    protected WindowCoveringCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * Upon receipt of this command, the window covering will adjust its position so the physical lift/slide and tilt is
     * at the maximum open/up position. This will happen as fast as possible. The server attributes shall be updated as
     * follows:
     * if the PositionAware feature is supported:
     * • TargetPositionLiftPercent100ths attribute shall be set to 0.00%.
     * • TargetPositionTiltPercent100ths attribute shall be set to 0.00%.
     * The server positioning attributes will follow the movements, once the movement has successfully finished, the
     * server attributes shall be updated as follows:
     * if the PositionAware feature is supported:
     * • CurrentPositionLiftPercent100ths attribute shall be 0.00%.
     * • CurrentPositionLiftPercentage attribute shall be 0%.
     * • CurrentPositionTiltPercent100ths attribute shall be 0.00%.
     * • CurrentPositionTiltPercentage attribute shall be 0%.
     * if the AbsolutePosition feature is supported:
     * • CurrentPositionLift attribute shall be equal to the InstalledOpenLimitLift attribute.
     * • CurrentPositionTilt attribute shall be equal to the InstalledOpenLimitTilt attribute.
     */
    public static ClusterCommand upOrOpen() {
        return new ClusterCommand("upOrOpen");
    }

    /**
     * Upon receipt of this command, the window covering will adjust its position so the physical lift/slide and tilt is
     * at the maximum closed/down position. This will happen as fast as possible. The server attributes supported shall
     * be updated as follows:
     * if the PositionAware feature is supported:
     * • TargetPositionLiftPercent100ths attribute shall be set to 100.00%.
     * • TargetPositionTiltPercent100ths attribute shall be set to 100.00%.
     * The server positioning attributes will follow the movements, once the movement has successfully finished, the
     * server attributes shall be updated as follows:
     * if the PositionAware feature is supported:
     * • CurrentPositionLiftPercent100ths attribute shall be 100.00%.
     * • CurrentPositionLiftPercentage attribute shall be 100%.
     * • CurrentPositionTiltPercent100ths attribute shall be 100.00%.
     * • CurrentPositionTiltPercentage attribute shall be 100%.
     * if the AbsolutePosition feature is supported:
     * • CurrentPositionLift attribute shall be equal to the InstalledClosedLimitLift attribute.
     * • CurrentPositionTilt attribute shall be equal to the InstalledClosedLimitTilt attribute.
     */
    public static ClusterCommand downOrClose() {
        return new ClusterCommand("downOrClose");
    }

    /**
     * Upon receipt of this command, the window covering will stop any adjusting to the physical tilt and lift/slide
     * that is currently occurring. The server attributes supported shall be updated as follows:
     * • TargetPositionLiftPercent100ths attribute will be set to CurrentPositionLiftPercent100ths attribute value.
     * • TargetPositionTiltPercent100ths attribute will be set to CurrentPositionTiltPercent100ths attribute value.
     */
    public static ClusterCommand stopMotion() {
        return new ClusterCommand("stopMotion");
    }

    public static ClusterCommand goToLiftValue(Integer liftValue) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (liftValue != null) {
            map.put("liftValue", liftValue);
        }
        return new ClusterCommand("goToLiftValue", map);
    }

    /**
     * Upon receipt of this command, the server will adjust the window covering to the lift/slide percentage specified
     * in the payload of this command.
     * If the command includes LiftPercent100thsValue, then TargetPositionLiftPercent100ths attribute shall be set to
     * LiftPercent100thsValue. Otherwise the TargetPositionLiftPercent100ths attribute shall be set to
     * LiftPercentageValue * 100.
     * If a client includes LiftPercent100thsValue in the command, the LiftPercentageValue shall be set to
     * LiftPercent100thsValue / 100, so a legacy server which only supports LiftPercentageValue (not
     * LiftPercent100thsValue) has a value to set the target position.
     * If the server does not support the PositionAware feature, then a zero percentage shall be treated as a UpOrOpen
     * command and a non-zero percentage shall be treated as an DownOrClose command. If the device is only a tilt
     * control device, then the command SHOULD be ignored and a UNSUPPORTED_COMMAND status SHOULD be returned.
     */
    public static ClusterCommand goToLiftPercentage(Integer liftPercent100thsValue) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (liftPercent100thsValue != null) {
            map.put("liftPercent100thsValue", liftPercent100thsValue);
        }
        return new ClusterCommand("goToLiftPercentage", map);
    }

    public static ClusterCommand goToTiltValue(Integer tiltValue) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (tiltValue != null) {
            map.put("tiltValue", tiltValue);
        }
        return new ClusterCommand("goToTiltValue", map);
    }

    /**
     * Upon receipt of this command, the server will adjust the window covering to the tilt percentage specified in the
     * payload of this command.
     * If the command includes TiltPercent100thsValue, then TargetPositionTiltPercent100ths attribute shall be set to
     * TiltPercent100thsValue. Otherwise the TargetPositionTiltPercent100ths attribute shall be set to
     * TiltPercentageValue * 100.
     * If a client includes TiltPercent100thsValue in the command, the TiltPercentageValue shall be set to
     * TiltPercent100thsValue / 100, so a legacy server which only supports TiltPercentageValue (not
     * TiltPercent100thsValue) has a value to set the target position.
     * If the server does not support the PositionAware feature, then a zero percentage shall be treated as a UpOrOpen
     * command and a non-zero percentage shall be treated as an DownOrClose command. If the device is only a tilt
     * control device, then the command SHOULD be ignored and a UNSUPPORTED_COMMAND status SHOULD be returned.
     */
    public static ClusterCommand goToTiltPercentage(Integer tiltPercent100thsValue) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (tiltPercent100thsValue != null) {
            map.put("tiltPercent100thsValue", tiltPercent100thsValue);
        }
        return new ClusterCommand("goToTiltPercentage", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "type : " + type + "\n";
        str += "physicalClosedLimitLift : " + physicalClosedLimitLift + "\n";
        str += "physicalClosedLimitTilt : " + physicalClosedLimitTilt + "\n";
        str += "currentPositionLift : " + currentPositionLift + "\n";
        str += "currentPositionTilt : " + currentPositionTilt + "\n";
        str += "numberOfActuationsLift : " + numberOfActuationsLift + "\n";
        str += "numberOfActuationsTilt : " + numberOfActuationsTilt + "\n";
        str += "configStatus : " + configStatus + "\n";
        str += "currentPositionLiftPercentage : " + currentPositionLiftPercentage + "\n";
        str += "currentPositionTiltPercentage : " + currentPositionTiltPercentage + "\n";
        str += "operationalStatus : " + operationalStatus + "\n";
        str += "targetPositionLiftPercent100ths : " + targetPositionLiftPercent100ths + "\n";
        str += "targetPositionTiltPercent100ths : " + targetPositionTiltPercent100ths + "\n";
        str += "endProductType : " + endProductType + "\n";
        str += "currentPositionLiftPercent100ths : " + currentPositionLiftPercent100ths + "\n";
        str += "currentPositionTiltPercent100ths : " + currentPositionTiltPercent100ths + "\n";
        str += "installedOpenLimitLift : " + installedOpenLimitLift + "\n";
        str += "installedClosedLimitLift : " + installedClosedLimitLift + "\n";
        str += "installedOpenLimitTilt : " + installedOpenLimitTilt + "\n";
        str += "installedClosedLimitTilt : " + installedClosedLimitTilt + "\n";
        str += "mode : " + mode + "\n";
        str += "safetyStatus : " + safetyStatus + "\n";
        return str;
    }
}
