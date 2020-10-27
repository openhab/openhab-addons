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
package org.openhab.binding.withings.internal.api.measure;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
public class LatestMeasureData {

    private final @Nullable BigDecimal weight;
    private final @Nullable BigDecimal height;
    private final @Nullable BigDecimal fatMass;

    public LatestMeasureData(Optional<MeasuresResponse.Measure> weight, Optional<MeasuresResponse.Measure> height,
            Optional<MeasuresResponse.Measure> fatMass) {
        this.weight = calculateValue(weight, 1);
        this.height = calculateValue(height, 2);
        this.fatMass = calculateValue(fatMass, 1);
    }

    public @Nullable BigDecimal getWeight() {
        return weight;
    }

    public @Nullable BigDecimal getHeight() {
        return height;
    }

    public @Nullable BigDecimal getFatMass() {
        return fatMass;
    }

    private static @Nullable BigDecimal calculateValue(Optional<MeasuresResponse.Measure> measureOptional, int scale) {
        if (measureOptional.isPresent()) {
            MeasuresResponse.Measure measure = measureOptional.get();
            BigDecimal divisor = createUnitDivisor(measure.getUnit());
            return BigDecimal.valueOf(measure.getValue()).divide(divisor, scale, RoundingMode.HALF_UP);
        }
        return null;
    }

    private static BigDecimal createUnitDivisor(int unit) {
        return BigDecimal.TEN.pow(BigDecimal.valueOf(unit).abs().intValue());
    }
}
