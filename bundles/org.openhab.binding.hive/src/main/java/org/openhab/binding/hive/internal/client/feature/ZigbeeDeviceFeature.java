/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hive.internal.client.feature;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hive.internal.client.Eui64;
import org.openhab.binding.hive.internal.client.FeatureAttribute;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class ZigbeeDeviceFeature implements Feature {
    private final @Nullable FeatureAttribute<Eui64> eui64;
    private final @Nullable FeatureAttribute<Integer> averageLQI;
    private final @Nullable FeatureAttribute<Integer> lastKnownLQI;
    private final @Nullable FeatureAttribute<Integer> averageRSSI;
    private final @Nullable FeatureAttribute<Integer> lastKnownRSSI;

    private ZigbeeDeviceFeature(
            final @Nullable FeatureAttribute<Eui64> eui64,
            final @Nullable FeatureAttribute<Integer> averageLQI,
            final @Nullable FeatureAttribute<Integer> lastKnownLQI,
            final @Nullable FeatureAttribute<Integer> averageRSSI,
            final @Nullable FeatureAttribute<Integer> lastKnownRSSI
    ) {
        this.eui64 = eui64;
        this.averageLQI = averageLQI;
        this.lastKnownLQI = lastKnownLQI;
        this.averageRSSI = averageRSSI;
        this.lastKnownRSSI = lastKnownRSSI;
    }

    public @Nullable FeatureAttribute<Eui64> getEui64() {
        return this.eui64;
    }

    public @Nullable FeatureAttribute<Integer> getAverageLQI() {
        return this.averageLQI;
    }

    public @Nullable FeatureAttribute<Integer> getLastKnownLQI() {
        return this.lastKnownLQI;
    }

    public @Nullable FeatureAttribute<Integer> getAverageRSSI() {
        return this.averageRSSI;
    }

    public @Nullable FeatureAttribute<Integer> getLastKnownRSSI() {
        return this.lastKnownRSSI;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static final class Builder {
        private @Nullable FeatureAttribute<Eui64> eui64;
        private @Nullable FeatureAttribute<Integer> averageLQI;
        private @Nullable FeatureAttribute<Integer> lastKnownLQI;
        private @Nullable FeatureAttribute<Integer> averageRSSI;
        private @Nullable FeatureAttribute<Integer> lastKnownRSSI;

        public Builder from(final ZigbeeDeviceFeature zigbeeDeviceFeature) {
            Objects.requireNonNull(zigbeeDeviceFeature);

            return this.eui64(zigbeeDeviceFeature.getEui64())
                    .averageLQI(zigbeeDeviceFeature.getAverageLQI())
                    .lastKnownLQI(zigbeeDeviceFeature.getLastKnownLQI())
                    .averageRSSI(zigbeeDeviceFeature.getAverageRSSI())
                    .lastKnownRSSI(zigbeeDeviceFeature.getLastKnownRSSI());
        }

        public Builder eui64(final @Nullable FeatureAttribute<Eui64> eui64) {
            this.eui64 = eui64;

            return this;
        }

        public Builder averageLQI(final @Nullable FeatureAttribute<Integer> averageLQI) {
            this.averageLQI = averageLQI;

            return this;
        }

        public Builder lastKnownLQI(final @Nullable FeatureAttribute<Integer> lastKnownLQI) {
            this.lastKnownLQI = lastKnownLQI;

            return this;
        }

        public Builder averageRSSI(final @Nullable FeatureAttribute<Integer> averageRSSI) {
            this.averageRSSI = averageRSSI;

            return this;
        }

        public Builder lastKnownRSSI(final @Nullable FeatureAttribute<Integer> lastKnownRSSI) {
            this.lastKnownRSSI = lastKnownRSSI;

            return this;
        }
        
        public ZigbeeDeviceFeature build() {
            return new ZigbeeDeviceFeature(
                    this.eui64,
                    this.averageLQI,
                    this.lastKnownLQI,
                    this.averageRSSI,
                    this.lastKnownRSSI
            );
        }
    }
}
