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
package org.openhab.binding.hive.internal.client;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hive.internal.client.dto.FeatureAttributeDto;
import org.openhab.binding.hive.internal.client.dto.HiveApiInstant;
import org.openhab.binding.hive.internal.client.exception.HiveClientResponseException;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class FeatureAttributeFactory {
    private FeatureAttributeFactory() {
        throw new AssertionError();
    }

    private static <F> void applyNano1DtoFix(
            final FeatureAttributeDto<F> dto
    ) {
        // Try to fix the weirdness of the NANO1 hub
        @Nullable F fixupValue = dto.targetValue;
        if (dto.reportedValue == null && fixupValue != null) {
            dto.reportedValue = fixupValue;
        } else if (dto.reportedValue != null) {
            fixupValue = dto.reportedValue;
        } else {
            throw new IllegalStateException("reportedValue is null and I cannot fix it");
        }

        if (dto.displayValue == null) {
            if (fixupValue != null) {
                dto.displayValue = fixupValue;
            } else {
                throw new IllegalStateException("displayValue is null and I cannot fix it");
            }
        }
    }

    private static <F, T> void buildFeatureAttribute(
            final DefaultFeatureAttribute.Builder<T> featureAttributeBuilder,
            final Adapter<F, T> adapter,
            final FeatureAttributeDto<F> dto
    ) throws HiveClientResponseException {
        Objects.requireNonNull(featureAttributeBuilder);
        Objects.requireNonNull(adapter);
        Objects.requireNonNull(dto);

        final @Nullable F displayValue = dto.displayValue;
        final @Nullable F reportedValue = dto.reportedValue;
        final @Nullable HiveApiInstant reportChangedTime = dto.reportChangedTime;
        final @Nullable HiveApiInstant reportReceivedTime = dto.reportReceivedTime;

        if (displayValue == null) {
            throw new HiveClientResponseException("Display value is unexpectedly null.");
        }
        featureAttributeBuilder.displayValue(adapter.apply(displayValue));

        if (reportedValue == null) {
            throw new HiveClientResponseException("Reported value is unexpectedly null.");
        }
        featureAttributeBuilder.reportedValue(adapter.apply(reportedValue));

        if (reportChangedTime != null) {
            featureAttributeBuilder.reportChangedTime(reportChangedTime.asInstant());
        }

        if (reportReceivedTime != null) {
            featureAttributeBuilder.reportReceivedTime(reportReceivedTime.asInstant());
        }
    }

    public static <T> @Nullable FeatureAttribute<T> getReadOnlyFromDto(final @Nullable FeatureAttributeDto<T> dto) throws HiveClientResponseException {
        return getReadOnlyFromDtoWithAdapter(Adapter.identity(), dto);
    }

    public static <F, T> @Nullable FeatureAttribute<T> getReadOnlyFromDtoWithAdapter(
            final Adapter<F, T> adapter,
            final @Nullable FeatureAttributeDto<F> dto
    ) throws HiveClientResponseException {
        if (dto == null) {
            return null;
        }

        applyNano1DtoFix(dto);

        final DefaultFeatureAttribute.Builder<T> featureAttributeBuilder = DefaultFeatureAttribute.builder();

        buildFeatureAttribute(
                featureAttributeBuilder,
                adapter,
                dto
        );

        return featureAttributeBuilder.build();
    }

    public static <T> @Nullable SettableFeatureAttribute<T> getSettableFromDto(final @Nullable FeatureAttributeDto<T> dto) throws HiveClientResponseException {
        return getSettableFromDtoWithAdapter(Adapter.identity(), dto);
    }

    public static <F, T> @Nullable SettableFeatureAttribute<T> getSettableFromDtoWithAdapter(
            final Adapter<F, T> adapter,
            final @Nullable FeatureAttributeDto<F> dto
    ) throws HiveClientResponseException {
        if (dto == null) {
            return null;
        }

        applyNano1DtoFix(dto);

        final DefaultFeatureAttribute.Builder<T> featureAttributeBuilder = DefaultFeatureAttribute.builder();

        buildFeatureAttribute(
                featureAttributeBuilder,
                adapter,
                dto
        );

        final @Nullable F targetValue = dto.targetValue;
        if (targetValue != null) {
            featureAttributeBuilder.targetValue(adapter.apply(targetValue));
        }

        return featureAttributeBuilder.build();
    }

    @FunctionalInterface
    public interface Adapter<F, T> {
        T apply(F from) throws HiveClientResponseException;

        static <T> Adapter<T, T> identity() {
            return from -> from;
        }
    }
}
