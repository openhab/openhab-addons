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
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;

/**
 * RvcOperationalState
 *
 * @author Dan Cunningham - Initial contribution
 */
public class RvcOperationalStateCluster extends OperationalStateCluster {

    public static final int CLUSTER_ID = 0x0061;
    public static final String CLUSTER_NAME = "RvcOperationalState";
    public static final String CLUSTER_PREFIX = "rvcOperationalState";

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
        return str;
    }
}
