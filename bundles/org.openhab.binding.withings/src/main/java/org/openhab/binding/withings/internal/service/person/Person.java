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
package org.openhab.binding.withings.internal.service.person;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.withings.internal.api.measure.LatestMeasureData;
import org.openhab.binding.withings.internal.api.sleep.LatestSleepData;

/**
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
public class Person {

    private final Optional<LatestMeasureData> measureData;
    private final Optional<LatestSleepData> sleepData;

    public Person(Optional<LatestMeasureData> measureData, Optional<LatestSleepData> sleepData) {
        this.measureData = measureData;
        this.sleepData = sleepData;
    }

    public @Nullable BigDecimal getWeight() {
        return extractMeasureData(LatestMeasureData::getWeight);
    }

    public @Nullable BigDecimal getHeight() {
        return extractMeasureData(LatestMeasureData::getHeight);
    }

    public @Nullable BigDecimal getFatMass() {
        return extractMeasureData(LatestMeasureData::getFatMass);
    }

    public @Nullable Date getLastSleepStart() {
        return extractSleepData(LatestSleepData::getSleepStart);
    }

    public @Nullable Date getLastSleepEnd() {
        return extractSleepData(LatestSleepData::getSleepEnd);
    }

    public @Nullable Integer getLastSleepScore() {
        return extractSleepData(LatestSleepData::getSleepScore);
    }

    private <R> @Nullable R extractMeasureData(Function<LatestMeasureData, R> function) {
        return measureData.map(function).orElse(null);
    }

    private <R> @Nullable R extractSleepData(Function<LatestSleepData, R> function) {
        return sleepData.map(function).orElse(null);
    }
}
