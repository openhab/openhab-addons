/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.astro.internal.model;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.astro.internal.util.MathUtils;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;

/**
 * Holds the season dates of the year and the current name.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class Season {
    private final Map<Hemisphere, List<Integer>> seasonOrder = Map.of(Hemisphere.NORTHERN, List.of(0, 1, 2, 3),
            Hemisphere.SOUTHERN, List.of(2, 3, 0, 1));
    private final Hemisphere hemisphere;
    private final List<Instant> equiSols;

    public Season(double latitude, Instant... equiSols) {
        // Expect to receive last of previous year, all from current year, first of next year
        if (equiSols.length != SeasonName.values().length + 2) {
            throw new IllegalArgumentException("Incorrect number of seasons provided");
        }
        this.hemisphere = Hemisphere.getHemisphere(latitude);
        this.equiSols = Arrays.stream(equiSols).sorted().toList();
    }

    public int getYear() {
        return equiSols.get(1).get(ChronoField.YEAR);
    }

    /**
     * Returns the date of the beginning of spring.
     */
    public Instant getSpring() {
        return equiSols.get(Objects.requireNonNull(seasonOrder.get(hemisphere)).get(0) + 1);
    }

    /**
     * Returns the date of the beginning of summer.
     */
    public Instant getSummer() {
        return equiSols.get(Objects.requireNonNull(seasonOrder.get(hemisphere)).get(1) + 1);
    }

    /**
     * Returns the date of the beginning of autumn.
     */
    public Instant getAutumn() {
        return equiSols.get(Objects.requireNonNull(seasonOrder.get(hemisphere)).get(2) + 1);
    }

    /**
     * Returns the date of the beginning of winter.
     */
    public Instant getWinter() {
        return equiSols.get(Objects.requireNonNull(seasonOrder.get(hemisphere)).get(3) + 1);
    }

    /**
     * Returns the current season name.
     */
    public SeasonName getName() {
        var now = Instant.now();
        for (int i = 0; i < equiSols.size() - 1; i++) {
            if (equiSols.get(i).isBefore(now) && equiSols.get(i + 1).isAfter(now)) {
                SeasonName seasonName = SeasonName.values()[(int) MathUtils.mod(i + 3, 4)];
                return seasonName;
            }
        }
        throw new IllegalArgumentException("This case should not arrive");
    }

    /**
     * Returns the next season.
     */
    public Instant getNextSeason() {
        return getNext(Instant.now());
    }

    /**
     * Returns the next season name.
     */
    public SeasonName getNextName() {
        int ordinal = getName().ordinal() + 1;
        if (ordinal > 3) {
            ordinal = 0;
        }
        return SeasonName.values()[ordinal];
    }

    /**
     * Returns the time left for current season
     */
    public QuantityType<Time> getTimeLeft() {
        var now = Instant.now();
        var timeLeft = Duration.between(now, getNext(now));

        return new QuantityType<>(timeLeft.toDays(), Units.DAY);
    }

    public Instant getNext(Instant now) {
        for (Instant equiSol : equiSols) {
            if (equiSol.isAfter(now)) {
                return equiSol;
            }
        }
        throw new IllegalArgumentException("This case should not arrive");
    }
}
