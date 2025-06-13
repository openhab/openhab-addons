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
 * UnitLocalization
 *
 * @author Dan Cunningham - Initial contribution
 */
public class UnitLocalizationCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x002D;
    public static final String CLUSTER_NAME = "UnitLocalization";
    public static final String CLUSTER_PREFIX = "unitLocalization";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_TEMPERATURE_UNIT = "temperatureUnit";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * The TemperatureUnit attribute shall indicate the unit for the Node to use only when conveying temperature in
     * communication to the user. If provided, this value shall take priority over any unit implied through the
     * ActiveLocale Attribute.
     */
    public TempUnitEnum temperatureUnit; // 0 TempUnitEnum RW VM

    // Enums
    public enum TempUnitEnum implements MatterEnum {
        FAHRENHEIT(0, "Fahrenheit"),
        CELSIUS(1, "Celsius"),
        KELVIN(2, "Kelvin");

        public final Integer value;
        public final String label;

        private TempUnitEnum(Integer value, String label) {
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
         * The Node can be configured to use different units of temperature when conveying values to a user.
         */
        public boolean temperatureUnit;

        public FeatureMap(boolean temperatureUnit) {
            this.temperatureUnit = temperatureUnit;
        }
    }

    public UnitLocalizationCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 45, "UnitLocalization");
    }

    protected UnitLocalizationCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "temperatureUnit : " + temperatureUnit + "\n";
        return str;
    }
}
