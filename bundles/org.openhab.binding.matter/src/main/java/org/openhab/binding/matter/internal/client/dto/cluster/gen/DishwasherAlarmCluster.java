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
import java.util.Map;
import java.util.LinkedHashMap;

import org.eclipse.jdt.annotation.NonNull;

import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;

/**
 * DishwasherAlarm
 *
 * @author Dan Cunningham - Initial contribution
 */
public class DishwasherAlarmCluster extends BaseCluster {

public static final int CLUSTER_ID = 0x005D;
    public static final String CLUSTER_NAME = "DishwasherAlarm";
    public static final String CLUSTER_PREFIX = "dishwasherAlarm";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";

    public Integer clusterRevision; // 65533 ClusterRevision 



    // Bitmaps
    public static class AlarmBitmap {
        public boolean inflowError;
        public boolean drainError;
        public boolean doorError;
        public boolean tempTooLow;
        public boolean tempTooHigh;
        public boolean waterLevelError;
        public AlarmBitmap(boolean inflowError, boolean drainError, boolean doorError, boolean tempTooLow, boolean tempTooHigh, boolean waterLevelError){
            this.inflowError = inflowError;
            this.drainError = drainError;
            this.doorError = doorError;
            this.tempTooLow = tempTooLow;
            this.tempTooHigh = tempTooHigh;
            this.waterLevelError = waterLevelError;
        }
    }

    public DishwasherAlarmCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 93, "DishwasherAlarm");
    }

    
    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        return str;
    }
}
