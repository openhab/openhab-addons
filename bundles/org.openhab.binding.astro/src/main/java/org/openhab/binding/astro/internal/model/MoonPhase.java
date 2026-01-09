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

<<<<<<< Upstream, based on main
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
=======
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
>>>>>>> bb4de3d Starting to work on transition to Instant for MoonPhase

import javax.measure.quantity.Angle;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.astro.internal.util.DateTimeUtils;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Holds the calculates moon phase informations.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Christoph Weitkamp - Introduced UoM
 */
@NonNullByDefault
public class MoonPhase {
<<<<<<< Upstream, based on main
    private final Map<MoonPhaseName, @Nullable Calendar> phases = new HashMap<>(MoonPhaseName.values().length);

=======
    private final Map<MoonPhaseName, @Nullable Instant> phases = new HashMap<>(MoonPhaseName.values().length);
>>>>>>> bb4de3d Starting to work on transition to Instant for MoonPhase
    private double age;
    private double illumination;
    private double agePercent;
    private double ageDegree;

    private @Nullable MoonPhaseName name;

    public MoonPhase() {
<<<<<<< Upstream, based on main
        phases.put(MoonPhaseName.FIRST_QUARTER, null);
        phases.put(MoonPhaseName.FULL, null);
        phases.put(MoonPhaseName.THIRD_QUARTER, null);
        phases.put(MoonPhaseName.NEW, null);
=======
        Arrays.stream(MoonPhaseName.values()).filter(phase -> !Double.isNaN(phase.mode))
                .forEach(phase -> phases.put(phase, null));
    }

    public @Nullable Instant getPhase(MoonPhaseName phase) {
        return phases.get(phase);
>>>>>>> bb4de3d Starting to work on transition to Instant for MoonPhase
    }

    /**
     * Returns the date at which the moon is in the first quarter.
     */
    @Nullable
<<<<<<< Upstream, based on main
    public Calendar getFirstQuarter() {
        return getPhaseDate(MoonPhaseName.FIRST_QUARTER);
=======
    public Instant getFirstQuarter() {
        return getPhase(MoonPhaseName.FIRST_QUARTER);
    }

    public void setPhase(MoonPhaseName phase, double jdWhen) {
        phases.put(phase, DateTimeUtils.jdToInstant(jdWhen));
>>>>>>> bb4de3d Starting to work on transition to Instant for MoonPhase
    }

    /**
     * Returns the date of the full moon.
     */
    @Nullable
<<<<<<< Upstream, based on main
    public Calendar getFull() {
        return getPhaseDate(MoonPhaseName.FULL);
=======
    public Instant getFull() {
        return getPhase(MoonPhaseName.FULL);
>>>>>>> bb4de3d Starting to work on transition to Instant for MoonPhase
    }

    /**
     * Returns the date at which the moon is in the third quarter.
     */
    @Nullable
<<<<<<< Upstream, based on main
    public Calendar getThirdQuarter() {
        return getPhaseDate(MoonPhaseName.THIRD_QUARTER);
=======
    public Instant getThirdQuarter() {
        return getPhase(MoonPhaseName.THIRD_QUARTER);
>>>>>>> bb4de3d Starting to work on transition to Instant for MoonPhase
    }

    /**
     * Returns the date of the new moon.
     */
    @Nullable
<<<<<<< Upstream, based on main
    public Calendar getNew() {
        return getPhaseDate(MoonPhaseName.NEW);
    }

    @Nullable
    public Calendar getPhaseDate(MoonPhaseName moonPhase) {
        if (!phases.containsKey(moonPhase)) {
            throw new IllegalArgumentException("MoonPhase does not handle %s".formatted(moonPhase.toString()));
        }
        return phases.get(moonPhase);
    }

    public void setPhase(MoonPhaseName moonPhase, @Nullable Calendar calendar) {
        if (!phases.containsKey(moonPhase)) {
            throw new IllegalArgumentException("MoonPhase does not handle %s".formatted(moonPhase.toString()));
        }
        phases.put(moonPhase, calendar);
    }

    public Stream<MoonPhaseName> remarkablePhases() {
        return phases.keySet().stream();
=======
    public Instant getNew() {
        return getPhase(MoonPhaseName.NEW);
>>>>>>> bb4de3d Starting to work on transition to Instant for MoonPhase
    }

    /**
     * Returns the age in days.
     */
    public QuantityType<Time> getAge() {
        return new QuantityType<>(age, Units.DAY);
    }

    /**
     * Sets the age in days.
     */
    public void setAge(double age) {
        this.age = age;
    }

    /**
     * Returns the illumination.
     */
    public State getIllumination() {
        return Double.isNaN(illumination) ? UnDefType.UNDEF
                : illumination < 0 ? UnDefType.NULL : new QuantityType<>(illumination, Units.PERCENT);
    }

    /**
     * Sets the illumination.
     */
    public void setIllumination(double illumination) {
        this.illumination = illumination;
    }

    /**
     * Returns the phase name.
     */
    @Nullable
    public MoonPhaseName getName() {
        return name;
    }

    /**
     * Sets the phase name.
     */
    public void setName(@Nullable MoonPhaseName name) {
        this.name = name;
    }

    /**
     * Returns the age in degree.
     */
    public QuantityType<Angle> getAgeDegree() {
        return new QuantityType<>(ageDegree, Units.DEGREE_ANGLE);
    }

    /**
     * Returns the age in percent.
     */
    public QuantityType<Dimensionless> getAgePercent() {
        return new QuantityType<>(agePercent, Units.PERCENT);
    }

    /**
     * Sets the age in percent.
     */
    public void setAgePercent(double agePercent) {
        this.agePercent = agePercent;
        this.ageDegree = agePercent * 3.6;
    }
}
