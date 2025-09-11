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
 * ValveConfigurationAndControl
 *
 * @author Dan Cunningham - Initial contribution
 */
public class ValveConfigurationAndControlCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0081;
    public static final String CLUSTER_NAME = "ValveConfigurationAndControl";
    public static final String CLUSTER_PREFIX = "valveConfigurationAndControl";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_OPEN_DURATION = "openDuration";
    public static final String ATTRIBUTE_DEFAULT_OPEN_DURATION = "defaultOpenDuration";
    public static final String ATTRIBUTE_AUTO_CLOSE_TIME = "autoCloseTime";
    public static final String ATTRIBUTE_REMAINING_DURATION = "remainingDuration";
    public static final String ATTRIBUTE_CURRENT_STATE = "currentState";
    public static final String ATTRIBUTE_TARGET_STATE = "targetState";
    public static final String ATTRIBUTE_CURRENT_LEVEL = "currentLevel";
    public static final String ATTRIBUTE_TARGET_LEVEL = "targetLevel";
    public static final String ATTRIBUTE_DEFAULT_OPEN_LEVEL = "defaultOpenLevel";
    public static final String ATTRIBUTE_VALVE_FAULT = "valveFault";
    public static final String ATTRIBUTE_LEVEL_STEP = "levelStep";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * Indicates the total duration, in seconds, for which the valve will remain open for this current opening.
     * A value of null shall indicate the duration is not set, meaning that the valve will remain open until closed by
     * the user or some other automation.
     */
    public Integer openDuration; // 0 elapsed-s R V
    /**
     * Indicates the default duration, in seconds, for which the valve will remain open, if the OpenDuration field is
     * not present in the Open command.
     * A value of null shall indicate the duration is not set, meaning that the valve will remain open until closed by
     * the user or some other automation.
     */
    public Integer defaultOpenDuration; // 1 elapsed-s RW VO
    /**
     * Indicates the UTC time when the valve will close, depending on value of the OpenDuration attribute.
     * Null:
     * • When OpenDuration is null, or
     * • When the valve does not have a synchronized UTCTime in the Time Synchronization cluster, or
     * • When the valve is closed.
     * When the value of this attribute is earlier or equal to the current UTC time, the valve shall automatically
     * transition to its closed position. The behavior of transitioning to the closed position, shall match the behavior
     * described in the Close command.
     * If this attribute is not null and the Time Synchronization cluster receives a SetUTCTime command, modifying the
     * current UTC time of the device, the value of this attribute shall be adjusted to match the new UTC time plus the
     * value of the RemainingDuration attribute.
     */
    public BigInteger autoCloseTime; // 2 epoch-us R V
    /**
     * Indicates the remaining duration, in seconds, until the valve closes. Null:
     * • When OpenDuration is null, or
     * • When the valve is closed.
     * The value of this attribute shall only be reported in the following cases:
     * • When it changes from null to any other value and vice versa, or
     * • When it changes to 0, or
     * • When it increases, or
     * • When the closing time changes.
     * Meaning that clients SHOULD NOT rely on the reporting of this attribute in order to keep track of the remaining
     * duration, due to this attribute not being reported during regular countdown.
     * When reading this attribute it shall return the remaining duration, in seconds, until the valve closes.
     * When the value of this attribute counts down to 0, the valve shall automatically transition to its closed
     * position. The behavior of transitioning to the closed position shall match the behavior described in the Close
     * command.
     */
    public Integer remainingDuration; // 3 elapsed-s R V
    /**
     * Indicates the current state of the valve.
     * A value of null shall indicate that the current state is not known.
     */
    public ValveStateEnum currentState; // 4 ValveStateEnum R V
    /**
     * Indicates the target state, while changing the state, of the valve.
     * A value of null shall indicate that no target position is set, since the change in state is either done or
     * failed.
     */
    public ValveStateEnum targetState; // 5 ValveStateEnum R V
    /**
     * Indicates the current level of the valve as a percentage value, between fully closed and fully open. During a
     * transition from one level to another level, the valve SHOULD keep this attribute updated to the best of its
     * ability, in order to represent the actual level of the valve during the movement.
     * A value of 100 percent shall indicate the fully open position. A value of 0 percent shall indicate the fully
     * closed position.
     * A value of null shall indicate that the current state is not known.
     */
    public Integer currentLevel; // 6 percent R V
    /**
     * Indicates the target level of the valve as a percentage value, between fully closed and fully open.
     * The interpretation of the percentage value is the same as for the CurrentLevel attribute.
     * A value of null shall indicate that no target position is set, since the change of level is either done or
     * failed.
     */
    public Integer targetLevel; // 7 percent R V
    /**
     * Indicates the default value used for the TargetLevel attribute, when a valve transitions from the closed to the
     * open state, caused by an Open command, if a TargetLevel field is not present in the Open command.
     * If the LevelStep attribute is present and the value of a write interaction to this attribute field is not 100,
     * the value shall be a supported value as defined by the LevelStep attribute, such that (Value received in the
     * write interaction) % (Value of LevelStep attribute) equals 0. If the resulting value is not 0, the requested
     * DefaultOpenLevel value is considered an unsupported value and a CONSTRAINT_ERROR status shall be returned.
     */
    public Integer defaultOpenLevel; // 8 percent RW VO
    /**
     * Indicates any faults registered by the valve.
     */
    public ValveFaultBitmap valveFault; // 9 ValveFaultBitmap R V
    /**
     * Indicates the step size the valve can support.
     * The step size defined by this attribute is counted from 0 and the final step towards 100 may be different than
     * what is defined in this attribute. For example, if the value of this attribute is 15, it results in these target
     * values being supported; 0, 15, 30, 45, 60, 75, 90 and 100.
     * The values of 0 and 100 shall always be supported, regardless of the value of this attribute.
     */
    public Integer levelStep; // 10 uint8 R V

    // Structs
    /**
     * This event shall be generated when the valve state changed. For level changes, after the end of movement, for
     * state changes when the new state has been reached.
     */
    public static class ValveStateChanged {
        /**
         * This field shall indicate the new state of the valve.
         */
        public ValveStateEnum valveState; // ValveStateEnum
        /**
         * This field shall indicate the new level of the valve.
         */
        public Integer valveLevel; // percent

        public ValveStateChanged(ValveStateEnum valveState, Integer valveLevel) {
            this.valveState = valveState;
            this.valveLevel = valveLevel;
        }
    }

    /**
     * This event shall be generated when the valve registers or clears a fault, e.g. not being able to transition to
     * the requested target level or state.
     */
    public static class ValveFault {
        /**
         * This field shall indicate the value of the ValveFault attribute, at the time this event is generated.
         */
        public ValveFaultBitmap valveFault; // ValveFaultBitmap

        public ValveFault(ValveFaultBitmap valveFault) {
            this.valveFault = valveFault;
        }
    }

    // Enums
    public enum ValveStateEnum implements MatterEnum {
        CLOSED(0, "Closed"),
        OPEN(1, "Open"),
        TRANSITIONING(2, "Transitioning");

        public final Integer value;
        public final String label;

        private ValveStateEnum(Integer value, String label) {
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

    public enum StatusCodeEnum implements MatterEnum {
        FAILURE_DUE_TO_FAULT(2, "Failure Due To Fault");

        public final Integer value;
        public final String label;

        private StatusCodeEnum(Integer value, String label) {
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
    public static class ValveFaultBitmap {
        public boolean generalFault;
        public boolean blocked;
        public boolean leaking;
        public boolean notConnected;
        public boolean shortCircuit;
        public boolean currentExceeded;

        public ValveFaultBitmap(boolean generalFault, boolean blocked, boolean leaking, boolean notConnected,
                boolean shortCircuit, boolean currentExceeded) {
            this.generalFault = generalFault;
            this.blocked = blocked;
            this.leaking = leaking;
            this.notConnected = notConnected;
            this.shortCircuit = shortCircuit;
            this.currentExceeded = currentExceeded;
        }
    }

    public static class FeatureMap {
        /**
         * 
         * This feature shall indicate that the valve uses Time Synchronization and UTC time to indicate duration and
         * auto close time.
         * This feature shall NOT be supported unless the device supports the Time Synchronization cluster.
         */
        public boolean timeSync;
        /**
         * 
         * This feature shall indicate that the valve is capable of being adjusted to a specific position, as a
         * percentage, of its full range of motion.
         */
        public boolean level;

        public FeatureMap(boolean timeSync, boolean level) {
            this.timeSync = timeSync;
            this.level = level;
        }
    }

    public ValveConfigurationAndControlCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 129, "ValveConfigurationAndControl");
    }

    protected ValveConfigurationAndControlCluster(BigInteger nodeId, int endpointId, int clusterId,
            String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command is used to set the valve to its open position.
     */
    public static ClusterCommand open(Integer openDuration, Integer targetLevel) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (openDuration != null) {
            map.put("openDuration", openDuration);
        }
        if (targetLevel != null) {
            map.put("targetLevel", targetLevel);
        }
        return new ClusterCommand("open", map);
    }

    /**
     * This command is used to set the valve to its closed position.
     */
    public static ClusterCommand close() {
        return new ClusterCommand("close");
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "openDuration : " + openDuration + "\n";
        str += "defaultOpenDuration : " + defaultOpenDuration + "\n";
        str += "autoCloseTime : " + autoCloseTime + "\n";
        str += "remainingDuration : " + remainingDuration + "\n";
        str += "currentState : " + currentState + "\n";
        str += "targetState : " + targetState + "\n";
        str += "currentLevel : " + currentLevel + "\n";
        str += "targetLevel : " + targetLevel + "\n";
        str += "defaultOpenLevel : " + defaultOpenLevel + "\n";
        str += "valveFault : " + valveFault + "\n";
        str += "levelStep : " + levelStep + "\n";
        return str;
    }
}
