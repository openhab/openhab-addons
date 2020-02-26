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
public final class TransientModeHeatingActionsFeature implements Feature {
    private final @Nullable SettableFeatureAttribute<Quantity<Temperature>> boostTargetTemperature;

    private TransientModeHeatingActionsFeature(
            final @Nullable SettableFeatureAttribute<Quantity<Temperature>> boostTargetTemperature
    ) {
        this.boostTargetTemperature = boostTargetTemperature;
    }

    public @Nullable SettableFeatureAttribute<Quantity<Temperature>> getBoostTargetTemperature() {
        return this.boostTargetTemperature;
    }

    public TransientModeHeatingActionsFeature withTargetBoostTargetTemperature(final Quantity<Temperature> targetBoostTargetTemperature) {
        Objects.requireNonNull(targetBoostTargetTemperature);

        final @Nullable SettableFeatureAttribute<Quantity<Temperature>> boostTargetTemperature = this.boostTargetTemperature;
        if (boostTargetTemperature == null) {
            throw new IllegalStateException(FeatureBuilderUtil.getCannotSetTargetMessage("boostTargetTemperature"));
        }

        return TransientModeHeatingActionsFeature.builder()
                .from(this)
                .boostTargetTemperature(
                        boostTargetTemperature.withTargetValue(targetBoostTargetTemperature)
                )
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private @Nullable SettableFeatureAttribute<Quantity<Temperature>> boostTargetTemperature;

        public Builder from(final TransientModeHeatingActionsFeature transientModeHeatingActionsFeature) {
            Objects.requireNonNull(transientModeHeatingActionsFeature);

            return this.boostTargetTemperature(transientModeHeatingActionsFeature.getBoostTargetTemperature());
        }

        public Builder boostTargetTemperature(final @Nullable SettableFeatureAttribute<Quantity<Temperature>> boostTargetTemperature) {
            this.boostTargetTemperature = boostTargetTemperature;

            return this;
        }

        public TransientModeHeatingActionsFeature build() {
            return new TransientModeHeatingActionsFeature(
                    this.boostTargetTemperature
            );
        }
    }
}
