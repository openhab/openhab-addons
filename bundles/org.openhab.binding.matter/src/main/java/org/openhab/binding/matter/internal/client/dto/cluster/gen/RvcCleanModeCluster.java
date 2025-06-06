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
 * RvcCleanMode
 *
 * @author Dan Cunningham - Initial contribution
 */
public class RvcCleanModeCluster extends ModeBaseCluster {

    public static final int CLUSTER_ID = 0x0055;
    public static final String CLUSTER_NAME = "RvcCleanMode";
    public static final String CLUSTER_PREFIX = "rvcCleanMode";

    // Enums
    public enum ModeChangeStatus implements MatterEnum {
        CLEANING_IN_PROGRESS(64, "Cleaning In Progress");

        public final Integer value;
        public final String label;

        private ModeChangeStatus(Integer value, String label) {
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
        DEEP_CLEAN(16384, "Deep Clean"),
        VACUUM(16385, "Vacuum"),
        MOP(16386, "Mop");

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

    public RvcCleanModeCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 85, "RvcCleanMode");
    }

    protected RvcCleanModeCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        return str;
    }
}
