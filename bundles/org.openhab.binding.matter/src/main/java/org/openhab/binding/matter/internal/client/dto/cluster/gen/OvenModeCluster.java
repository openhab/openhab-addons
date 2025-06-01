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
 * OvenMode
 *
 * @author Dan Cunningham - Initial contribution
 */
public class OvenModeCluster extends ModeBaseCluster {

    public static final int CLUSTER_ID = 0x0049;
    public static final String CLUSTER_NAME = "OvenMode";
    public static final String CLUSTER_PREFIX = "ovenMode";

    // Structs
    /**
     * The table below lists the changes relative to the Mode Base cluster for the fields of the ModeOptionStruct type.
     * A blank field indicates no change.
     */
    public class ModeOptionStruct {
        public String label; //
        public String mode; //
        public String modeTags; //

        public ModeOptionStruct(String label, String mode, String modeTags) {
            this.label = label;
            this.mode = mode;
            this.modeTags = modeTags;
        }
    }

    // Enums
    public enum ModeTag implements MatterEnum {
        AUTO(0, "Auto"),
        QUICK(1, "Quick"),
        QUIET(2, "Quiet"),
        LOW_NOISE(3, "Low Noise"),
        LOW_ENERGY(4, "Low Energy"),
        VACATION(5, "Vacation"),
        MIN(6, "Min"),
        MAX(7, "Max"),
        NIGHT(8, "Night"),
        DAY(9, "Day"),
        BAKE(16384, "Bake"),
        CONVECTION(16385, "Convection"),
        GRILL(16386, "Grill"),
        ROAST(16387, "Roast"),
        CLEAN(16388, "Clean"),
        CONVECTION_BAKE(16389, "Convection Bake"),
        CONVECTION_ROAST(16390, "Convection Roast"),
        WARMING(16391, "Warming"),
        PROOFING(16392, "Proofing"),
        STEAM(16393, "Steam");

        public final Integer value;
        public final String label;

        private ModeTag(Integer value, String label) {
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

    public OvenModeCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 73, "OvenMode");
    }

    protected OvenModeCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        return str;
    }
}
