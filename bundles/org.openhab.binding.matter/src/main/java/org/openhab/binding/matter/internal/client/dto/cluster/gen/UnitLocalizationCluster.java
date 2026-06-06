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
 * UnitLocalization
 *
 * @author Dan Cunningham - Initial contribution
 */
public class UnitLocalizationCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x002D;
    public static final String CLUSTER_NAME = "UnitLocalization";
    public static final String CLUSTER_PREFIX = "unitLocalization";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_TEMPERATURE_UNIT = "temperatureUnit";
    public static final String ATTRIBUTE_SUPPORTED_TEMPERATURE_UNITS = "supportedTemperatureUnits";

    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * Indicates the unit for the Node to use only when conveying temperature in communication to the user, for example
     * such as via a user interface on the device. If provided, this value shall take priority over any unit implied
     * through the ActiveLocale Attribute.
     * An attempt to write to this attribute with a value not included in the SupportedTemperatureUnits attribute list
     * shall result in a CONSTRAINT_ERROR.
     */
    public TempUnitEnum temperatureUnit; // 0 TempUnitEnum RW VM
    /**
     * Indicates a list of units supported by the Node to be used when writing the TemperatureUnit attribute of this
     * cluster. Each entry in the list shall be unique.
     */
    public List<TempUnitEnum> supportedTemperatureUnits; // 1 list R V

    // Enums
    public enum TempUnitEnum implements MatterEnum {
        FAHRENHEIT(0, "Fahrenheit"),
        CELSIUS(1, "Celsius"),
        KELVIN(2, "Kelvin");

        private final Integer value;
        private final String label;

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
        str += "featureMap : " + featureMap + "\n";
        str += "temperatureUnit : " + temperatureUnit + "\n";
        str += "supportedTemperatureUnits : " + supportedTemperatureUnits + "\n";
        return str;
    }
}
