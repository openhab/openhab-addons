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
package org.openhab.binding.amazonechocontrol.internal.dto.smarthome;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link JsonSmartHomeCapability} encapsulates a smarthome capability API response
 *
 * @author Lukas Knoeller - Initial contribution
 */
@NonNullByDefault
public class JsonSmartHomeCapability {
    public @Nullable String capabilityType;
    public @Nullable String type;
    public @Nullable String version;
    public @Nullable String interfaceName;
    public @Nullable Properties properties;
    public @Nullable Configuration configuration;
    public @Nullable Resources resources;
    public @Nullable String instance;

    @Override
    public String toString() {
        return "SmartHomeCapability{capabilityType='" + capabilityType + "', type='" + type + "', version='" + version
                + "', interfaceName='" + interfaceName + "', properties=" + properties + "', configuration="
                + configuration + "', resources=" + resources + "}";
    }

    public static class Properties {
        public @Nullable List<Properties.Property> supported;
        public boolean proactivelyReported = false;
        public boolean retrievable = false;
        public boolean readOnly = true;

        @Override
        public String toString() {
            return "Properties{supported=" + supported + ", proactivelyReported=" + proactivelyReported
                    + ", retrievable=" + retrievable + ", readOnly=" + readOnly + "}";
        }

        public static class Property {
            public @Nullable String name;

            @Override
            public String toString() {
                return "Property{name='" + name + "'}";
            }
        }
    }

    public static class Configuration {
        public @Nullable Range supportedRange;
        public @Nullable String unitOfMeasure;
        public @Nullable List<Object> presets;

        @Override
        public String toString() {
            return "Configuration{supportedRange=" + supportedRange + ", unitOfMeasure='" + unitOfMeasure + "'"
                    + ", presets=" + presets + "}";
        }

        public static class Range {
            public @Nullable Double minimumValue;
            public @Nullable Double maximumValue;
            public @Nullable Double precision;

            @Override
            public String toString() {
                return "Range{minimumValue=" + minimumValue + ", maximumValue=" + maximumValue + ", precision="
                        + precision + "}";
            }
        }
    }

    public static class Resources {
        public @Nullable List<Resources.Names> friendlyNames;

        @Override
        public String toString() {
            return "Resources{friendlyNames=" + friendlyNames + "}";
        }

        public static class Names {
            public @Nullable Value value;
            @SerializedName("@type")
            public @Nullable String type;

            @Override
            public String toString() {
                return "Names{value=" + value + ", type='" + type + "'}";
            }

            public static class Value {
                public @Nullable String assetId;
                public @Nullable String text;
                public @Nullable String locale;

                @Override
                public String toString() {
                    return "Value{assetId='" + assetId + "'" + ", text='" + text + "'" + ", locale='" + locale + "'}";
                }
            }
        }
    }
}
