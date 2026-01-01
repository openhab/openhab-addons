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

import java.util.Calendar;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;

/**
 * Holds the calculates moon phase informations.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Christoph Weitkamp - Introduced UoM
 */
@NonNullByDefault
public class MoonPhase {
    private @Nullable Calendar firstQuarter;
    private @Nullable Calendar full;
    private @Nullable Calendar thirdQuarter;
    private @Nullable Calendar newCalendar;
    private double age;
    private double illumination;
    private double agePercent;
    private double ageDegree;

    private @Nullable MoonPhaseName name;

    /**
     * Returns the date at which the moon is in the first quarter.
     */
    @Nullable
    public Calendar getFirstQuarter() {
        return firstQuarter;
    }

    /**
     * Sets the date at which the moon is in the first quarter.
     */
    public void setFirstQuarter(@Nullable Calendar firstQuarter) {
        this.firstQuarter = firstQuarter;
    }

    /**
     * Returns the date of the full moon.
     */
    @Nullable
    public Calendar getFull() {
        return full;
    }

    /**
     * Sets the date of the full moon.
     */
    public void setFull(@Nullable Calendar full) {
        this.full = full;
    }

    /**
     * Returns the date at which the moon is in the third quarter.
     */
    @Nullable
    public Calendar getThirdQuarter() {
        return thirdQuarter;
    }

    /**
     * Sets the date at which the moon is in the third quarter.
     */
    public void setThirdQuarter(@Nullable Calendar thirdQuarter) {
        this.thirdQuarter = thirdQuarter;
    }

    /**
     * Returns the date of the new moon.
     */
    @Nullable
    public Calendar getNew() {
        return newCalendar;
    }

    /**
     * Sets the date of the new moon.
     */
    public void setNew(@Nullable Calendar newCalendar) {
        this.newCalendar = newCalendar;
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
    public QuantityType<Dimensionless> getIllumination() {
        return new QuantityType<>(illumination, Units.PERCENT);
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
     * Sets the age in degree.
     */
    public void setAgeDegree(double ageDegree) {
        this.ageDegree = ageDegree;
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
    }
}
