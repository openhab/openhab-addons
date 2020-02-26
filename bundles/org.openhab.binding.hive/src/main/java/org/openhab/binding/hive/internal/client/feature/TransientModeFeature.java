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
import java.time.ZonedDateTime;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hive.internal.client.FeatureAttribute;
import org.openhab.binding.hive.internal.client.SettableFeatureAttribute;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class TransientModeFeature implements Feature {
    private final @Nullable SettableFeatureAttribute<Duration> duration;
    private final @Nullable SettableFeatureAttribute<Boolean> isEnabled;
    private final @Nullable FeatureAttribute<ZonedDateTime> startDatetime;
    private final @Nullable FeatureAttribute<ZonedDateTime> endDatetime;

    private TransientModeFeature(
            final @Nullable SettableFeatureAttribute<Duration> duration,
            final @Nullable SettableFeatureAttribute<Boolean> isEnabled,
            final @Nullable FeatureAttribute<ZonedDateTime> startDatetime,
            final @Nullable FeatureAttribute<ZonedDateTime> endDatetime
    ) {
        this.duration = duration;
        this.isEnabled = isEnabled;
        this.startDatetime = startDatetime;
        this.endDatetime = endDatetime;
    }

    public @Nullable SettableFeatureAttribute<Duration> getDuration() {
        return this.duration;
    }

    public TransientModeFeature withTargetDuration(final Duration targetDuration) {
        Objects.requireNonNull(targetDuration);

        final @Nullable SettableFeatureAttribute<Duration> duration = this.duration;
        if (duration == null) {
            throw new IllegalStateException(FeatureBuilderUtil.getCannotSetTargetMessage("duration"));
        }

        return TransientModeFeature.builder()
                .from(this)
                .duration(duration.withTargetValue(targetDuration))
                .build();
    }

    public @Nullable SettableFeatureAttribute<Boolean> getIsEnabled() {
        return this.isEnabled;
    }

    public TransientModeFeature withTargetIsEnabled(final boolean targetIsEnabled) {
        final @Nullable SettableFeatureAttribute<Boolean> isEnabled = this.isEnabled;
        if (isEnabled == null) {
            throw new IllegalStateException(FeatureBuilderUtil.getCannotSetTargetMessage("isEnabled"));
        }

        return TransientModeFeature.builder()
                .from(this)
                .isEnabled(isEnabled.withTargetValue(targetIsEnabled))
                .build();
    }

    public @Nullable FeatureAttribute<ZonedDateTime> getStartDatetime() {
        return this.startDatetime;
    }

    public @Nullable FeatureAttribute<ZonedDateTime> getEndDatetime() {
        return this.endDatetime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private @Nullable SettableFeatureAttribute<Duration> duration;
        private @Nullable SettableFeatureAttribute<Boolean> isEnabled;
        private @Nullable FeatureAttribute<ZonedDateTime> startDatetime;
        private @Nullable FeatureAttribute<ZonedDateTime> endDatetime;

        public Builder from(final TransientModeFeature transientModeFeature) {
            Objects.requireNonNull(transientModeFeature);

            return this.duration(transientModeFeature.getDuration())
                    .isEnabled(transientModeFeature.getIsEnabled())
                    .startDatetime(transientModeFeature.getStartDatetime())
                    .endDatetime(transientModeFeature.getEndDatetime());
        }

        public Builder duration(final @Nullable SettableFeatureAttribute<Duration> duration) {
            this.duration = duration;

            return this;
        }

        public Builder isEnabled(final @Nullable SettableFeatureAttribute<Boolean> isEnabled) {
            this.isEnabled = isEnabled;

            return this;
        }

        public Builder startDatetime(final @Nullable FeatureAttribute<ZonedDateTime> startDatetime) {
            this.startDatetime = startDatetime;

            return this;
        }

        public Builder endDatetime(final @Nullable FeatureAttribute<ZonedDateTime> endDatetime) {
            this.endDatetime = endDatetime;

            return this;
        }

        public TransientModeFeature build() {
            return new TransientModeFeature(
                    this.duration,
                    this.isEnabled,
                    this.startDatetime,
                    this.endDatetime
            );
        }
    }
}
