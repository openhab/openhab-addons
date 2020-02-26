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

import java.time.Duration;
import java.util.Objects;

import javax.measure.Quantity;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hive.internal.client.SettableFeatureAttribute;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class AutoBoostFeature implements Feature {
    private final @Nullable SettableFeatureAttribute<Duration> autoBoostDuration;
    private final @Nullable SettableFeatureAttribute<Quantity<Temperature>> autoBoostTargetHeatTemperature;

    private AutoBoostFeature(
            final @Nullable SettableFeatureAttribute<Duration> autoBoostDuration,
            final @Nullable SettableFeatureAttribute<Quantity<Temperature>> autoBoostTargetHeatTemperature
    ) {
        this.autoBoostDuration = autoBoostDuration;
        this.autoBoostTargetHeatTemperature = autoBoostTargetHeatTemperature;
    }

    public @Nullable SettableFeatureAttribute<Duration> getAutoBoostDuration() {
        return this.autoBoostDuration;
    }

    public AutoBoostFeature withTargetAutoBoostDuration(final Duration targetAutoBoostDuration) {
        Objects.requireNonNull(targetAutoBoostDuration);

        final @Nullable SettableFeatureAttribute<Duration> autoBoostDuration = this.autoBoostDuration;
        if (autoBoostDuration == null) {
            throw new IllegalStateException(FeatureBuilderUtil.getCannotSetTargetMessage("autoBoostDuration"));
        }

        return AutoBoostFeature.builder()
                .from(this)
                .autoBoostDuration(
                        autoBoostDuration.withTargetValue(targetAutoBoostDuration)
                )
                .build();
    }

    public @Nullable SettableFeatureAttribute<Quantity<Temperature>> getAutoBoostTargetHeatTemperature() {
        return this.autoBoostTargetHeatTemperature;
    }

    public AutoBoostFeature withTargetAutoBoostTargetHeatTemperature(final Quantity<Temperature> targetAutoBoostTargetHeatTemperature) {
        Objects.requireNonNull(targetAutoBoostTargetHeatTemperature);

        final @Nullable SettableFeatureAttribute<Quantity<Temperature>> autoBoostTargetHeatTemperature = this.autoBoostTargetHeatTemperature;
        if (autoBoostTargetHeatTemperature == null) {
            throw new IllegalStateException(FeatureBuilderUtil.getCannotSetTargetMessage("autoBoostTargetHeatTemperature"));
        }

        return AutoBoostFeature.builder()
                .from(this)
                .autoBoostTargetHeatTemperature(
                        autoBoostTargetHeatTemperature.withTargetValue(targetAutoBoostTargetHeatTemperature)
                )
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private @Nullable SettableFeatureAttribute<Duration> autoBoostDuration;
        private @Nullable SettableFeatureAttribute<Quantity<Temperature>> autoBoostTargetHeatTemperature;

        public Builder from(final AutoBoostFeature autoBoostFeature) {
            Objects.requireNonNull(autoBoostFeature);

            this.autoBoostDuration(autoBoostFeature.getAutoBoostDuration());
            this.autoBoostTargetHeatTemperature(autoBoostFeature.getAutoBoostTargetHeatTemperature());

            return this;
        }

        public Builder autoBoostDuration(final @Nullable SettableFeatureAttribute<Duration> autoBoostDuration) {
            this.autoBoostDuration = autoBoostDuration;

            return this;
        }

        public Builder autoBoostTargetHeatTemperature(final @Nullable SettableFeatureAttribute<Quantity<Temperature>> autoBoostTargetHeatTemperature) {
            this.autoBoostTargetHeatTemperature = autoBoostTargetHeatTemperature;

            return this;
        }

        public AutoBoostFeature build() {
            return new AutoBoostFeature(
                    this.autoBoostDuration,
                    this.autoBoostTargetHeatTemperature
            );
        }
    }
}
