/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.insteon.internal.device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The DeviceType class holds device type definitions that are read from
 * an xml file.
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Improvements for openHAB 3 insteon binding
 */
@NonNullByDefault
public class DeviceType {
    private String name;
    private Map<String, Boolean> flags = new HashMap<>();
    private Map<String, FeatureEntry> features = new LinkedHashMap<>();

    /**
     * Constructor
     *
     * @param name the name for this device type
     * @param flags the flags for this device type
     * @param features the features for this device type
     */
    public DeviceType(String name, Map<String, Boolean> flags, Map<String, FeatureEntry> features) {
        this.name = name;
        this.flags = flags;
        this.features = features;
    }

    /**
     * Returns name
     *
     * @return the name for this device type
     */
    public String getName() {
        return name;
    }

    /**
     * Returns flags
     *
     * @return all flags for this device type
     */
    public Map<String, Boolean> getFlags() {
        return flags;
    }

    /**
     * Returns supported features
     *
     * @return all features that this device type supports
     */
    public List<FeatureEntry> getFeatures() {
        return features.values().stream().collect(Collectors.toList());
    }

    /**
     * Returns supported feature groups
     *
     * @return all feature groups that this device type supports
     */
    public List<FeatureEntry> getFeatureGroups() {
        return features.values().stream().filter(FeatureEntry::hasConnectedFeatures).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        String s = "name:" + name;
        if (!features.isEmpty()) {
            s += "|features:" + features.values().stream().map(FeatureEntry::toString).collect(Collectors.joining(","));
        }
        if (!flags.isEmpty()) {
            s += "|flags:" + flags.entrySet().stream().map(Entry::toString).collect(Collectors.joining(","));
        }
        return s;
    }

    /**
     * Class that reflects a feature entry
     */
    public static class FeatureEntry {
        private String name;
        private String type;
        private Map<String, String> parameters;
        private List<String> connectedFeatures = new ArrayList<>();

        public FeatureEntry(String name, String type, Map<String, String> parameters) {
            this.name = name;
            this.type = type;
            this.parameters = parameters;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public Map<String, String> getParameters() {
            return parameters;
        }

        public List<String> getConnectedFeatures() {
            return connectedFeatures;
        }

        public boolean hasConnectedFeatures() {
            return !connectedFeatures.isEmpty();
        }

        public void addConnectedFeature(String name) {
            connectedFeatures.add(name);
        }

        @Override
        public String toString() {
            String s = name + "->" + type;
            if (!connectedFeatures.isEmpty()) {
                s += connectedFeatures;
            }
            return s;
        }
    }
}
