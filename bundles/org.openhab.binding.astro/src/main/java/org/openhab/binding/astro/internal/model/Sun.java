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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Holds the calculated sun data.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class Sun extends RiseSet implements Planet {

    private Map<SunPhase, Range> ranges = new HashMap<>();

    private Position position = Position.NONE;
    private Zodiac zodiac = Zodiac.NONE;
    private EclipseSet eclipseSet = EclipseSet.NONE;
    private Radiation radiation = Radiation.NONE;

    private @Nullable Season season = null;

    private @Nullable SunPhase sunPhase;

    private Circadian circadian = Circadian.NONE;

    /**
     * Returns the astro dawn range.
     */
    @Nullable
    public Range getAstroDawn() {
        return ranges.get(SunPhase.ASTRO_DAWN);
    }

    /**
     * Sets the astro dawn range.
     */
    public void setAstroDawn(Range astroDawn) {
        ranges.put(SunPhase.ASTRO_DAWN, astroDawn);
    }

    /**
     * Returns the nautic dawn range.
     */
    @Nullable
    public Range getNauticDawn() {
        return ranges.get(SunPhase.NAUTIC_DAWN);
    }

    /**
     * Sets the nautic dawn range.
     */
    public void setNauticDawn(Range nauticDawn) {
        ranges.put(SunPhase.NAUTIC_DAWN, nauticDawn);
    }

    /**
     * Returns the civil dawn range.
     */
    @Nullable
    public Range getCivilDawn() {
        return ranges.get(SunPhase.CIVIL_DAWN);
    }

    /**
     * Sets the civil dawn range.
     */
    public void setCivilDawn(Range civilDawn) {
        ranges.put(SunPhase.CIVIL_DAWN, civilDawn);
    }

    /**
     * Returns the civil dusk range.
     */
    @Nullable
    public Range getCivilDusk() {
        return ranges.get(SunPhase.CIVIL_DUSK);
    }

    /**
     * Sets the civil dusk range.
     */
    public void setCivilDusk(Range civilDusk) {
        ranges.put(SunPhase.CIVIL_DUSK, civilDusk);
    }

    /**
     * Returns the nautic dusk range.
     */
    @Nullable
    public Range getNauticDusk() {
        return ranges.get(SunPhase.NAUTIC_DUSK);
    }

    /**
     * Sets the nautic dusk range.
     */
    public void setNauticDusk(Range nauticDusk) {
        ranges.put(SunPhase.NAUTIC_DUSK, nauticDusk);
    }

    /**
     * Returns the astro dusk range.
     */
    @Nullable
    public Range getAstroDusk() {
        return ranges.get(SunPhase.ASTRO_DUSK);
    }

    /**
     * Sets the astro dusk range.
     */
    public void setAstroDusk(Range astroDusk) {
        ranges.put(SunPhase.ASTRO_DUSK, astroDusk);
    }

    /**
     * Returns the noon range, start and end is always equal.
     */
    @Nullable
    public Range getNoon() {
        return ranges.get(SunPhase.NOON);
    }

    /**
     * Sets the noon range.
     */
    public void setNoon(Range noon) {
        ranges.put(SunPhase.NOON, noon);
    }

    /**
     * Returns the daylight range.
     */
    @Nullable
    public Range getDaylight() {
        return ranges.get(SunPhase.DAYLIGHT);
    }

    /**
     * Sets the daylight range.
     */
    public void setDaylight(Range daylight) {
        ranges.put(SunPhase.DAYLIGHT, daylight);
    }

    /**
     * Returns the morning night range.
     */
    @Nullable
    public Range getMorningNight() {
        return ranges.get(SunPhase.MORNING_NIGHT);
    }

    /**
     * Sets the morning night range.
     */
    public void setMorningNight(Range morningNight) {
        ranges.put(SunPhase.MORNING_NIGHT, morningNight);
    }

    /**
     * Returns the evening night range.
     */
    @Nullable
    public Range getEveningNight() {
        return ranges.get(SunPhase.EVENING_NIGHT);
    }

    /**
     * Sets the evening night range.
     */
    public void setEveningNight(Range eveningNight) {
        ranges.put(SunPhase.EVENING_NIGHT, eveningNight);
    }

    /**
     * Returns the night range.
     */
    @Nullable
    public Range getNight() {
        return ranges.get(SunPhase.NIGHT);
    }

    /**
     * Sets the night range.
     */
    public void setNight(Range night) {
        ranges.put(SunPhase.NIGHT, night);
    }

    /**
     * Sets the rise range.
     */
    @Override
    public void setRise(Range rise) {
        super.setRise(rise);
        ranges.put(SunPhase.SUN_RISE, rise);
    }

    /**
     * Sets the set range.
     */
    @Override
    public void setSet(Range set) {
        super.setSet(set);
        ranges.put(SunPhase.SUN_SET, set);
    }

    /**
     * Returns the sun position.
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Returns the sun radiation
     */
    public Radiation getRadiation() {
        return radiation;
    }

    public void setRadiation(Radiation radiation) {
        this.radiation = radiation;
    }

    /**
     * Sets the sun position.
     */
    public void setPosition(Position position) {
        this.position = position;
    }

    /**
     * Returns the zodiac.
     */
    public Zodiac getZodiac() {
        return zodiac;
    }

    /**
     * Sets the zodiac.
     */
    public void setZodiac(Zodiac zodiac) {
        this.zodiac = zodiac;
    }

    /**
     * Returns the seasons.
     */
    @Nullable
    public Season getSeason() {
        return season;
    }

    /**
     * Sets the seasons.
     */
    public void setSeason(Season season) {
        this.season = season;
    }

    /**
     * Returns the eclipses.
     */
    public EclipseSet getEclipseSet() {
        return eclipseSet;
    }

    public void setEclipseSet(EclipseSet eclipseSet) {
        this.eclipseSet = eclipseSet;
    }

    /**
     * Returns the sun phase.
     */
    @Nullable
    public SunPhase getSunPhase() {
        return sunPhase;
    }

    /**
     * Sets the sun phase.
     */
    public void setSunPhase(@Nullable SunPhase sunPhase) {
        this.sunPhase = sunPhase;
    }

    /**
     * Returns all ranges of the sun.
     */
    public Map<SunPhase, Range> getAllRanges() {
        return ranges;
    }

    public Circadian getCircadian() {
        return circadian;
    }

    public void setCircadian(Circadian circadian) {
        this.circadian = circadian;
    }
}
