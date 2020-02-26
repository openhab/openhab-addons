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

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class PhysicalDeviceFeature implements Feature {
    private final @Nullable FeatureAttribute<String> hardwareIdentifier;
    private final @Nullable FeatureAttribute<String> model;
    private final @Nullable FeatureAttribute<String> manufacturer;
    private final @Nullable FeatureAttribute<String> softwareVersion;

    private PhysicalDeviceFeature(
            final @Nullable FeatureAttribute<String> hardwareIdentifier,
            final @Nullable FeatureAttribute<String> model,
            final @Nullable FeatureAttribute<String> manufacturer,
            final @Nullable FeatureAttribute<String> softwareVersion
    ) {
        this.hardwareIdentifier = hardwareIdentifier;
        this.model = model;
        this.manufacturer = manufacturer;
        this.softwareVersion = softwareVersion;
    }

    public @Nullable FeatureAttribute<String> getHardwareIdentifier() {
        return this.hardwareIdentifier;
    }

    public @Nullable FeatureAttribute<String> getModel() {
        return this.model;
    }

    public @Nullable FeatureAttribute<String> getManufacturer() {
        return this.manufacturer;
    }

    public @Nullable FeatureAttribute<String> getSoftwareVersion() {
        return this.softwareVersion;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static final class Builder {
        private @Nullable FeatureAttribute<String> hardwareIdentifier;
        private @Nullable FeatureAttribute<String> model;
        private @Nullable FeatureAttribute<String> manufacturer;
        private @Nullable FeatureAttribute<String> softwareVersion;

        public Builder from(final PhysicalDeviceFeature physicalDeviceFeature) {
            Objects.requireNonNull(physicalDeviceFeature);

            return this.hardwareIdentifier(physicalDeviceFeature.getHardwareIdentifier())
                    .model(physicalDeviceFeature.getModel())
                    .manufacturer(physicalDeviceFeature.getManufacturer())
                    .softwareVersion(physicalDeviceFeature.getSoftwareVersion());
        }

        public Builder hardwareIdentifier(final @Nullable FeatureAttribute<String> hardwareIdentifier) {
            this.hardwareIdentifier = hardwareIdentifier;

            return this;
        }

        public Builder model(final @Nullable FeatureAttribute<String> model) {
            this.model = model;

            return this;
        }

        public Builder manufacturer(final @Nullable FeatureAttribute<String> manufacturer) {
            this.manufacturer = manufacturer;

            return this;
        }

        public Builder softwareVersion(final @Nullable FeatureAttribute<String> softwareVersion) {
            this.softwareVersion = softwareVersion;

            return this;
        }
        
        public PhysicalDeviceFeature build() {
            return new PhysicalDeviceFeature(
                    this.hardwareIdentifier,
                    this.model,
                    this.manufacturer,
                    this.softwareVersion
            );
        }
    }
}
