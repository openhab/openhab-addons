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
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

/**
 * UserLabel
 *
 * @author Dan Cunningham - Initial contribution
 */
public class UserLabelCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0041;
    public static final String CLUSTER_NAME = "UserLabel";
    public static final String CLUSTER_PREFIX = "userLabel";
    public static final String ATTRIBUTE_LABEL_LIST = "labelList";

    /**
     * This is a list of string tuples. Each entry is a LabelStruct.
     */
    public List<LabelStruct> labelList; // 0 list

    // Structs
    /**
     * This is a string tuple with strings that are user defined.
     */
    public static class LabelStruct {
        /**
         * The Label or Value semantic is not defined here.
         * Label examples: "room", "zone", "group", "direction".
         */
        public String label; // string
        /**
         * The Label or Value semantic is not defined here. The Value is a discriminator for a Label that may have
         * multiple instances.
         * Label:Value examples: "room":"bedroom 2", "orientation":"North", "floor":"2", "direction":"up"
         */
        public String value; // string

        public LabelStruct(String label, String value) {
            this.label = label;
            this.value = value;
        }
    }

    public UserLabelCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 65, "UserLabel");
    }

    protected UserLabelCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "labelList : " + labelList + "\n";
        return str;
    }
}
