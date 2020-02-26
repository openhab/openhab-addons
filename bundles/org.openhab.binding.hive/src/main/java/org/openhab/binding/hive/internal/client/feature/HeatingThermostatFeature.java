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
import org.openhab.binding.hive.internal.client.*;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class HeatingThermostatFeature implements Feature {
    private final @Nullable SettableFeatureAttribute<HeatingThermostatOperatingMode> operatingMode;
    private final @Nullable FeatureAttribute<HeatingThermostatOperatingState> operatingState;
    private final @Nullable SettableFeatureAttribute<Quantity<Temperature>> targetHeatTemperature;
    private final @Nullable SettableFeatureAttribute<OverrideMode> temporaryOperatingModeOverride;

    private HeatingThermostatFeature(
            final @Nullable SettableFeatureAttribute<HeatingThermostatOperatingMode> operatingMode,
            final @Nullable FeatureAttribute<HeatingThermostatOperatingState> operatingState,
            final @Nullable SettableFeatureAttribute<Quantity<Temperature>> targetHeatTemperature,
            final @Nullable SettableFeatureAttribute<OverrideMode> temporaryOperatingModeOverride
    ) {
        this.operatingMode = operatingMode;
        this.operatingState = operatingState;
        this.targetHeatTemperature = targetHeatTemperature;
        this.temporaryOperatingModeOverride = temporaryOperatingModeOverride;
    }

    public @Nullable SettableFeatureAttribute<HeatingThermostatOperatingMode> getOperatingMode() {
        return this.operatingMode;
    }

    public HeatingThermostatFeature withTargetOperatingMode(final HeatingThermostatOperatingMode targetOperatingMode) {
        Objects.requireNonNull(targetOperatingMode);

        final @Nullable SettableFeatureAttribute<HeatingThermostatOperatingMode> operatingMode = this.operatingMode;
        if (operatingMode == null) {
            throw new IllegalStateException(FeatureBuilderUtil.getCannotSetTargetMessage("operatingMode"));
        }

        return HeatingThermostatFeature.builder()
                .from(this)
                .operatingMode(
                        operatingMode.withTargetValue(targetOperatingMode)
                )
                .build();
    }

    public @Nullable FeatureAttribute<HeatingThermostatOperatingState> getOperatingState() {
        return this.operatingState;
    }

    public @Nullable SettableFeatureAttribute<Quantity<Temperature>> getTargetHeatTemperature() {
        return this.targetHeatTemperature;
    }

    public HeatingThermostatFeature withTargetTargetHeatTemperature(final Quantity<Temperature> targetTargetHeatTemperature) {
        Objects.requireNonNull(targetTargetHeatTemperature);

        final @Nullable SettableFeatureAttribute<Quantity<Temperature>> targetHeatTemperature = this.targetHeatTemperature;
        if (targetHeatTemperature == null) {
            throw new IllegalStateException(FeatureBuilderUtil.getCannotSetTargetMessage("targetHeatTemperature"));
        }

        return HeatingThermostatFeature.builder()
                .from(this)
                .targetHeatTemperature(
                        targetHeatTemperature.withTargetValue(targetTargetHeatTemperature)
                )
                .build();
    }

    public @Nullable SettableFeatureAttribute<OverrideMode> getTemporaryOperatingModeOverride() {
        return this.temporaryOperatingModeOverride;
    }

    public HeatingThermostatFeature withTargetTemporaryOperatingModeOverride(final OverrideMode targetOverrideMode) {
        Objects.requireNonNull(targetOverrideMode);

        final @Nullable SettableFeatureAttribute<OverrideMode> temporaryOperatingModeOverride = this.temporaryOperatingModeOverride;
        if (temporaryOperatingModeOverride == null) {
            throw new IllegalStateException(FeatureBuilderUtil.getCannotSetTargetMessage("temporaryOperatingModeOverride"));
        }

        return HeatingThermostatFeature.builder()
                .from(this)
                .temporaryOperatingModeOverride(
                        temporaryOperatingModeOverride.withTargetValue(targetOverrideMode)
                )
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private @Nullable SettableFeatureAttribute<HeatingThermostatOperatingMode> operatingMode;
        private @Nullable FeatureAttribute<HeatingThermostatOperatingState> operatingState;
        private @Nullable SettableFeatureAttribute<Quantity<Temperature>> targetHeatTemperature;
        private @Nullable SettableFeatureAttribute<OverrideMode> temporaryOperatingModeOverride;

        public Builder from(final HeatingThermostatFeature heatingThermostatFeature) {
            Objects.requireNonNull(heatingThermostatFeature);

            return this.operatingMode(heatingThermostatFeature.getOperatingMode())
                    .operatingState(heatingThermostatFeature.getOperatingState())
                    .targetHeatTemperature(heatingThermostatFeature.getTargetHeatTemperature())
                    .temporaryOperatingModeOverride(heatingThermostatFeature.getTemporaryOperatingModeOverride());
        }

        public Builder operatingMode(final @Nullable SettableFeatureAttribute<HeatingThermostatOperatingMode> operatingMode) {
            this.operatingMode = operatingMode;

            return this;
        }

        public Builder operatingState(final @Nullable FeatureAttribute<HeatingThermostatOperatingState> operatingState) {
            this.operatingState = operatingState;

            return this;
        }

        public Builder targetHeatTemperature(final @Nullable SettableFeatureAttribute<Quantity<Temperature>> targetHeatTemperature) {
            this.targetHeatTemperature = targetHeatTemperature;

            return this;
        }

        public Builder temporaryOperatingModeOverride(final @Nullable SettableFeatureAttribute<OverrideMode> temporaryOperatingModeOverride) {
            this.temporaryOperatingModeOverride = temporaryOperatingModeOverride;

            return this;
        }

        public HeatingThermostatFeature build() {
            return new HeatingThermostatFeature(
                    this.operatingMode,
                    this.operatingState,
                    this.targetHeatTemperature,
                    this.temporaryOperatingModeOverride
            );
        }
    }
}
