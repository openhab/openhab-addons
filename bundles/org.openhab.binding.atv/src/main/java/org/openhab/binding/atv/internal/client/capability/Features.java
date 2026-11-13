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
package org.openhab.binding.atv.internal.client.capability;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.dto.FeatureInfo;
import org.openhab.binding.atv.internal.client.dto.FeatureName;
import org.openhab.binding.atv.internal.client.dto.FeatureState;

/**
 * API for querying supported features and their state.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface Features {

    /**
     * Returns the current state of a feature.
     *
     * @param featureName feature to query
     * @return feature state and options
     */
    FeatureInfo getFeature(FeatureName featureName);

    /**
     * Returns the state of all features.
     *
     * @param includeUnsupported if {@code true}, also include unsupported features
     * @return map from feature name to feature info
     */
    default Map<FeatureName, FeatureInfo> allFeatures(boolean includeUnsupported) {
        Map<FeatureName, FeatureInfo> features = new LinkedHashMap<>();
        for (FeatureName name : FeatureName.values()) {
            FeatureInfo info = getFeature(name);
            if (info.state() != FeatureState.Unsupported || includeUnsupported) {
                features.put(name, info);
            }
        }
        return features;
    }

    /**
     * Returns the state of all supported features.
     *
     * @return map from feature name to feature info
     */
    default Map<FeatureName, FeatureInfo> allFeatures() {
        return allFeatures(false);
    }

    /**
     * Returns if all given features are in one of the specified states.
     *
     * @param states accepted states (a feature only has to be in one of them)
     * @param featureNames features to check
     * @return {@code true} if all features are in one of the given states
     */
    default boolean inState(List<FeatureState> states, FeatureName... featureNames) {
        for (FeatureName name : featureNames) {
            if (!states.contains(getFeature(name).state())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns if all given features are in a specific state.
     *
     * @param state expected state
     * @param featureNames features to check
     * @return {@code true} if all features are in the given state
     */
    default boolean inState(FeatureState state, FeatureName... featureNames) {
        return inState(List.of(state), featureNames);
    }
}
