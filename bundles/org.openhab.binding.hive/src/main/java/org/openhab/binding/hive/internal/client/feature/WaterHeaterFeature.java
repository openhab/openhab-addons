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
import org.openhab.binding.hive.internal.client.FeatureAttribute;
import org.openhab.binding.hive.internal.client.OverrideMode;
import org.openhab.binding.hive.internal.client.SettableFeatureAttribute;
import org.openhab.binding.hive.internal.client.WaterHeaterOperatingMode;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class WaterHeaterFeature implements Feature {
    private final @Nullable SettableFeatureAttribute<WaterHeaterOperatingMode> operatingMode;
    private final @Nullable FeatureAttribute<Boolean> isOn;
    private final @Nullable SettableFeatureAttribute<OverrideMode> temporaryOperatingModeOverride;

    private WaterHeaterFeature(
            final @Nullable SettableFeatureAttribute<WaterHeaterOperatingMode> operatingMode,
            final @Nullable FeatureAttribute<Boolean> isOn,
            final @Nullable SettableFeatureAttribute<OverrideMode> temporaryOperatingModeOverride
    ) {
        this.operatingMode = operatingMode;
        this.isOn = isOn;
        this.temporaryOperatingModeOverride = temporaryOperatingModeOverride;
    }

    public @Nullable SettableFeatureAttribute<WaterHeaterOperatingMode> getOperatingMode() {
        return this.operatingMode;
    }

    public WaterHeaterFeature withTargetOperatingMode(final WaterHeaterOperatingMode targetOperatingMode) {
        Objects.requireNonNull(targetOperatingMode);

        final @Nullable SettableFeatureAttribute<WaterHeaterOperatingMode> operatingMode = this.operatingMode;
        if (operatingMode == null) {
            throw new IllegalStateException(FeatureBuilderUtil.getCannotSetTargetMessage("operatingMode"));
        }

        return WaterHeaterFeature.builder()
                .from(this)
                .operatingMode(operatingMode.withTargetValue(targetOperatingMode))
                .build();
    }

    public @Nullable FeatureAttribute<Boolean> getIsOn() {
        return this.isOn;
    }

    public @Nullable SettableFeatureAttribute<OverrideMode> getTemporaryOperatingModeOverride() {
        return this.temporaryOperatingModeOverride;
    }

    public WaterHeaterFeature withTargetTemporaryOperatingModeOverride(final OverrideMode targetOverrideMode) {
        Objects.requireNonNull(targetOverrideMode);

        final @Nullable SettableFeatureAttribute<OverrideMode> temporaryOperatingModeOverride = this.temporaryOperatingModeOverride;
        if (temporaryOperatingModeOverride == null) {
            throw new IllegalStateException(FeatureBuilderUtil.getCannotSetTargetMessage("temporaryOperatingModeOverride"));
        }

        return WaterHeaterFeature.builder()
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
        private @Nullable SettableFeatureAttribute<WaterHeaterOperatingMode> operatingMode;
        private @Nullable FeatureAttribute<Boolean> isOn;
        private @Nullable SettableFeatureAttribute<OverrideMode> temporaryOperatingModeOverride;

        public Builder from(final WaterHeaterFeature waterHeaterFeature) {
            Objects.requireNonNull(waterHeaterFeature);

            return this.operatingMode(waterHeaterFeature.getOperatingMode())
                    .isOn(waterHeaterFeature.getIsOn())
                    .temporaryOperatingModeOverride(waterHeaterFeature.getTemporaryOperatingModeOverride());
        }

        public Builder operatingMode(final @Nullable SettableFeatureAttribute<WaterHeaterOperatingMode> operatingMode) {
            this.operatingMode = operatingMode;

            return this;
        }

        public Builder isOn(final @Nullable FeatureAttribute<Boolean> isOn) {
            this.isOn = isOn;

            return this;
        }

        public Builder temporaryOperatingModeOverride(final @Nullable SettableFeatureAttribute<OverrideMode> temporaryOperatingModeOverride) {
            this.temporaryOperatingModeOverride = temporaryOperatingModeOverride;

            return this;
        }

        public WaterHeaterFeature build() {
            return new WaterHeaterFeature(
                    this.operatingMode,
                    this.isOn,
                    this.temporaryOperatingModeOverride
            );
        }
    }
}
