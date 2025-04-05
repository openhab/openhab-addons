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
 * FormaldehydeConcentrationMeasurement
 *
 * @author Dan Cunningham - Initial contribution
 */
public class FormaldehydeConcentrationMeasurementCluster extends BaseCluster {

public static final int CLUSTER_ID = 0x042B;
    public static final String CLUSTER_NAME = "FormaldehydeConcentrationMeasurement";
    public static final String CLUSTER_PREFIX = "formaldehydeConcentrationMeasurement";



    //Enums
    /**
    * Where mentioned, Billion refers to 10, Trillion refers to 1012 (short scale).
    */
    public enum MeasurementUnitEnum implements MatterEnum {
        PPM(0, "Ppm"),
        PPB(1, "Ppb"),
        PPT(2, "Ppt"),
        MGM3(3, "Mgm3"),
        UGM3(4, "Ugm3"),
        NGM3(5, "Ngm3"),
        PM3(6, "Pm3"),
        BQM3(7, "Bqm3");
        public final Integer value;
        public final String label;
        private MeasurementUnitEnum(Integer value, String label){
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
    public enum MeasurementMediumEnum implements MatterEnum {
        AIR(0, "Air"),
        WATER(1, "Water"),
        SOIL(2, "Soil");
        public final Integer value;
        public final String label;
        private MeasurementMediumEnum(Integer value, String label){
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
    public enum LevelValueEnum implements MatterEnum {
        UNKNOWN(0, "Unknown"),
        LOW(1, "Low"),
        MEDIUM(2, "Medium"),
        HIGH(3, "High"),
        CRITICAL(4, "Critical");
        public final Integer value;
        public final String label;
        private LevelValueEnum(Integer value, String label){
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


    public FormaldehydeConcentrationMeasurementCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 1067, "FormaldehydeConcentrationMeasurement");
    }

    
    @Override
    public @NonNull String toString() {
        String str = "";
        return str;
    }
}
