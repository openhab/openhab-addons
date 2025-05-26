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
 * LaundryWasherMode
 *
 * @author Dan Cunningham - Initial contribution
 */
public class LaundryWasherModeCluster extends ModeBaseCluster {

    public static final int CLUSTER_ID = 0x0051;
    public static final String CLUSTER_NAME = "LaundryWasherMode";
    public static final String CLUSTER_PREFIX = "laundryWasherMode";

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
        NORMAL(16384, "Normal"),
        DELICATE(16385, "Delicate"),
        HEAVY(16386, "Heavy"),
        WHITES(16387, "Whites");

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

    public LaundryWasherModeCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 81, "LaundryWasherMode");
    }

    protected LaundryWasherModeCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        return str;
    }
}
