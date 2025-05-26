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
 * RvcRunMode
 *
 * @author Dan Cunningham - Initial contribution
 */
public class RvcRunModeCluster extends ModeBaseCluster {

    public static final int CLUSTER_ID = 0x0054;
    public static final String CLUSTER_NAME = "RvcRunMode";
    public static final String CLUSTER_PREFIX = "rvcRunMode";

    // Enums
    public enum ModeChangeStatus implements MatterEnum {
        STUCK(65, "Stuck"),
        DUST_BIN_MISSING(66, "Dust Bin Missing"),
        DUST_BIN_FULL(67, "Dust Bin Full"),
        WATER_TANK_EMPTY(68, "Water Tank Empty"),
        WATER_TANK_MISSING(69, "Water Tank Missing"),
        WATER_TANK_LID_OPEN(70, "Water Tank Lid Open"),
        MOP_CLEANING_PAD_MISSING(71, "Mop Cleaning Pad Missing"),
        BATTERY_LOW(72, "Battery Low");

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
        IDLE(16384, "Idle"),
        CLEANING(16385, "Cleaning"),
        MAPPING(16386, "Mapping");

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

    public RvcRunModeCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 84, "RvcRunMode");
    }

    protected RvcRunModeCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        return str;
    }
}
