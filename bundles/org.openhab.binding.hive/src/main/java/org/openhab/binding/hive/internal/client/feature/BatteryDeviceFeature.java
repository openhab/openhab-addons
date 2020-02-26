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
import javax.measure.quantity.ElectricPotential;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hive.internal.client.BatteryLevel;
import org.openhab.binding.hive.internal.client.FeatureAttribute;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class BatteryDeviceFeature implements Feature {
    private final @Nullable FeatureAttribute<BatteryLevel> batteryLevel;
    private final @Nullable FeatureAttribute<String> batteryState;
    private final @Nullable FeatureAttribute<Quantity<ElectricPotential>> batteryVoltage;
    private final @Nullable FeatureAttribute<String> batteryNotificationState;

    private BatteryDeviceFeature(
            final @Nullable FeatureAttribute<BatteryLevel> batteryLevel,
            final @Nullable FeatureAttribute<String> batteryState,
            final @Nullable FeatureAttribute<Quantity<ElectricPotential>> batteryVoltage,
            final @Nullable FeatureAttribute<String> batteryNotificationState
    ) {
        this.batteryLevel = batteryLevel;
        this.batteryState = batteryState;
        this.batteryVoltage = batteryVoltage;
        this.batteryNotificationState = batteryNotificationState;
    }

    public @Nullable FeatureAttribute<BatteryLevel> getBatteryLevel() {
        return this.batteryLevel;
    }

    public @Nullable FeatureAttribute<String> getBatteryState() {
        return this.batteryState;
    }

    public @Nullable FeatureAttribute<Quantity<ElectricPotential>> getBatteryVoltage() {
        return this.batteryVoltage;
    }

    public @Nullable FeatureAttribute<String> getBatteryNotificationState() {
        return this.batteryNotificationState;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private @Nullable FeatureAttribute<BatteryLevel> batteryLevel;
        private @Nullable FeatureAttribute<String> batteryState;
        private @Nullable FeatureAttribute<Quantity<ElectricPotential>> batteryVoltage;
        private @Nullable FeatureAttribute<String> batteryNotificationState;

        public Builder from(final BatteryDeviceFeature batteryDeviceFeature) {
            Objects.requireNonNull(batteryDeviceFeature);

            return this.batteryLevel(batteryDeviceFeature.getBatteryLevel())
                    .batteryState(batteryDeviceFeature.getBatteryState())
                    .batteryVoltage(batteryDeviceFeature.getBatteryVoltage())
                    .batteryNotificationState(batteryDeviceFeature.getBatteryNotificationState());
        }

        public Builder batteryLevel(final @Nullable FeatureAttribute<BatteryLevel> batteryLevel) {
            this.batteryLevel = batteryLevel;

            return this;
        }

        public Builder batteryState(final @Nullable FeatureAttribute<String> batteryState) {
            this.batteryState = batteryState;

            return this;
        }

        public Builder batteryVoltage(final @Nullable FeatureAttribute<Quantity<ElectricPotential>> batteryVoltage) {
            this.batteryVoltage = batteryVoltage;

            return this;
        }

        public Builder batteryNotificationState(final @Nullable FeatureAttribute<String> batteryNotificationState) {
            this.batteryNotificationState = batteryNotificationState;

            return this;
        }

        public BatteryDeviceFeature build() {
            return new BatteryDeviceFeature(
                    this.batteryLevel,
                    this.batteryState,
                    this.batteryVoltage,
                    this.batteryNotificationState
            );
        }
    }
}
