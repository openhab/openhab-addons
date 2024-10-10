/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The DeviceType class holds device type definitions that are read from
 * an xml file.
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 */
@NonNullByDefault
public class LegacyDeviceType {
    private String productKey;
    private String model = "";
    private String description = "";
    private Map<String, String> features = new HashMap<>();
    private Map<String, FeatureGroup> featureGroups = new HashMap<>();

    /**
     * Constructor
     *
     * @param productKey the product key for this device type
     */
    public LegacyDeviceType(String productKey) {
        this.productKey = productKey;
    }

    /**
     * Get supported features
     *
     * @return all features that this device type supports
     */
    public Map<String, String> getFeatures() {
        return features;
    }

    /**
     * Get all feature groups
     *
     * @return all feature groups of this device type
     */
    public Map<String, FeatureGroup> getFeatureGroups() {
        return featureGroups;
    }

    /**
     * Sets the descriptive model string
     *
     * @param model descriptive model string
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * Sets free text description
     *
     * @param description free text description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Adds feature to this device type
     *
     * @param key the key (e.g. "switch") under which this feature can be referenced in the item binding config
     * @param featureName the name (e.g. "GenericSwitch") under which the feature has been defined
     * @return false if feature was already there
     */
    public boolean addFeature(String key, String featureName) {
        if (features.containsKey(key)) {
            return false;
        }
        features.put(key, featureName);
        return true;
    }

    /**
     * Adds feature group to device type
     *
     * @param key name of the feature group, which acts as key for lookup later
     * @param featureGroup feature group to add
     * @return true if add succeeded, false if group was already there
     */
    public boolean addFeatureGroup(String key, FeatureGroup featureGroup) {
        if (features.containsKey(key)) {
            return false;
        }
        featureGroups.put(key, featureGroup);
        return true;
    }

    @Override
    public String toString() {
        String s = "pk:" + productKey + "|model:" + model + "|desc:" + description + "|features";
        for (Entry<String, String> entry : features.entrySet()) {
            s += ":" + entry.getKey() + "=" + entry.getValue();
        }
        s += "|groups";
        for (Entry<String, FeatureGroup> entry : featureGroups.entrySet()) {
            s += ":" + entry.getKey() + "=" + entry.getValue();
        }
        return s;
    }

    /**
     * Class that reflects feature group association
     *
     * @author Bernd Pfrommer - Initial contribution
     */
    public static class FeatureGroup {
        private String name;
        private String type;
        private ArrayList<String> fgFeatures = new ArrayList<>();

        FeatureGroup(String name, String type) {
            this.name = name;
            this.type = type;
        }

        public void addFeature(String f) {
            fgFeatures.add(f);
        }

        public ArrayList<String> getFeatures() {
            return fgFeatures;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            String s = "";
            for (String feature : fgFeatures) {
                s += feature + ",";
            }
            return s.replaceAll(",$", "");
        }
    }
}
