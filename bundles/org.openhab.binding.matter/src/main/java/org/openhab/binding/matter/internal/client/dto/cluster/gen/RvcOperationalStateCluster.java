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
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;

/**
 * RvcOperationalState
 *
 * @author Dan Cunningham - Initial contribution
 */
public class RvcOperationalStateCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0061;
    public static final String CLUSTER_NAME = "RvcOperationalState";
    public static final String CLUSTER_PREFIX = "rvcOperationalState";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_PHASE_LIST = "phaseList";
    public static final String ATTRIBUTE_CURRENT_PHASE = "currentPhase";
    public static final String ATTRIBUTE_COUNTDOWN_TIME = "countdownTime";
    public static final String ATTRIBUTE_OPERATIONAL_STATE_LIST = "operationalStateList";
    public static final String ATTRIBUTE_OPERATIONAL_STATE = "operationalState";
    public static final String ATTRIBUTE_OPERATIONAL_ERROR = "operationalError";

    public Integer clusterRevision; // 65533 ClusterRevision
    /**
     * Indicates a list of names of different phases that the device can go through for the selected function or mode.
     * The list may not be in sequence order. For example in a washing machine this could include items such as
     * &quot;pre-soak&quot;, &quot;rinse&quot;, and &quot;spin&quot;. These phases are manufacturer specific and may
     * change when a different function or mode is selected.
     * A null value indicates that the device does not present phases during its operation. When this attribute’s value
     * is null, the CurrentPhase attribute shall also be set to null.
     */
    public List<String> phaseList; // 0 list R V
    /**
     * This attribute represents the current phase of operation being performed by the server. This shall be the
     * positional index representing the value from the set provided in the PhaseList Attribute, where the first item in
     * that list is an index of 0. Thus, this attribute shall have a maximum value that is &quot;length(PhaseList) -
     * 1&quot;.
     * Null if the PhaseList attribute is null or if the PhaseList attribute is an empty list.
     */
    public Integer currentPhase; // 1 uint8 R V
    /**
     * Indicates the estimated time left before the operation is completed, in seconds.
     * A value of 0 (zero) means that the operation has completed.
     * A value of null represents that there is no time currently defined until operation completion. This may happen,
     * for example, because no operation is in progress or because the completion time is unknown.
     * Changes to this attribute shall only be marked as reportable in the following cases:
     * • If it has changed due to a change in the CurrentPhase or OperationalState attributes, or
     * • When it changes from 0 to any other value and vice versa, or
     * • When it changes from null to any other value and vice versa, or
     * • When it increases, or
     * • When there is any increase or decrease in the estimated time remaining that was due to progressing insight of
     * the server’s control logic, or
     * • When it changes at a rate significantly different from one unit per second.
     * Changes to this attribute merely due to the normal passage of time with no other dynamic change of device state
     * shall NOT be reported.
     * As this attribute is not being reported during a regular countdown, clients SHOULD NOT rely on the reporting of
     * this attribute in order to keep track of the remaining duration.
     */
    public Integer countdownTime; // 2 elapsed-s R V
    /**
     * This attribute describes the set of possible operational states that the device exposes. An operational state is
     * a fundamental device state such as Running or Error. Details of the phase of a device when, for example, in a
     * state of Running are provided by the CurrentPhase attribute.
     * All devices shall, at a minimum, expose the set of states matching the commands that are also supported by the
     * cluster instance, in addition to Error. The set of possible device states are defined in the
     * OperationalStateEnum. A device type requiring implementation of this cluster shall define the set of states that
     * are applicable to that specific device type.
     */
    public List<OperationalStateStruct> operationalStateList; // 3 list R V
    /**
     * This attribute specifies the current operational state of a device. This shall be populated with a valid
     * OperationalStateID from the set of values in the OperationalStateList Attribute.
     */
    public OperationalStateEnum operationalState; // 4 OperationalStateEnum R V
    /**
     * This attribute shall specify the details of any current error condition being experienced on the device when the
     * OperationalState attribute is populated with Error. Please see ErrorStateStruct for general requirements on the
     * population of this attribute.
     * When there is no error detected, this shall have an ErrorStateID of NoError.
     */
    public ErrorStateStruct operationalError; // 5 ErrorStateStruct R V

    // Structs
    /**
     * This event is generated when a reportable error condition is detected. A device that generates this event shall
     * also set the OperationalState attribute to Error, indicating an error condition.
     * This event shall contain the following fields:
     */
    public static class OperationalError {
        public ErrorStateStruct errorState; // ErrorStateStruct

        public OperationalError(ErrorStateStruct errorState) {
            this.errorState = errorState;
        }
    }

    /**
     * This event SHOULD be generated when the overall operation ends, successfully or otherwise. For example, the
     * completion of a cleaning operation in a Robot Vacuum Cleaner, or the completion of a wash cycle in a Washing
     * Machine.
     * It is highly recommended that appliances device types employing the Operational State cluster support this event,
     * even if it is optional. This assists clients in executing automations or issuing notifications at critical points
     * in the device operation cycles.
     * This event shall contain the following fields:
     */
    public static class OperationCompletion {
        /**
         * This field provides an indication of the state at the end of the operation. This field shall have a value
         * from the ErrorStateEnum set. A value of NoError indicates success, that is, no error has been detected.
         */
        public Integer completionErrorCode; // enum8
        /**
         * The total operational time, in seconds, from when the operation was started via an initial Start command or
         * autonomous/manual starting action, until the operation completed. This includes any time spent while paused.
         * There may be cases whereby the total operational time exceeds the maximum value that can be conveyed by this
         * attribute, in such instances, this attribute shall be populated with null.
         */
        public Integer totalOperationalTime; // elapsed-s
        /**
         * The total time spent in the paused state, in seconds. There may be cases whereby the total paused time
         * exceeds the maximum value that can be conveyed by this attribute, in such instances, this attribute shall be
         * populated with null.
         */
        public Integer pausedTime; // elapsed-s

        public OperationCompletion(Integer completionErrorCode, Integer totalOperationalTime, Integer pausedTime) {
            this.completionErrorCode = completionErrorCode;
            this.totalOperationalTime = totalOperationalTime;
            this.pausedTime = pausedTime;
        }
    }

    /**
     * The OperationalStateStruct is used to indicate a possible state of the device.
     */
    public static class OperationalStateStruct {
        /**
         * This shall be populated with a value from the OperationalStateEnum.
         */
        public OperationalStateEnum operationalStateId; // OperationalStateEnum
        /**
         * This field shall be present if the OperationalStateID is from the set reserved for Manufacturer Specific
         * States, otherwise it shall NOT be present. If present, this shall contain a human-readable description of the
         * operational state.
         */
        public String operationalStateLabel; // string

        public OperationalStateStruct(OperationalStateEnum operationalStateId, String operationalStateLabel) {
            this.operationalStateId = operationalStateId;
            this.operationalStateLabel = operationalStateLabel;
        }
    }

    public static class ErrorStateStruct {
        /**
         * This shall be populated with a value from the ErrorStateEnum.
         */
        public ErrorStateEnum errorStateId; // ErrorStateEnum
        /**
         * This field shall be present if the ErrorStateID is from the set reserved for Manufacturer Specific Errors,
         * otherwise it shall NOT be present. If present, this shall contain a human-readable description of the
         * ErrorStateID; e.g. for a manufacturer specific ErrorStateID of &quot;0x80&quot; the ErrorStateLabel may
         * contain &quot;My special error&quot;.
         */
        public String errorStateLabel; // string
        /**
         * This shall be a human-readable string that provides details about the error condition. As an example, if the
         * ErrorStateID indicates that the device is a Robotic Vacuum that is stuck, the ErrorStateDetails contains
         * &quot;left wheel blocked&quot;.
         */
        public String errorStateDetails; // string

        public ErrorStateStruct(ErrorStateEnum errorStateId, String errorStateLabel, String errorStateDetails) {
            this.errorStateId = errorStateId;
            this.errorStateLabel = errorStateLabel;
            this.errorStateDetails = errorStateDetails;
        }
    }

    // Enums
    /**
     * The values defined herein are applicable to this derived cluster of Operational State only and are additional to
     * the set of values defined in Operational State itself.
     * RVC Pause Compatibility defines the compatibility of the states this cluster defines with the Pause command.
     * ### Table 13. RVC Pause Compatibility
     * RVC Resume Compatibility defines the compatibility of the states this cluster defines with the Resume command.
     * ### Table 14. RVC Resume Compatibility
     * While in the Charging or Docked states, the device shall NOT attempt to resume unless it transitioned to those
     * states while operating and can resume, such as, for example, if it is recharging while in a cleaning cycle. Else,
     * if the operational state is Charging or Docked but there’s no operation to resume or the operation can’t be
     * resumed, the device shall respond with an OperationalCommandResponse command with an ErrorStateID of
     * CommandInvalidInState but take no further action.
     */
    public enum OperationalStateEnum implements MatterEnum {
        STOPPED(0, "Stopped"),
        RUNNING(1, "Running"),
        PAUSED(2, "Paused"),
        ERROR(3, "Error"),
        SEEKING_CHARGER(64, "Seeking Charger"),
        CHARGING(65, "Charging"),
        DOCKED(66, "Docked");

        public final Integer value;
        public final String label;

        private OperationalStateEnum(Integer value, String label) {
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
     * The values defined herein are applicable to this derived cluster of Operational State only and are additional to
     * the set of values defined in Operational State itself.
     */
    public enum ErrorStateEnum implements MatterEnum {
        NO_ERROR(0, "No Error"),
        UNABLE_TO_START_OR_RESUME(1, "Unable To Start Or Resume"),
        UNABLE_TO_COMPLETE_OPERATION(2, "Unable To Complete Operation"),
        COMMAND_INVALID_IN_STATE(3, "Command Invalid In State"),
        FAILED_TO_FIND_CHARGING_DOCK(64, "Failed To Find Charging Dock"),
        STUCK(65, "Stuck"),
        DUST_BIN_MISSING(66, "Dust Bin Missing"),
        DUST_BIN_FULL(67, "Dust Bin Full"),
        WATER_TANK_EMPTY(68, "Water Tank Empty"),
        WATER_TANK_MISSING(69, "Water Tank Missing"),
        WATER_TANK_LID_OPEN(70, "Water Tank Lid Open"),
        MOP_CLEANING_PAD_MISSING(71, "Mop Cleaning Pad Missing");

        public final Integer value;
        public final String label;

        private ErrorStateEnum(Integer value, String label) {
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

    public RvcOperationalStateCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 97, "RvcOperationalState");
    }

    protected RvcOperationalStateCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command shall be supported if the device supports remotely pausing the operation. If this command is
     * supported, the Resume command shall also be supported.
     * On receipt of this command, the device shall pause its operation if it is possible based on the current function
     * of the server. For example, if it is at a point where it is safe to do so and/or permitted, but can be restarted
     * from the point at which pause occurred.
     * If this command is received when already in the Paused state the device shall respond with an
     * OperationalCommandResponse command with an ErrorStateID of NoError but take no further action.
     * A device that receives this command in any state which is not Pause-compatible shall respond with an
     * OperationalCommandResponse command with an ErrorStateID of CommandInvalidInState and shall take no further
     * action.
     * States are defined as Pause-compatible as follows:
     * • For states defined in this cluster specification, in Table 3, “Pause Compatibility”.
     * • For states defined by derived cluster specifications, in the corresponding specifications.
     * • For manufacturer-specific states, by the manufacturer.
     * A device that is unable to honor the Pause command for whatever reason shall respond with an
     * OperationalCommandResponse command with an ErrorStateID of CommandInvalidInState but take no further action.
     * Otherwise, on success:
     * • The OperationalState attribute shall be set to Paused.
     * • The device shall respond with an OperationalCommandResponse command with an ErrorStateID of NoError.
     * The following table defines the compatibility of this cluster’s states with the Pause command.
     * ### Table 3. Pause Compatibility
     */
    public static ClusterCommand pause() {
        return new ClusterCommand("pause");
    }

    /**
     * This command shall be supported if the device supports remotely stopping the operation.
     * On receipt of this command, the device shall stop its operation if it is at a position where it is safe to do so
     * and/or permitted. Restart of the device following the receipt of the Stop command shall require attended
     * operation unless remote start is allowed by the device type and any jurisdiction governing remote operation of
     * the device.
     * If this command is received when already in the Stopped state the device shall respond with an
     * OperationalCommandResponse command with an ErrorStateID of NoError but take no further action.
     * A device that is unable to honor the Stop command for whatever reason shall respond with an
     * OperationalCommandResponse command with an ErrorStateID of CommandInvalidInState but take no further action.
     * Otherwise, on success:
     * • The OperationalState attribute shall be set to Stopped.
     * • The device shall respond with an OperationalCommandResponse command with an ErrorStateID of NoError.
     */
    public static ClusterCommand stop() {
        return new ClusterCommand("stop");
    }

    /**
     * This command shall be supported if the device supports remotely starting the operation. If this command is
     * supported, the &#x27;Stop command shall also be supported.
     * On receipt of this command, the device shall start its operation if it is safe to do so and the device is in an
     * operational state from which it can be started. There may be either regulatory or manufacturer-imposed safety and
     * security requirements that first necessitate some specific action at the device before a Start command can be
     * honored. In such instances, a device shall respond with a status code of CommandInvalidInState if a Start command
     * is received prior to the required on-device action.
     * If this command is received when already in the Running state the device shall respond with an
     * OperationalCommandResponse command with an ErrorStateID of NoError but take no further action.
     * A device that is unable to honor the Start command for whatever reason shall respond with an
     * OperationalCommandResponse command with an ErrorStateID of UnableToStartOrResume but take no further action.
     * Otherwise, on success:
     * • The OperationalState attribute shall be set to Running.
     * • The device shall respond with an OperationalCommandResponse command with an ErrorStateID of NoError.
     */
    public static ClusterCommand start() {
        return new ClusterCommand("start");
    }

    /**
     * This command shall be supported if the device supports remotely resuming the operation. If this command is
     * supported, the Pause command shall also be supported.
     * On receipt of this command, the device shall resume its operation from the point it was at when it received the
     * Pause command, or from the point when it was paused by means outside of this cluster (for example by manual
     * button press).
     * If this command is received when already in the Running state the device shall respond with an
     * OperationalCommandResponse command with an ErrorStateID of NoError but take no further action.
     * A device that receives this command in any state which is not Resume-compatible shall respond with an
     * OperationalCommandResponse command with an ErrorStateID of CommandInvalidInState and shall take no further
     * action.
     * States are defined as Resume-compatible as follows:
     * • For states defined in this cluster specification, in Table 4, “Resume Compatibility”.
     * • For states defined by derived cluster specifications, in the corresponding specifications.
     * • For manufacturer-specific states, by the manufacturer.
     * The following table defines the compatibility of this cluster’s states with the Resume command.
     * ### Table 4. Resume Compatibility
     * A device that is unable to honor the Resume command for any other reason shall respond with an
     * OperationalCommandResponse command with an ErrorStateID of UnableToStartOrResume but take no further action.
     * Otherwise, on success:
     * • The OperationalState attribute shall be set to the most recent non-Error operational state prior to entering
     * the Paused state.
     * • The device shall respond with an OperationalCommandResponse command with an ErrorStateID of NoError.
     */
    public static ClusterCommand resume() {
        return new ClusterCommand("resume");
    }

    /**
     * On receipt of this command, the device shall start seeking the charging dock, if possible in the current state of
     * the device.
     * If this command is received when already in the SeekingCharger state the device shall respond with an
     * OperationalCommandResponse command with an ErrorStateID of NoError but the command shall have no other effect.
     * A device that receives this command in any state which does not allow seeking the charger, such as Charging or
     * Docked, shall respond with an OperationalCommandResponse command with an ErrorStateID of CommandInvalidInState
     * and shall have no other effect.
     * Otherwise, on success:
     * • The OperationalState attribute shall be set to SeekingCharger.
     * • The device shall respond with an OperationalCommandResponse command with an ErrorStateID of NoError.
     */
    public static ClusterCommand goHome() {
        return new ClusterCommand("goHome");
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "phaseList : " + phaseList + "\n";
        str += "currentPhase : " + currentPhase + "\n";
        str += "countdownTime : " + countdownTime + "\n";
        str += "operationalStateList : " + operationalStateList + "\n";
        str += "operationalState : " + operationalState + "\n";
        str += "operationalError : " + operationalError + "\n";
        return str;
    }
}
