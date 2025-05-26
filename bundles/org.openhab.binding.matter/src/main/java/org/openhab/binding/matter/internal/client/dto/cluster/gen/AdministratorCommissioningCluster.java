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
 * AdministratorCommissioning
 *
 * @author Dan Cunningham - Initial contribution
 */
public class AdministratorCommissioningCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x003C;
    public static final String CLUSTER_NAME = "AdministratorCommissioning";
    public static final String CLUSTER_PREFIX = "administratorCommissioning";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_WINDOW_STATUS = "windowStatus";
    public static final String ATTRIBUTE_ADMIN_FABRIC_INDEX = "adminFabricIndex";
    public static final String ATTRIBUTE_ADMIN_VENDOR_ID = "adminVendorId";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * Indicates whether a new Commissioning window has been opened by an Administrator, using either the
     * OpenCommissioningWindow command or the OpenBasicCommissioningWindow command.
     * This attribute shall revert to WindowNotOpen upon expiry of a commissioning window.
     * &gt; [!NOTE]
     * &gt; An initial commissioning window is not opened using either the OpenCommissioningWindow command or the
     * OpenBasicCommissioningWindow command, and therefore this attribute shall be set to WindowNotOpen on initial
     * commissioning.
     */
    public CommissioningWindowStatusEnum windowStatus; // 0 CommissioningWindowStatusEnum R V
    /**
     * When the WindowStatus attribute is not set to WindowNotOpen, this attribute shall indicate the FabricIndex
     * associated with the Fabric scoping of the Administrator that opened the window. This may be used to
     * cross-reference in the Fabrics attribute of the Node Operational Credentials cluster.
     * If, during an open commissioning window, the fabric for the Administrator that opened the window is removed, then
     * this attribute shall be set to null.
     * When the WindowStatus attribute is set to WindowNotOpen, this attribute shall be set to null.
     */
    public Integer adminFabricIndex; // 1 fabric-idx R V
    /**
     * When the WindowStatus attribute is not set to WindowNotOpen, this attribute shall indicate the Vendor ID
     * associated with the Fabric scoping of the Administrator that opened the window. This field shall match the
     * VendorID field of the Fabrics attribute list entry associated with the Administrator having opened the window, at
     * the time of window opening. If the fabric for the Administrator that opened the window is removed from the node
     * while the commissioning window is still open, this attribute shall NOT be updated.
     * When the WindowStatus attribute is set to WindowNotOpen, this attribute shall be set to null.
     */
    public Integer adminVendorId; // 2 vendor-id R V

    // Enums
    public enum CommissioningWindowStatusEnum implements MatterEnum {
        WINDOW_NOT_OPEN(0, "Window Not Open"),
        ENHANCED_WINDOW_OPEN(1, "Enhanced Window Open"),
        BASIC_WINDOW_OPEN(2, "Basic Window Open");

        public final Integer value;
        public final String label;

        private CommissioningWindowStatusEnum(Integer value, String label) {
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
        BUSY(2, "Busy"),
        PAKE_PARAMETER_ERROR(3, "Pake Parameter Error"),
        WINDOW_NOT_OPEN(4, "Window Not Open");

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
    public static class FeatureMap {
        /**
         * 
         * Node supports Basic Commissioning Method.
         */
        public boolean basic;

        public FeatureMap(boolean basic) {
            this.basic = basic;
        }
    }

    public AdministratorCommissioningCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 60, "AdministratorCommissioning");
    }

    protected AdministratorCommissioningCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command is used by a current Administrator to instruct a Node to go into commissioning mode. The Enhanced
     * Commissioning Method specifies a window of time during which an already commissioned Node accepts PASE sessions.
     * The current Administrator MUST specify a timeout value for the duration of the OpenCommissioningWindow command.
     * When the OpenCommissioningWindow command expires or commissioning completes, the Node shall remove the Passcode
     * by deleting the PAKE passcode verifier as well as stop publishing the DNS-SD record corresponding to this command
     * as described in Section 4.3.1, “Commissionable Node Discovery”. The commissioning into a new Fabric completes
     * when the Node successfully receives a CommissioningComplete command, see Section 5.5, “Commissioning Flows”.
     * The parameters for OpenCommissioningWindow command are as follows:
     * A current Administrator may invoke this command to put a node in commissioning mode for the next Administrator.
     * On completion, the command shall return a cluster specific status code from the Section 11.19.6, “Status Codes”
     * below reflecting success or reasons for failure of the operation. The new Administrator shall discover the Node
     * on the IP network using DNS-based Service Discovery (DNS-SD) for commissioning.
     * If any format or validity errors related to the PAKEPasscodeVerifier, Iterations or Salt arguments arise, this
     * command shall fail with a cluster specific status code of PAKEParameterError.
     * If a commissioning window is already currently open, this command shall fail with a cluster specific status code
     * of Busy.
     * If the fail-safe timer is currently armed, this command shall fail with a cluster specific status code of Busy,
     * since it is likely that concurrent commissioning operations from multiple separate Commissioners are about to
     * take place.
     * In case of any other parameter error, this command shall fail with a status code of COMMAND_INVALID.
     */
    public static ClusterCommand openCommissioningWindow(Integer commissioningTimeout, OctetString pakePasscodeVerifier,
            Integer discriminator, Integer iterations, OctetString salt) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (commissioningTimeout != null) {
            map.put("commissioningTimeout", commissioningTimeout);
        }
        if (pakePasscodeVerifier != null) {
            map.put("pakePasscodeVerifier", pakePasscodeVerifier);
        }
        if (discriminator != null) {
            map.put("discriminator", discriminator);
        }
        if (iterations != null) {
            map.put("iterations", iterations);
        }
        if (salt != null) {
            map.put("salt", salt);
        }
        return new ClusterCommand("openCommissioningWindow", map);
    }

    /**
     * This command may be used by a current Administrator to instruct a Node to go into commissioning mode, if the node
     * supports the Basic Commissioning Method. The Basic Commissioning Method specifies a window of time during which
     * an already commissioned Node accepts PASE sessions. The current Administrator shall specify a timeout value for
     * the duration of the OpenBasicCommissioningWindow command.
     * If a commissioning window is already currently open, this command shall fail with a cluster specific status code
     * of Busy.
     * If the fail-safe timer is currently armed, this command shall fail with a cluster specific status code of Busy,
     * since it is likely that concurrent commissioning operations from multiple separate Commissioners are about to
     * take place.
     * In case of any other parameter error, this command shall fail with a status code of COMMAND_INVALID.
     * The commissioning into a new Fabric completes when the Node successfully receives a CommissioningComplete
     * command, see Section 5.5, “Commissioning Flows”. The new Administrator shall discover the Node on the IP network
     * using DNS-based Service Discovery (DNS-SD) for commissioning.
     */
    public static ClusterCommand openBasicCommissioningWindow(Integer commissioningTimeout) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (commissioningTimeout != null) {
            map.put("commissioningTimeout", commissioningTimeout);
        }
        return new ClusterCommand("openBasicCommissioningWindow", map);
    }

    /**
     * This command is used by a current Administrator to instruct a Node to revoke any active OpenCommissioningWindow
     * or OpenBasicCommissioningWindow command. This is an idempotent command and the Node shall (for ECM) delete the
     * temporary PAKEPasscodeVerifier and associated data, and stop publishing the DNS-SD record associated with the
     * OpenCommissioningWindow or OpenBasicCommissioningWindow command, see Section 4.3.1, “Commissionable Node
     * Discovery”.
     * If no commissioning window was open at time of receipt, this command shall fail with a cluster specific status
     * code of WindowNotOpen.
     * If the commissioning window was open and the fail-safe was armed when this command is received, the device shall
     * immediately expire the fail-safe and perform the cleanup steps outlined in Section 11.10.7.2.2, “Behavior on
     * expiry of Fail-Safe timer”.
     */
    public static ClusterCommand revokeCommissioning() {
        return new ClusterCommand("revokeCommissioning");
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "windowStatus : " + windowStatus + "\n";
        str += "adminFabricIndex : " + adminFabricIndex + "\n";
        str += "adminVendorId : " + adminVendorId + "\n";
        return str;
    }
}
