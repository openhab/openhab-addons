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
import org.openhab.binding.hive.internal.client.OnOffMode;
import org.openhab.binding.hive.internal.client.SettableFeatureAttribute;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class OnOffDeviceFeature implements Feature {
    private final @Nullable SettableFeatureAttribute<OnOffMode> mode;

    private OnOffDeviceFeature(
            final @Nullable SettableFeatureAttribute<OnOffMode> mode
    ) {
        this.mode = mode;
    }

    public @Nullable SettableFeatureAttribute<OnOffMode> getMode() {
        return this.mode;
    }

    public OnOffDeviceFeature withTargetMode(final OnOffMode targetMode) {
        Objects.requireNonNull(targetMode);

        final @Nullable SettableFeatureAttribute<OnOffMode> mode = this.mode;
        if (mode == null) {
            throw new IllegalStateException(FeatureBuilderUtil.getCannotSetTargetMessage("mode"));
        }

        return OnOffDeviceFeature.builder()
                .from(this)
                .mode(mode.withTargetValue(targetMode))
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private @Nullable SettableFeatureAttribute<OnOffMode> mode;

        public Builder from(final OnOffDeviceFeature onOffDeviceFeature) {
            Objects.requireNonNull(onOffDeviceFeature);

            return this.mode(onOffDeviceFeature.getMode());
        }

        public Builder mode(final @Nullable SettableFeatureAttribute<OnOffMode> mode) {
            this.mode = mode;

            return this;
        }

        public OnOffDeviceFeature build() {
            return new OnOffDeviceFeature(this.mode);
        }
    }
}
