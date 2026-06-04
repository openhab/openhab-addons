/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;

/**
 * ClosureControl
 *
 * @author Dan Cunningham - Initial contribution
 */
public class ClosureControlCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0104;
    public static final String CLUSTER_NAME = "ClosureControl";
    public static final String CLUSTER_PREFIX = "closureControl";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_COUNTDOWN_TIME = "countdownTime";
    public static final String ATTRIBUTE_MAIN_STATE = "mainState";
    public static final String ATTRIBUTE_CURRENT_ERROR_LIST = "currentErrorList";
    public static final String ATTRIBUTE_OVERALL_CURRENT_STATE = "overallCurrentState";
    public static final String ATTRIBUTE_OVERALL_TARGET_STATE = "overallTargetState";
    public static final String ATTRIBUTE_LATCH_CONTROL_MODES = "latchControlModes";

    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * Indicates the estimated time left before the operation is completed, in seconds.
     * A value of 0 (zero) means that the operation has completed.
     * A value of null indicates that there is no time currently defined until operation completion. This may happen
     * because the completion time is unknown.
     * Changes to this attribute shall only be marked as reportable in the following cases:
     * - If the tracked operation has changed due to a change in the MainState attribute, or
     * - When it changes from 0 to any other value and vice versa, or
     * - When it changes from null to any other value and vice versa, or
     * - When it increases, or
     * - When there is any increase or decrease in the estimated time remaining that was due to progressing insight of
     * the server's control logic.
     * Changes to this attribute merely due to the normal passage of time with no other dynamic change of closure state
     * shall NOT be reported.
     * As this attribute is not being reported during a regular countdown, clients SHOULD NOT rely on the reporting of
     * this attribute in order to keep track of the remaining duration.
     */
    public Integer countdownTime; // 0 elapsed-s R V
    /**
     * Indicates the current operational state of the closure associated with the server.
     * > [!NOTE]
     * > NOTE: The MainState diagram is provided exclusively for informational purposes only and is an exemplary design
     * of the internals of a closure implementation to help illustrate the aspects of function that are considered by
     * the cluster's normative text.
     */
    public MainStateEnum mainState; // 1 MainStateEnum R V
    /**
     * Indicates the currently active errors.
     * An empty list shall indicate that there are no active errors.
     * There shall NOT be duplicate values of ClosureErrorEnum within the CurrentErrorList.
     */
    public List<ClosureErrorEnum> currentErrorList; // 2 list R V
    /**
     * Indicates the current Position, Latch and/or Speed states, whichever are applicable according to the feature
     * flags set.
     * Null, if the state is unknown. Examples could be, but are not limited to:
     * - The state of Position/Latch is not known yet because the closure is not calibrated.
     * - The product has lost its Position/Latch state after manual motion during a shutdown.
     * The values of the different fields within the structure of this attribute depend on:
     * - The effects of MoveTo commands.
     * - The effects of SetTarget and Step commands in a Closure Dimension Cluster associated with this cluster, as
     * described in Section 5.4.4, "Association Between Closure Control and Closure Dimension Clusters".
     * - The Stop command.
     */
    public OverallCurrentStateStruct overallCurrentState; // 3 OverallCurrentStateStruct R V
    /**
     * Indicates the TargetPosition, TargetLatch and/or TargetSpeed values, whichever are applicable according to the
     * feature flags set.
     * Null, if the state is unknown. For example after a reboot.
     */
    public OverallTargetStateStruct overallTargetState; // 4 OverallTargetStateStruct R V
    /**
     * This attribute shall specify whether the latch mechanism can be latched or unlatched remotely.
     */
    public LatchControlModesBitmap latchControlModes; // 5 LatchControlModesBitmap R V

    // Structs
    /**
     * This event shall be generated when a reportable error condition is detected. A closure that generates this event
     * shall also set the MainState attribute to Error, indicating an error condition.
     * This event shall contain the following fields:
     */
    public static class OperationalError {
        public List<ClosureErrorEnum> errorState; // list

        public OperationalError(List<ClosureErrorEnum> errorState) {
            this.errorState = errorState;
        }
    }

    /**
     * This event, if supported, shall be generated when the overall operation ends, either successfully or otherwise.
     * For example, the event is sent upon the completion of a movement operation in a blind.
     */
    public static class MovementCompleted {
        public MovementCompleted() {
        }
    }

    /**
     * This event, if supported, shall be generated when the MainStateEnum attribute changes state to and from
     * disengaged, indicating if the actuator is Engaged or Disengaged.
     * This event shall contain the following fields:
     * - True, when the actuator is in a Engaged state, actuator movements possible.
     * - False, when the actuator is in a Disengaged state, preventing any actuator movements.
     */
    public static class EngageStateChanged {
        public Boolean engageValue; // bool

        public EngageStateChanged(Boolean engageValue) {
            this.engageValue = engageValue;
        }
    }

    /**
     * This event, if supported, shall be generated when the SecureState field in the OverallCurrentState attribute
     * changes. It is used to indicate whether a closure is securing a space against possible unauthorized entry.
     * This event shall contain the following fields:
     * - True, when the closure is in a secure state, e.g. unauthorized/undetectable access is not possible.
     * - False, when the closure is in an insecure state, e.g. unauthorized/undetectable access is possible.
     */
    public static class SecureStateChanged {
        public Boolean secureValue; // bool

        public SecureStateChanged(Boolean secureValue) {
            this.secureValue = secureValue;
        }
    }

    public static class OverallCurrentStateStruct {
        /**
         * This field shall indicate the current Position state of the closure, as defined in the CurrentPositionEnum.
         * When the Positioning (PS) feature flag is set, the rules for setting the value of the Position field are:
         * - If the closure doesn't know accurately its current state the value null shall be used.
         * - Otherwise, the most appropriate supported value shall be used.
         * Clients which only consider the binary opened/closed states of a closure SHOULD consider the closure to be
         * closed if the value of this field is FullyClosed. Otherwise those clients SHOULD consider the closure opened
         * (non-closed).
         */
        public CurrentPositionEnum position; // CurrentPositionEnum
        /**
         * This field shall indicate the current latching state of the closure.
         * When the MotionLatching (LT) feature flag is set, the rules for setting the value of the Latch field are:
         * - If the closure doesn't know its current state, the value shall be null.
         * - Else, if the closure is partially latched or not latched, the value shall be false.
         * - Otherwise, if the closure is fully latched, the value shall be true.
         * > [!NOTE]
         * > NOTE: Some products exposing the MotionLatching (LT) feature might not be able to drive an actuator to
         * achieve a latched state. Such products are built with springs or similar mechanisms to unlatch but require
         * the user to latch manually.
         */
        public Boolean latch; // bool
        /**
         * This field shall indicate the current speed of the closure, as defined in the ThreeLevelAutoEnum.
         * When the Speed (SP) feature flag is set, the rules for setting the value of the Speed field are:
         * If the closure's MainState attribute is currently either in WaitingForMotion or Moving state, the closure's
         * most accurate current overall speed shall be used. Otherwise, the value used shall be the most appropriate
         * default supported speed value.
         */
        public ThreeLevelAutoEnum speed; // ThreeLevelAutoEnum
        /**
         * This field shall indicate the current secure state of the closure.
         * A secure state requires the closure to meet all of the following conditions defined by the
         * OverallCurrentState Struct based on feature support:
         * - If the Positioning feature is supported, then the Position field of OverallCurrentState is FullyClosed.
         * - If the MotionLatching feature is supported, then the Latch field of OverallCurrentState is True.
         * The rules for setting the value of the SecureState field shall be:
         * - True if the closure meets the required conditions for a secure state, preventing unauthorized or
         * undetectable access.
         * - False if the closure does not meet these conditions and unauthorized or undetectable access is possible.
         * - null if the closure's current secure state is unknown.
         * This field provides no additional details regarding mechanical properties of the closure mechanism. It is
         * intended only as supplementary information and not as a replacement for a comprehensive security system. It
         * is primarily useful for closures on the outer shell of objects, such as garage doors, windows, or doors.
         */
        public Boolean secureState; // bool

        public OverallCurrentStateStruct(CurrentPositionEnum position, Boolean latch, ThreeLevelAutoEnum speed,
                Boolean secureState) {
            this.position = position;
            this.latch = latch;
            this.speed = speed;
            this.secureState = secureState;
        }
    }

    public static class OverallTargetStateStruct {
        /**
         * This field shall indicate the target position that the closure is moving to. It shall be null if there is no
         * target position.
         */
        public TargetPositionEnum position; // TargetPositionEnum
        /**
         * This field shall indicate the desired latching state of the closure. It shall be null if there is no desired
         * latching state.
         */
        public Boolean latch; // bool
        /**
         * This field shall indicate the desired speed at which the closure should perform the movement toward the
         * target position. If no speed value has yet been set, the server shall select and set one of the speed values
         * defined in the ThreeLevelAutoEnum.
         */
        public ThreeLevelAutoEnum speed; // ThreeLevelAutoEnum

        public OverallTargetStateStruct(TargetPositionEnum position, Boolean latch, ThreeLevelAutoEnum speed) {
            this.position = position;
            this.latch = latch;
            this.speed = speed;
        }
    }

    // Enums
    public enum CurrentPositionEnum implements MatterEnum {
        FULLY_CLOSED(0, "Fully Closed"),
        FULLY_OPENED(1, "Fully Opened"),
        PARTIALLY_OPENED(2, "Partially Opened"),
        OPENED_FOR_PEDESTRIAN(3, "Opened For Pedestrian"),
        OPENED_FOR_VENTILATION(4, "Opened For Ventilation"),
        OPENED_AT_SIGNATURE(5, "Opened At Signature");

        private final Integer value;
        private final String label;

        private CurrentPositionEnum(Integer value, String label) {
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

    public enum TargetPositionEnum implements MatterEnum {
        MOVE_TO_FULLY_CLOSED(0, "Move To Fully Closed"),
        MOVE_TO_FULLY_OPEN(1, "Move To Fully Open"),
        MOVE_TO_PEDESTRIAN_POSITION(2, "Move To Pedestrian Position"),
        MOVE_TO_VENTILATION_POSITION(3, "Move To Ventilation Position"),
        MOVE_TO_SIGNATURE_POSITION(4, "Move To Signature Position");

        private final Integer value;
        private final String label;

        private TargetPositionEnum(Integer value, String label) {
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

    public enum MainStateEnum implements MatterEnum {
        STOPPED(0, "Stopped"),
        MOVING(1, "Moving"),
        WAITING_FOR_MOTION(2, "Waiting For Motion"),
        ERROR(3, "Error"),
        CALIBRATING(4, "Calibrating"),
        PROTECTED(5, "Protected"),
        DISENGAGED(6, "Disengaged"),
        SETUP_REQUIRED(7, "Setup Required");

        private final Integer value;
        private final String label;

        private MainStateEnum(Integer value, String label) {
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
     * The following ranges are reserved for general and manufacturer specified closure errors.
     * The manufacturer-specific error definitions shall NOT duplicate the general error definitions. Such
     * manufacturer-specific error definitions shall be scoped in the context of the Vendor ID present in the Basic
     * Information cluster.
     * The general closure error values are defined in the table below.
     */
    public enum ClosureErrorEnum implements MatterEnum {
        PHYSICALLY_BLOCKED(0, "Physically Blocked"),
        BLOCKED_BY_SENSOR(1, "Blocked By Sensor"),
        TEMPERATURE_LIMITED(2, "Temperature Limited"),
        MAINTENANCE_REQUIRED(3, "Maintenance Required"),
        INTERNAL_INTERFERENCE(4, "Internal Interference");

        private final Integer value;
        private final String label;

        private ClosureErrorEnum(Integer value, String label) {
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
    public static class LatchControlModesBitmap {
        /**
         * Remote latching capability
         * This bit shall indicate whether the latch supports remote latching or not:
         * - 0 = the latch can only be latched through manual, physical operation.
         * - 1 = the latch can be latched via remote control (e.g., electronic or remote actuation).
         */
        public boolean remoteLatching;
        /**
         * Remote unlatching capability
         * This bit shall indicate whether the latch supports remote unlatching or not:
         * - 0 = the latch can only be unlatched through manual, physical operation.
         * - 1 = the latch can be unlatched via remote control (e.g., electronic or remote actuation).
         */
        public boolean remoteUnlatching;

        public LatchControlModesBitmap(boolean remoteLatching, boolean remoteUnlatching) {
            this.remoteLatching = remoteLatching;
            this.remoteUnlatching = remoteUnlatching;
        }
    }

    public static class FeatureMap {
        /**
         * 
         * This feature shall indicate that the closure can be set to discrete positions, including at minimum the
         * FullyOpen and FullyClosed positions.
         */
        public boolean positioning;
        /**
         * 
         * This feature shall indicate that the closure can be latched and unlatched. When latched, the feature secures
         * an axis, preventing associated actuators from moving components along that axis.
         */
        public boolean motionLatching;
        /**
         * 
         * This feature shall indicate that the closure is capable of changing its position or state instantaneously. As
         * a result, the Speed feature is not applicable, and the Stop command is not usable. In such closures, the
         * OverallCurrentState attribute shall immediately follow the OverallTargetState attribute. The state transition
         * diagram remains applicable, however, transitions involving the Stop state shall be omitted.
         */
        public boolean instantaneous;
        /**
         * 
         * This feature shall indicate that the closure supports configurable speed during motion toward a target
         * position. The feature uses the values in ThreeLevelAutoEnum to set the supported speed levels.
         */
        public boolean speed;
        /**
         * 
         * This feature shall indicate that the closure can be set to a designated Ventilation position (e.g., partially
         * open).
         */
        public boolean ventilation;
        /**
         * 
         * This feature shall indicate that the closure can be set to a dedicated Pedestrian position. The Pedestrian
         * position provides a clear walkway through the closure.
         */
        public boolean pedestrian;
        /**
         * 
         * This feature shall indicate the capability to trigger a calibration procedure. The calibration can either be
         * fully automated, or require manual steps not described in this specification.
         */
        public boolean calibration;
        /**
         * 
         * This feature shall indicate that the closure is capable of activating a form of protection, such as
         * protection against wind. A protection is manufacturer-specific, and it could be a simple software limitation,
         * or a mechanical system deployed by the closure.
         */
        public boolean protection;
        /**
         * 
         * This feature shall indicate that the closure can be operated manually by a user, such as to open a window.
         */
        public boolean manuallyOperable;

        public FeatureMap(boolean positioning, boolean motionLatching, boolean instantaneous, boolean speed,
                boolean ventilation, boolean pedestrian, boolean calibration, boolean protection,
                boolean manuallyOperable) {
            this.positioning = positioning;
            this.motionLatching = motionLatching;
            this.instantaneous = instantaneous;
            this.speed = speed;
            this.ventilation = ventilation;
            this.pedestrian = pedestrian;
            this.calibration = calibration;
            this.protection = protection;
            this.manuallyOperable = manuallyOperable;
        }
    }

    public ClosureControlCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 260, "ClosureControl");
    }

    protected ClosureControlCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * On receipt of this command, the closure shall stop its movement as fast as the closure is able too.
     * If the server's MainState attribute has one of the following values:
     * - Moving
     * - WaitingForMotion
     * - Calibrating
     * then any motions shall be stopped and the MainState attribute shall be set to Stopped.
     * A status code of SUCCESS shall always be returned, regardless of the value of the MainState attribute when this
     * command is received.
     */
    public static ClusterCommand stop() {
        return new ClusterCommand("stop");
    }

    /**
     * On receipt of this command, the closure shall operate to update its position, latch state and/or motion speed.
     * The rationale behind defining the conformance as being purely optional in the table above is to ensure that
     * commands containing one or more fields related to unsupported features are still accepted, rather than being
     * rejected outright. For example, if a simple client, which is not able to determine the capabilities of the
     * server, invokes a command that includes both position and speed, a server that does not support the speed feature
     * would simply ignore the speed field while still adjusting its position as requested.
     */
    public static ClusterCommand moveTo(TargetPositionEnum position, Boolean latch, ThreeLevelAutoEnum speed) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (position != null) {
            map.put("position", position);
        }
        if (latch != null) {
            map.put("latch", latch);
        }
        if (speed != null) {
            map.put("speed", speed);
        }
        return new ClusterCommand("moveTo", map);
    }

    /**
     * This command is used to trigger a calibration of the closure.
     */
    public static ClusterCommand calibrate() {
        return new ClusterCommand("calibrate");
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "featureMap : " + featureMap + "\n";
        str += "countdownTime : " + countdownTime + "\n";
        str += "mainState : " + mainState + "\n";
        str += "currentErrorList : " + currentErrorList + "\n";
        str += "overallCurrentState : " + overallCurrentState + "\n";
        str += "overallTargetState : " + overallTargetState + "\n";
        str += "latchControlModes : " + latchControlModes + "\n";
        return str;
    }
}
