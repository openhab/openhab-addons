/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

/**
 * Holds the calculated sun data.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class Sun extends RiseSet implements Planet {

    private Map<SunPhaseName, Range> ranges = new HashMap<>();

    private Position position = new Position();

    private SunZodiac zodiac = new SunZodiac(null, null);

    private Season season = new Season();

    private Eclipse eclipse = new Eclipse(EclipseKind.PARTIAL, EclipseKind.TOTAL, EclipseKind.RING);

    private Radiation radiation = new Radiation();

    private SunPhase phase = new SunPhase();

    /**
     * Returns the astro dawn range.
     */
    public Range getAstroDawn() {
        return ranges.get(SunPhaseName.ASTRO_DAWN);
    }

    /**
     * Sets the astro dawn range.
     */
    public void setAstroDawn(Range astroDawn) {
        ranges.put(SunPhaseName.ASTRO_DAWN, astroDawn);
    }

    /**
     * Returns the nautic dawn range.
     */
    public Range getNauticDawn() {
        return ranges.get(SunPhaseName.NAUTIC_DAWN);
    }

    /**
     * Sets the nautic dawn range.
     */
    public void setNauticDawn(Range nauticDawn) {
        ranges.put(SunPhaseName.NAUTIC_DAWN, nauticDawn);
    }

    /**
     * Returns the civil dawn range.
     */
    public Range getCivilDawn() {
        return ranges.get(SunPhaseName.CIVIL_DAWN);
    }

    /**
     * Sets the civil dawn range.
     */
    public void setCivilDawn(Range civilDawn) {
        ranges.put(SunPhaseName.CIVIL_DAWN, civilDawn);
    }

    /**
     * Returns the civil dusk range.
     */
    public Range getCivilDusk() {
        return ranges.get(SunPhaseName.CIVIL_DUSK);
    }

    /**
     * Sets the civil dusk range.
     */
    public void setCivilDusk(Range civilDusk) {
        ranges.put(SunPhaseName.CIVIL_DUSK, civilDusk);
    }

    /**
     * Returns the nautic dusk range.
     */
    public Range getNauticDusk() {
        return ranges.get(SunPhaseName.NAUTIC_DUSK);
    }

    /**
     * Sets the nautic dusk range.
     */
    public void setNauticDusk(Range nauticDusk) {
        ranges.put(SunPhaseName.NAUTIC_DUSK, nauticDusk);
    }

    /**
     * Returns the astro dusk range.
     */
    public Range getAstroDusk() {
        return ranges.get(SunPhaseName.ASTRO_DUSK);
    }

    /**
     * Sets the astro dusk range.
     */
    public void setAstroDusk(Range astroDusk) {
        ranges.put(SunPhaseName.ASTRO_DUSK, astroDusk);
    }

    /**
     * Returns the noon range, start and end is always equal.
     */
    public Range getNoon() {
        return ranges.get(SunPhaseName.NOON);
    }

    /**
     * Sets the noon range.
     */
    public void setNoon(Range noon) {
        ranges.put(SunPhaseName.NOON, noon);
    }

    /**
     * Returns the daylight range.
     */
    public Range getDaylight() {
        return ranges.get(SunPhaseName.DAYLIGHT);
    }

    /**
     * Sets the daylight range.
     */
    public void setDaylight(Range daylight) {
        ranges.put(SunPhaseName.DAYLIGHT, daylight);
    }

    /**
     * Returns the morning night range.
     */
    public Range getMorningNight() {
        return ranges.get(SunPhaseName.MORNING_NIGHT);
    }

    /**
     * Sets the morning night range.
     */
    public void setMorningNight(Range morningNight) {
        ranges.put(SunPhaseName.MORNING_NIGHT, morningNight);
    }

    /**
     * Returns the evening night range.
     */
    public Range getEveningNight() {
        return ranges.get(SunPhaseName.EVENING_NIGHT);
    }

    /**
     * Sets the evening night range.
     */
    public void setEveningNight(Range eveningNight) {
        ranges.put(SunPhaseName.EVENING_NIGHT, eveningNight);
    }

    /**
     * Returns the night range.
     */
    public Range getNight() {
        return ranges.get(SunPhaseName.NIGHT);
    }

    /**
     * Sets the night range.
     */
    public void setNight(Range night) {
        ranges.put(SunPhaseName.NIGHT, night);
    }

    /**
     * Sets the rise range.
     */
    @Override
    public void setRise(Range rise) {
        super.setRise(rise);
        ranges.put(SunPhaseName.SUN_RISE, rise);
    }

    /**
     * Sets the set range.
     */
    @Override
    public void setSet(Range set) {
        super.setSet(set);
        ranges.put(SunPhaseName.SUN_SET, set);
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

    /**
     * Sets the sun position.
     */
    public void setPosition(Position position) {
        this.position = position;
    }

    /**
     * Returns the zodiac.
     */
    public SunZodiac getZodiac() {
        return zodiac;
    }

    /**
     * Sets the zodiac.
     */
    public void setZodiac(SunZodiac zodiac) {
        this.zodiac = zodiac;
    }

    /**
     * Returns the seasons.
     */
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
    public Eclipse getEclipse() {
        return eclipse;
    }

    /**
     * Sets the eclipses.
     */
    public void setEclipse(Eclipse eclipse) {
        this.eclipse = eclipse;
    }

    /**
     * Returns the sun phase.
     */
    public SunPhase getPhase() {
        return phase;
    }

    /**
     * Sets the sun phase.
     */
    public void setPhase(SunPhase phase) {
        this.phase = phase;
    }

    /**
     * Returns all ranges of the sun.
     */
    public Map<SunPhaseName, Range> getAllRanges() {
        return ranges;
    }
}
