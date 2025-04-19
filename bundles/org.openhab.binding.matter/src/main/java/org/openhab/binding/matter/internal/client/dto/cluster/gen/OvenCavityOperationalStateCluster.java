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
 * OvenCavityOperationalState
 *
 * @author Dan Cunningham - Initial contribution
 */
public class OvenCavityOperationalStateCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0048;
    public static final String CLUSTER_NAME = "OvenCavityOperationalState";
    public static final String CLUSTER_PREFIX = "ovenCavityOperationalState";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";

    public Integer clusterRevision; // 65533 ClusterRevision
    // Structs

    /**
     * The OperationalStateStruct is used to indicate a possible state of the device.
     */
    public class OperationalStateStruct {
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

    public class ErrorStateStruct {
        /**
         * This shall be populated with a value from the ErrorStateEnum.
         */
        public ErrorStateEnum errorStateID; // ErrorStateEnum
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

        public ErrorStateStruct(ErrorStateEnum errorStateID, String errorStateLabel, String errorStateDetails) {
            this.errorStateID = errorStateID;
            this.errorStateLabel = errorStateLabel;
            this.errorStateDetails = errorStateDetails;
        }
    }

    // Enums
    /**
     * This type defines the set of known operational state values, and is derived from enum8. The following table
     * defines the applicable ranges for values that are defined within this type. All values that are undefined shall
     * be treated as reserved. As shown by the table, states that may be specific to a certain Device Type or other
     * modality shall be defined in a derived cluster of this cluster.
     * The derived cluster-specific state definitions shall NOT duplicate any general state definitions. That is, a
     * derived cluster specification of this cluster cannot define states with the same semantics as the general states
     * defined below.
     * A manufacturer-specific state definition shall NOT duplicate the general state definitions or derived cluster
     * state definitions. That is, a manufacturer-defined state defined for this cluster or a derived cluster thereof
     * cannot define a state with the same semantics as the general states defined below or states defined in a derived
     * cluster. Such manufacturer-specific state definitions shall be scoped in the context of the Vendor ID present in
     * the Basic Information cluster.
     * The following table defines the generally applicable states.
     */
    public enum OperationalStateEnum implements MatterEnum {
        STOPPED(0, "Stopped"),
        RUNNING(1, "Running"),
        PAUSED(2, "Paused"),
        ERROR(3, "Error");

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
     * This type defines the set of known operational error values, and is derived from enum8. The following table
     * defines the applicable ranges for values that are defined within this type. All values that are undefined shall
     * be treated as reserved. As shown by the table, errors that may be specific to a certain Device Type or other
     * modality shall be defined in a derived cluster of this cluster.
     * The derived cluster-specific error definitions shall NOT duplicate the general error definitions.
     * That is, a derived cluster specification of this cluster cannot define errors with the same semantics as the
     * general errors defined below.
     * The manufacturer-specific error definitions shall NOT duplicate the general error definitions or derived
     * cluster-specific error definitions. That is, a manufacturer-defined error defined for this cluster or a derived
     * cluster thereof cannot define errors with the same semantics as the general errors defined below or errors
     * defined in a derived cluster. Such manufacturer-specific error definitions shall be scoped in the context of the
     * Vendor ID present in the Basic Information cluster.
     * The set of ErrorStateID field values defined in each of the generic or derived Operational State cluster
     * specifications is called ErrorState.
     */
    public enum ErrorStateEnum implements MatterEnum {
        NO_ERROR(0, "NoError"),
        UNABLE_TO_START_OR_RESUME(1, "UnableToStartOrResume"),
        UNABLE_TO_COMPLETE_OPERATION(2, "UnableToCompleteOperation"),
        COMMAND_INVALID_IN_STATE(3, "CommandInvalidInState");

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

    public OvenCavityOperationalStateCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 72, "OvenCavityOperationalState");
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        return str;
    }
}
