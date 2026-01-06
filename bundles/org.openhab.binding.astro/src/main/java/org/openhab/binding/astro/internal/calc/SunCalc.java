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
package org.openhab.binding.astro.internal.calc;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.astro.internal.model.Eclipse;
import org.openhab.binding.astro.internal.model.EclipseType;
import org.openhab.binding.astro.internal.model.Position;
import org.openhab.binding.astro.internal.model.Range;
import org.openhab.binding.astro.internal.model.Season;
import org.openhab.binding.astro.internal.model.Sun;
import org.openhab.binding.astro.internal.model.SunPhaseName;
import org.openhab.binding.astro.internal.util.DateTimeUtils;
import org.openhab.binding.astro.internal.util.MathUtils;

/**
 * Calculates the SunPosition (azimuth, elevation) and Sun data.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Christoph Weitkamp - Introduced UoM
 * @implNote based on the calculations of http://www.suncalc.net
 */
@NonNullByDefault
public class SunCalc {
    private static final double M0 = Math.toRadians(357.5291);
    private static final double M1 = Math.toRadians(0.98560028);
    private static final double J0 = 0.0009;
    private static final double J1 = 0.0053;
    private static final double J2 = -0.0069;
    private static final double C1 = Math.toRadians(1.9148);
    private static final double C2 = Math.toRadians(0.0200);
    private static final double C3 = Math.toRadians(0.0003);
    private static final double P = Math.toRadians(102.9372);
    private static final double E = Math.toRadians(23.45);
    private static final double TH0 = Math.toRadians(280.1600);
    private static final double TH1 = Math.toRadians(360.9856235);
    private static final double SUN_ANGLE = -0.83;
    private static final double SUN_DIAMETER = Math.toRadians(0.53); // sun diameter
    private static final double H0 = Math.toRadians(SUN_ANGLE);
    private static final double H1 = Math.toRadians(-6.0); // nautical twilight angle
    private static final double H2 = Math.toRadians(-12.0); // astronomical twilight angle
    private static final double H3 = Math.toRadians(-18.0); // darkness angle
    private static final int CURVE_TIME_INTERVAL = 20; // 20 minutes
    private static final double JD_ONE_MINUTE_FRACTION = 1.0 / 60 / 24;

    /**
     * Calculates the sun position (azimuth and elevation).
     */
    public void setPositionalInfo(Calendar calendar, double latitude, double longitude, @Nullable Double altitude,
            Sun sun) {
        double lw = Math.toRadians(-longitude);
        double phi = Math.toRadians(latitude);

        double j = DateTimeUtils.dateToJulianDate(calendar);
        double m = getSolarMeanAnomaly(j);
        double c = getEquationOfCenter(m);
        double lsun = getEclipticLongitude(m, c);
        double d = getSunDeclination(lsun);
        double a = getRightAscension(lsun);
        double th = getSiderealTime(j, lw);

        double azimuth = Math.toDegrees(getAzimuth(th, a, phi, d));
        double elevation = Math.toDegrees(getElevation(th, a, phi, d));
        double shadeLength = getShadeLength(elevation);

        Position position = sun.getPosition();
        position.setAzimuth(azimuth + 180);
        position.setElevation(elevation);
        position.setShadeLength(shadeLength);
    }

    /**
     * Returns true, if the sun is up all day (no rise and set).
     */
    private boolean isSunUpAllDay(Calendar calendar, double latitude, double longitude, @Nullable Double altitude) {
        Sun sun = new Sun();
        Calendar start = DateTimeUtils.truncateToMidnight(calendar);
        Calendar cal = (Calendar) start.clone();
        var numberOfSamples = 24 * 60 / CURVE_TIME_INTERVAL;
        for (int i = 0; i <= numberOfSamples; i++) {
            setPositionalInfo(cal, latitude, longitude, altitude, sun);
            if (sun.getPosition().getElevationAsDouble() < SUN_ANGLE) {
                return false;
            }
            cal.add(Calendar.MINUTE, CURVE_TIME_INTERVAL);
        }
        return true;
    }

    /**
     * Calculates all sun rise and sets at the specified coordinates.
     */
    public Sun getSunInfo(Calendar calendar, double latitude, double longitude, @Nullable Double altitude,
            boolean useMeteorologicalSeason, TimeZone zone, Locale locale) {
        return getSunInfo(calendar, latitude, longitude, altitude, false, useMeteorologicalSeason, zone, locale);
    }

    private Sun getSunInfo(Calendar calendar, double latitude, double longitude, @Nullable Double altitude,
            boolean onlyAstro, boolean useMeteorologicalSeason, TimeZone zone, Locale locale) {
        double lw = Math.toRadians(-longitude);
        double phi = Math.toRadians(latitude);
        double j = DateTimeUtils.midnightDateToJulianDate(calendar) + 0.5;
        double n = getJulianCycle(j, lw);
        double js = getApproxSolarTransit(0, lw, n);
        double m = getSolarMeanAnomaly(js);
        double c = getEquationOfCenter(m);
        double lsun = getEclipticLongitude(m, c);
        double d = getSunDeclination(lsun);
        double jtransit = getSolarTransit(js, m, lsun);
        double w0 = getHourAngle(H0, phi, d);
        double w1 = getHourAngle(H0 + SUN_DIAMETER, phi, d);
        double jset = getSunsetJulianDate(w0, m, lsun, lw, n);
        double jsetstart = getSunsetJulianDate(w1, m, lsun, lw, n);
        double jrise = getSunriseJulianDate(jtransit, jset);
        double jriseend = getSunriseJulianDate(jtransit, jsetstart);
        double w2 = getHourAngle(H1, phi, d);
        double jnau = getSunsetJulianDate(w2, m, lsun, lw, n);
        double jciv2 = getSunriseJulianDate(jtransit, jnau);

        double w3 = getHourAngle(H2, phi, d);
        double w4 = getHourAngle(H3, phi, d);
        double jastro = getSunsetJulianDate(w3, m, lsun, lw, n);
        double jdark = getSunsetJulianDate(w4, m, lsun, lw, n);
        double jnau2 = getSunriseJulianDate(jtransit, jastro);
        double jastro2 = getSunriseJulianDate(jtransit, jdark);

        Sun sun = new Sun();
        sun.setAstroDawn(new Range(DateTimeUtils.toCalendar(jastro2, zone, locale),
                DateTimeUtils.toCalendar(jnau2, zone, locale)));
        sun.setAstroDusk(new Range(DateTimeUtils.toCalendar(jastro, zone, locale),
                DateTimeUtils.toCalendar(jdark, zone, locale)));

        if (onlyAstro) {
            return sun;
        }

        sun.setNoon(new Range(DateTimeUtils.toCalendar(jtransit, zone, locale),
                DateTimeUtils.toCalendar(jtransit + JD_ONE_MINUTE_FRACTION, zone, locale)));
        sun.setRise(new Range(DateTimeUtils.toCalendar(jrise, zone, locale),
                DateTimeUtils.toCalendar(jriseend, zone, locale)));
        sun.setSet(new Range(DateTimeUtils.toCalendar(jsetstart, zone, locale),
                DateTimeUtils.toCalendar(jset, zone, locale)));

        sun.setCivilDawn(new Range(DateTimeUtils.toCalendar(jciv2, zone, locale),
                DateTimeUtils.toCalendar(jrise, zone, locale)));
        sun.setCivilDusk(
                new Range(DateTimeUtils.toCalendar(jset, zone, locale), DateTimeUtils.toCalendar(jnau, zone, locale)));

        sun.setNauticDawn(new Range(DateTimeUtils.toCalendar(jnau2, zone, locale),
                DateTimeUtils.toCalendar(jciv2, zone, locale)));
        sun.setNauticDusk(new Range(DateTimeUtils.toCalendar(jnau, zone, locale),
                DateTimeUtils.toCalendar(jastro, zone, locale)));

        boolean isSunUpAllDay = isSunUpAllDay(calendar, latitude, longitude, altitude);

        // daylight
        Range daylightRange = new Range();
        if (sun.getRise().getStart() == null && sun.getRise().getEnd() == null) {
            if (isSunUpAllDay) {
                daylightRange = new Range(DateTimeUtils.truncateToMidnight(calendar),
                        DateTimeUtils.truncateToMidnight(addDays(calendar, 1)));
            }
        } else {
            daylightRange = new Range(sun.getRise().getEnd(), sun.getSet().getStart());
        }
        sun.setDaylight(daylightRange);

        // morning night
        Sun sunYesterday = getSunInfo(addDays(calendar, -1), latitude, longitude, altitude, true,
                useMeteorologicalSeason, zone, locale);
        Range morningNightRange = null;
        Range range, range2;
        if ((range = sunYesterday.getAstroDusk()) != null && range.getEnd() != null
                && DateTimeUtils.isSameDay(range.getEnd(), calendar)) {
            morningNightRange = new Range(range.getEnd(),
                    (range2 = sun.getAstroDawn()) == null ? null : range2.getStart());
        } else if (isSunUpAllDay || (range2 = sun.getAstroDawn()) == null || range2.getStart() == null) {
            morningNightRange = new Range();
        } else {
            morningNightRange = new Range(DateTimeUtils.truncateToMidnight(calendar),
                    (range2 = sun.getAstroDawn()) == null ? null : range2.getStart());
        }
        sun.setMorningNight(morningNightRange);

        // evening night
        Range eveningNightRange = null;
        if ((range = sun.getAstroDusk()) != null && range.getEnd() != null
                && DateTimeUtils.isSameDay(range.getEnd(), calendar)) {
            eveningNightRange = new Range(range.getEnd(), DateTimeUtils.truncateToMidnight(addDays(calendar, 1)));
        } else {
            eveningNightRange = new Range();
        }
        sun.setEveningNight(eveningNightRange);

        // night
        if (isSunUpAllDay) {
            sun.setNight(new Range());
        } else {
            Sun sunTomorrow = getSunInfo(addDays(calendar, 1), latitude, longitude, altitude, true,
                    useMeteorologicalSeason, zone, locale);
            sun.setNight(new Range((range = sun.getAstroDusk()) == null ? null : range.getEnd(),
                    (range2 = sunTomorrow.getAstroDawn()) == null ? null : range2.getStart()));
        }

        // eclipse
        Eclipse eclipse = sun.getEclipse();
        MoonCalc mc = new MoonCalc();

        eclipse.getKinds().forEach(eclipseKind -> {
            double jdate = mc.getEclipse(calendar, EclipseType.SUN, j, eclipseKind);
            Calendar eclipseDate = DateTimeUtils.toCalendar(jdate, zone, locale);
            if (eclipseDate != null) {
                eclipse.set(eclipseKind, eclipseDate, new Position());
            }
        });

        sun.setZodiac(ZodiacCalc.calculate(lsun, calendar.toInstant()));

        Season season = sun.getSeason();
        var year = calendar.get(Calendar.YEAR);
        if (season == null || season.getYear() != year) {
            sun.setSeason(SeasonCalc.calculate(year, latitude, useMeteorologicalSeason, zone));
        }

        // phase
        for (Entry<SunPhaseName, Range> rangeEntry : sortByValue(sun.getAllRanges()).entrySet()) {
            SunPhaseName entryPhase = rangeEntry.getKey();
            if (rangeEntry.getValue().matches(calendar)) {
                if (entryPhase == SunPhaseName.MORNING_NIGHT || entryPhase == SunPhaseName.EVENING_NIGHT) {
                    sun.getPhase().setName(SunPhaseName.NIGHT);
                } else {
                    sun.getPhase().setName(entryPhase);
                }
            }
        }

        return sun;
    }

    /**
     * Adds the specified days to the calendar.
     */
    private Calendar addDays(Calendar calendar, int days) {
        Calendar cal = (Calendar) calendar.clone();
        cal.add(Calendar.DAY_OF_MONTH, days);
        return cal;
    }

    // all the following methods are translated to java based on the javascript
    // calculations of http://www.suncalc.net
    private double getJulianCycle(double j, double lw) {
        return Math.round(j - DateTimeUtils.JD_J2000 - J0 - lw / MathUtils.TWO_PI);
    }

    private double getApproxSolarTransit(double ht, double lw, double n) {
        return DateTimeUtils.JD_J2000 + J0 + (ht + lw) / MathUtils.TWO_PI + n;
    }

    private double getSolarMeanAnomaly(double js) {
        return M0 + M1 * (js - DateTimeUtils.JD_J2000);
    }

    private double getEquationOfCenter(double m) {
        return C1 * Math.sin(m) + C2 * Math.sin(2 * m) + C3 * Math.sin(3 * m);
    }

    private double getEclipticLongitude(double m, double c) {
        return m + P + c + Math.PI;
    }

    private double getSolarTransit(double js, double m, double lsun) {
        return js + (J1 * Math.sin(m)) + (J2 * Math.sin(2 * lsun));
    }

    private double getSunDeclination(double lsun) {
        return Math.asin(Math.sin(lsun) * Math.sin(E));
    }

    private double getRightAscension(double lsun) {
        return Math.atan2(Math.sin(lsun) * Math.cos(E), Math.cos(lsun));
    }

    private double getSiderealTime(double j, double lw) {
        return TH0 + TH1 * (j - DateTimeUtils.JD_J2000) - lw;
    }

    private double getAzimuth(double th, double a, double phi, double d) {
        double h = th - a;
        return Math.atan2(Math.sin(h), Math.cos(h) * Math.sin(phi) - Math.tan(d) * Math.cos(phi));
    }

    private double getElevation(double th, double a, double phi, double d) {
        return Math.asin(Math.sin(phi) * Math.sin(d) + Math.cos(phi) * Math.cos(d) * Math.cos(th - a));
    }

    private double getShadeLength(double elevation) {
        return 1 / MathUtils.tanDeg(elevation);
    }

    private double getHourAngle(double h, double phi, double d) {
        return Math.acos((Math.sin(h) - Math.sin(phi) * Math.sin(d)) / (Math.cos(phi) * Math.cos(d)));
    }

    private double getSunsetJulianDate(double w0, double m, double Lsun, double lw, double n) {
        return getSolarTransit(getApproxSolarTransit(w0, lw, n), m, Lsun);
    }

    private double getSunriseJulianDate(double jtransit, double jset) {
        return jtransit - (jset - jtransit);
    }

    public static Map<SunPhaseName, Range> sortByValue(Map<SunPhaseName, Range> map) {
        List<Entry<SunPhaseName, Range>> list = new ArrayList<>(map.entrySet());

        Collections.sort(list, new Comparator<>() {
            @Override
            public int compare(Entry<SunPhaseName, Range> p1, Entry<SunPhaseName, Range> p2) {
                Range p1Range = p1.getValue();
                Range p2Range = p2.getValue();
                return p1Range.compareTo(p2Range);
            }
        });

        Map<SunPhaseName, Range> result = new LinkedHashMap<>();
        for (Entry<SunPhaseName, Range> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}
