/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.TimeZone;

import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.astro.internal.util.DateTimeUtils;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;

/**
 * Holds the season dates of the year and the current name.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class Season {
    private static final Map<Hemisphere, List<SeasonName>> SEASON_ORDER = Map.of(Hemisphere.NORTHERN,
            List.of(SeasonName.WINTER, SeasonName.SPRING, SeasonName.SUMMER, SeasonName.AUTUMN, SeasonName.WINTER,
                    SeasonName.SPRING),
            Hemisphere.SOUTHERN, List.of(SeasonName.SUMMER, SeasonName.AUTUMN, SeasonName.WINTER, SeasonName.SPRING,
                    SeasonName.SUMMER, SeasonName.AUTUMN));

    private record LocalSeason(SeasonName name, Instant startsOn, Instant endsOn, int year) {
        boolean contains(Instant when) {
            return !startsOn.isAfter(when) && endsOn.isAfter(when);
        }
    }

    private final List<LocalSeason> seasons = new ArrayList<>(5);
    private final int year;

    public Season(double latitude, boolean useMeteorologicalSeason, TimeZone zone, Instant... equiSols) {
        // Expect to receive last of previous year, all from current year, first of next year
        if (equiSols.length != SeasonName.values().length + 2) {
            throw new IllegalArgumentException("Incorrect number of seasons provided");
        }
        var hemisphere = Hemisphere.getHemisphere(latitude);
        List<Instant> moments = Arrays.stream(equiSols).sorted()
                .map(i -> useMeteorologicalSeason ? DateTimeUtils.atMidnightOfFirstMonthDay(i, zone) : i).toList();
        for (int i = 0; i < moments.size() - 1; i++) {
            var current = moments.get(i);
            var next = moments.get(i + 1);
            var seasonName = Objects.requireNonNull(SEASON_ORDER.get(hemisphere)).get(i);
            ZonedDateTime zonedDateTime = current.atZone(zone.toZoneId());
            seasons.add(new LocalSeason(seasonName, current, next, zonedDateTime.getYear()));
        }
        year = seasons.stream().mapToInt(LocalSeason::year).max().orElseThrow(NoSuchElementException::new);
    }

    public int getYear() {
        return year;
    }

    private Instant getSeasonStart(SeasonName season) {
        return seasons.stream().filter(s -> s.name.equals(season) && s.year == year).map(s -> s.startsOn).findFirst()
                .orElseThrow(NoSuchElementException::new);
    }

    /**
     * Returns the date of the beginning of spring.
     */
    public Instant getSpring() {
        return getSeasonStart(SeasonName.SPRING);
    }

    /**
     * Returns the date of the beginning of summer.
     */
    public Instant getSummer() {
        return getSeasonStart(SeasonName.SUMMER);
    }

    /**
     * Returns the date of the beginning of autumn.
     */
    public Instant getAutumn() {
        return getSeasonStart(SeasonName.AUTUMN);
    }

    /**
     * Returns the date of the beginning of winter.
     */
    public Instant getWinter() {
        return getSeasonStart(SeasonName.WINTER);
    }

    private LocalSeason getSeason(Instant when) {
        return seasons.stream().filter(s -> s.contains(when)).findFirst().orElseThrow(NoSuchElementException::new);
    }

    /**
     * Returns the current season name.
     */
    public SeasonName getName() {
        return getSeason(Instant.now()).name;
    }

    /**
     * Returns the next season.
     */
    public Instant getNextSeason() {
        return getSeason(Instant.now()).endsOn;
    }

    /**
     * Returns the next season name.
     */
    public SeasonName getNextName() {
        return getSeason(Instant.now()).name.next();
    }

    /**
     * Returns the time left for current season
     */
    public QuantityType<Time> getTimeLeft() {
        var now = Instant.now();
        var timeLeft = Duration.between(now, getSeason(now).endsOn);

        return new QuantityType<>(timeLeft.toDays(), Units.DAY);
    }

    public Instant getNext(Instant when) {
        return getSeason(when).endsOn;
    }
}
