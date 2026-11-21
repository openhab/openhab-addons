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
package org.openhab.binding.atv.internal.client.core;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.capability.Features;
import org.openhab.binding.atv.internal.client.dto.FeatureInfo;
import org.openhab.binding.atv.internal.client.dto.FeatureName;
import org.openhab.binding.atv.internal.client.dto.FeatureState;
import org.openhab.binding.atv.internal.client.dto.Protocol;

/**
 * Relay implementation for supported feature functionality.
 *
 * <p>
 * This class holds a map from feature name to the instance handling that feature; when several protocols
 * announce the same feature, the protocol with the highest
 * priority (per {@link AppleTVRelay#DEFAULT_PRIORITIES}) keeps the mapping. {@link FeatureName#PushUpdates} is
 * special-cased: multiple protocols can register a push updater implementation but only one of them will ever be
 * used, so the feature is available as soon as at least one push updater is registered. Features without a mapping
 * report {@link FeatureState#Unsupported}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class FeaturesRelay extends BaseRelay<Features> implements Features {

    private record MappedFeature(Protocol protocol, Features instance) {
    }

    private final PushUpdaterRelay pushUpdaterRelay;
    private final Map<FeatureName, MappedFeature> featureMap = new EnumMap<>(FeatureName.class);

    /**
     * Creates a new relay features instance.
     *
     * @param guard device guard blocking calls after close
     * @param pushUpdaterRelay push updater relay, used for the {@link FeatureName#PushUpdates} special case
     */
    public FeaturesRelay(Guard guard, PushUpdaterRelay pushUpdaterRelay) {
        // Features implementations are looked up per feature name (not capability-relayed), so instances are
        // not required to implement CapabilitySource
        super(new Relayer<>(Features.class, AppleTVRelay.DEFAULT_PRIORITIES, false), guard);
        this.pushUpdaterRelay = Objects.requireNonNull(pushUpdaterRelay, "pushUpdaterRelay");
    }

    /**
     * Adds a mapping from a protocol to the features handled by that protocol. A feature is added to the map when
     * missing, or replaced when this protocol has higher priority than the previous mapping.
     *
     * @param protocol protocol handling the features
     * @param features features handled by the protocol
     */
    public synchronized void addMapping(Protocol protocol, Set<FeatureName> features) {
        relayer.get(protocol).ifPresent(instance -> {
            for (FeatureName feature : features) {
                MappedFeature previous = featureMap.get(feature);
                if (previous == null || hasHigherPriority(protocol, previous.protocol())) {
                    featureMap.put(feature, new MappedFeature(protocol, instance));
                }
            }
        });
    }

    @Override
    public synchronized FeatureInfo getFeature(FeatureName featureName) {
        guard.requireNotBlocked("getFeature");
        if (featureName == FeatureName.PushUpdates) {
            // Multiple protocols can register a push updater implementation, but only one of them will ever be
            // used (i.e. relaying is not done on method level). So if at least one push updater is available,
            // then we can return "Available" here.
            if (pushUpdaterRelay.count() >= 1) {
                return new FeatureInfo(FeatureState.Available);
            }
        }
        MappedFeature mapped = featureMap.get(featureName);
        if (mapped != null) {
            return mapped.instance().getFeature(featureName);
        }
        return new FeatureInfo(FeatureState.Unsupported);
    }

    private static boolean hasHigherPriority(Protocol first, Protocol second) {
        return AppleTVRelay.DEFAULT_PRIORITIES.indexOf(first) < AppleTVRelay.DEFAULT_PRIORITIES.indexOf(second);
    }
}
