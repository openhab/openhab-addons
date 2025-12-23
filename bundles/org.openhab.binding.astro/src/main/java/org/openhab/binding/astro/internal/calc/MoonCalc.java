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

import static org.openhab.binding.astro.internal.util.MathUtils.*;

<<<<<<< Upstream, based on main
<<<<<<< Upstream, based on main
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.InstantSource;
import java.util.Calendar;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
=======
import java.math.*;
import java.util.*;
>>>>>>> f56c745 Review Moon Distance and factorization of MoonCalc
=======
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
>>>>>>> 3188e3a Correcting import.

import org.eclipse.jdt.annotation.NonNullByDefault;
<<<<<<< Upstream, based on main
<<<<<<< Upstream, based on main
import org.openhab.binding.astro.internal.model.DistanceType;
import org.openhab.binding.astro.internal.model.EclipseSet;
import org.openhab.binding.astro.internal.model.Moon;
import org.openhab.binding.astro.internal.model.MoonPhase;
import org.openhab.binding.astro.internal.model.MoonPhaseName;
import org.openhab.binding.astro.internal.model.MoonPosition;
import org.openhab.binding.astro.internal.model.Range;
<<<<<<< Upstream, based on main
import org.openhab.binding.astro.internal.util.AstroConstants;
=======
=======
import org.openhab.binding.astro.internal.model.*;
>>>>>>> f56c745 Review Moon Distance and factorization of MoonCalc
<<<<<<< Upstream, based on main
>>>>>>> f25b664 Review Moon Distance and factorization of MoonCalc
=======
=======
import org.openhab.binding.astro.internal.model.DistanceType;
import org.openhab.binding.astro.internal.model.Eclipse;
import org.openhab.binding.astro.internal.model.EclipseKind;
import org.openhab.binding.astro.internal.model.EclipseType;
import org.openhab.binding.astro.internal.model.Moon;
import org.openhab.binding.astro.internal.model.MoonPhase;
import org.openhab.binding.astro.internal.model.MoonPhaseName;
import org.openhab.binding.astro.internal.model.Position;
import org.openhab.binding.astro.internal.model.Range;
>>>>>>> 3188e3a Correcting import.
>>>>>>> 046baae Correcting import.
import org.openhab.binding.astro.internal.util.DateTimeUtils;
import org.openhab.binding.astro.internal.util.MathUtils;

/**
 * Calculates the phase, eclipse, rise, set, distance, illumination and age of
 * the moon.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Christoph Weitkamp - Introduced UoM
 * @implNote based on the calculations of
 *           http://www.computus.de/mondphase/mondphase.htm azimuth/elevation and
 *           zodiac based on http://lexikon.astronomie.info/java/sunmoon/
 */
@NonNullByDefault
<<<<<<< Upstream, based on main
public class MoonCalc extends AstroCalc {
    private static final double FL = 1.0 - AstroConstants.WGS84_EARTH_FLATTENING;
    private static final EclipseCalc ECLIPSE_CALC = new MoonEclipseCalc();

    private final InstantSource instantSource;

    /**
     * Creates a new instance using the specified {@link InstantSource}.
     *
     * @param instantSource the source of the current time.
     */
    public MoonCalc(InstantSource instantSource) {
        this.instantSource = instantSource;
    }
=======
public class MoonCalc {
>>>>>>> 810a1e9 Initial commit for Moon phase revamp

    /**
     * Calculates all moon data at the specified coordinates
     */
    public Moon getMoonInfo(Calendar calendar, double latitude, double longitude, TimeZone zone, Locale locale) {
        Moon moon = new Moon();

        double[] riseSet = getRiseSet(calendar, latitude, longitude);
        Calendar rise = DateTimeUtils.timeToCalendar(calendar, riseSet[0]);
        Calendar set = DateTimeUtils.timeToCalendar(calendar, riseSet[1]);

        if (rise == null || set == null) {
            Calendar tomorrow = (Calendar) calendar.clone();
            tomorrow.add(Calendar.DAY_OF_MONTH, 1);

            double[] riseSeTomorrow = getRiseSet(tomorrow, latitude, longitude);
            if (rise == null) {
                rise = DateTimeUtils.timeToCalendar(tomorrow, riseSeTomorrow[0]);
            }
            if (set == null) {
                set = DateTimeUtils.timeToCalendar(tomorrow, riseSeTomorrow[1]);
            }
        }

        moon.setRise(new Range(rise, rise));
        moon.setSet(new Range(set, set));

        MoonPhase phase = moon.getPhase();
<<<<<<< Upstream, based on main
        double julianDateMidnight = DateTimeUtils.midnightDateToJulianDate(calendar);
        phase.remarkablePhases().forEach(mp -> phase.setPhase(mp,
                DateTimeUtils.toCalendar(getPhase(julianDateMidnight, mp, true), zone, locale)));
=======
        phase.setNew(
                DateTimeUtils.toCalendar(getNextPhase(calendar, julianDateMidnight, MoonPhaseName.NEW), zone, locale));
        phase.setFirstQuarter(DateTimeUtils
                .toCalendar(getNextPhase(calendar, julianDateMidnight, MoonPhaseName.FIRST_QUARTER), zone, locale));
        phase.setFull(
                DateTimeUtils.toCalendar(getNextPhase(calendar, julianDateMidnight, MoonPhaseName.FULL), zone, locale));
        phase.setThirdQuarter(DateTimeUtils
                .toCalendar(getNextPhase(calendar, julianDateMidnight, MoonPhaseName.THIRD_QUARTER), zone, locale));
>>>>>>> 810a1e9 Initial commit for Moon phase revamp

        double julianDate = DateTimeUtils.dateToJulianDate(calendar);

        if (moon.getEclipseSet().needsRecalc(julianDate)) {
            moon.setEclipseSet(new EclipseSet(ECLIPSE_CALC.getNextEclipses(julianDate).stream()
                    .map(eclipse -> eclipse.withPosition(getMoonPosition(eclipse.when(), latitude, longitude)))));
        }

        Set.of(DistanceType.APOGEE, DistanceType.PERIGEE)
                .forEach(type -> moon.setDistance(type, MoonDistanceCalc.get(type, julianDate)));

        return moon;
    }

    /**
     * Calculates the moon illumination and distance.
     */
    public void setPositionalInfo(Calendar calendar, double latitude, double longitude, Moon moon, TimeZone zone,
            Locale locale) {
        setMoonPhase(calendar, moon, zone, locale);

        double julianDate = DateTimeUtils.dateToJulianDate(calendar);
        MoonPosition moonPosition = getMoonPosition(julianDate, latitude, longitude);
        moon.setPosition(moonPosition);
        moon.setZodiac(ZodiacCalc.calculate(moonPosition.getLongitude(), null));
        moon.setDistance(DistanceType.CURRENT, MoonDistanceCalc.calculate(julianDate));
    }

    /**
     * Calculates the age and the current phase.
     */
    private void setMoonPhase(Calendar calendar, Moon moon, TimeZone zone, Locale locale) {
        MoonPhase phase = moon.getPhase();
        double julianDate = DateTimeUtils.dateToJulianDate(calendar);
<<<<<<< Upstream, based on main
        double parentNewMoon = getPhase(julianDate, MoonPhaseName.NEW, false);
=======
        double parentNewMoon = getPreviousPhase(calendar, julianDate, MoonPhaseName.NEW);
>>>>>>> 810a1e9 Initial commit for Moon phase revamp
        double age = Math.abs(parentNewMoon - julianDate);
        Calendar parentNewMoonCal = DateTimeUtils.toCalendar(parentNewMoon, zone, locale);
        if (parentNewMoonCal == null) {
            return;
        }
        phase.setAge(age);

        long parentNewMoonMillis = parentNewMoonCal.getTimeInMillis();
        Calendar cal = phase.getNew();
        if (cal == null) {
            return;
        }
        long ageRangeTimeMillis = cal.getTimeInMillis() - parentNewMoonMillis;
        long ageCurrentMillis = instantSource.millis() - parentNewMoonMillis;
        double agePercent = ageRangeTimeMillis != 0 ? ageCurrentMillis * 100.0 / ageRangeTimeMillis : 0;
        phase.setAgePercent(agePercent);
        phase.setAgeDegree(3.6 * agePercent);
        double illumination = getIllumination(julianDate);
        phase.setIllumination(illumination);

        Optional<MoonPhaseName> remarkablePhase = phase.remarkablePhases()
                .filter(p -> DateTimeUtils.isSameDay(calendar, phase.getPhaseDate(p))).findFirst();
        phase.setName(remarkablePhase.orElse(MoonPhaseName.fromAgePercent(agePercent / 100)));
    }

    /**
     * Calculates moonrise and moonset.
     */
    private double[] getRiseSet(Calendar calendar, double latitude, double longitude) {
        double lambda = prepareCoordinate(longitude, 180);
        if (longitude > 0) {
            lambda *= -1;
        }
        double phi = prepareCoordinate(latitude, 90);
        if (latitude < 0) {
            phi *= -1;
        }

        double moonJd = Math.floor(DateTimeUtils.midnightDateToJulianDate(calendar)) - 2400000.0;
        moonJd -= ((calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)) / 60000.0) / 1440.0;

        double sphi = sinDeg(phi);
        double cphi = cosDeg(phi);
        double sinho = sinDeg(8.0 / 60.0);

        int hour = 1;
        double utrise = -1;
        double utset = -1;
        do {
            double yminus = sinAlt(moonJd, hour - 1, lambda, cphi, sphi) - sinho;
            double yo = sinAlt(moonJd, hour, lambda, cphi, sphi) - sinho;
            double yplus = sinAlt(moonJd, hour + 1, lambda, cphi, sphi) - sinho;
            double[] quadRet = quad(yminus, yo, yplus);
            if (quadRet[3] == 1) {
                if (yminus < 0) {
                    utrise = hour + quadRet[1];
                } else {
                    utset = hour + quadRet[1];
                }
            } else if (quadRet[3] == 2) {
                if (quadRet[0] < 0) {
                    utrise = hour + quadRet[2];
                    utset = hour + quadRet[1];
                } else {
                    utrise = hour + quadRet[1];
                    utset = hour + quadRet[2];
                }
            }
            yminus = yplus;
            hour += 2;
        } while (hour < 25 && (utrise == -1 || utset == -1));

        double rise = prepareTime(utrise);
        double set = prepareTime(utset);

        return new double[] { rise, set };
    }

    /**
     * Prepares the coordinate for moonrise and moonset calculation.
     */
    private double prepareCoordinate(double coordinate, double system) {
        double c = Math.abs(coordinate);

        if (c - Math.floor(c) >= .599) {
            c = Math.floor(c) + (c - Math.floor(c)) / 1 * .6;
        }
        if (c > system) {
            c = Math.floor(c) % system + (c - Math.floor(c));
        }
        return Math.round(c * 100.0) / 100.0;
    }

    /**
     * Prepares a time value for converting to a calendar object.
     */
    private double prepareTime(double riseSet) {
        if (riseSet == -1) {
            return riseSet;
        }
        double riseMinute = (riseSet - Math.floor(riseSet)) * 60.0 / 100.0;
        double rounded;
        if (riseMinute >= .595) {
            riseMinute = 0;
            rounded = riseSet + 1;
        } else {
            rounded = riseSet;
        }
        rounded = Math.floor(rounded) + riseMinute;

        BigDecimal bd = new BigDecimal(Double.toString(rounded));
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Calculates the moon phase.
     */
    private double calcMoonPhase(double k, MoonPhaseName phase) {
<<<<<<< Upstream, based on main
        double kMod = Math.floor(k) + phase.cycleProgress;
=======
        if (Double.isNaN(phase.mode)) {
            throw new IllegalArgumentException("calcMoonPhase called for unhandled phase: %s".formatted(phase.name()));
        }
        double kMod = Math.floor(k) + phase.mode;
>>>>>>> 810a1e9 Initial commit for Moon phase revamp
        double t = kMod / 1236.85;
<<<<<<< Upstream, based on main
        double e = varE(t);
        double m = varM(kMod, t);
        double m1 = varM1(kMod, t);
        double f = varF(kMod, t);
        double o = varO(kMod, t);
        double jd = varJde(kMod, t);
<<<<<<< Upstream, based on main
        switch (phase) {
            case NEW:
                jd += -.4072 * sinDeg(m1) + .17241 * e * sinDeg(m) + .01608 * sinDeg(2 * m1) + .01039 * sinDeg(2 * f)
                        + .00739 * e * sinDeg(m1 - m) - .00514 * e * sinDeg(m1 + m) + .00208 * e * e * sinDeg(2 * m)
                        - .00111 * sinDeg(m1 - 2 * f) - .00057 * sinDeg(m1 + 2 * f);
                jd += .00056 * e * sinDeg(2 * m1 + m) - .00042 * sinDeg(3 * m1) + .00042 * e * sinDeg(m + 2 * f)
                        + .00038 * e * sinDeg(m - 2 * f) - .00024 * e * sinDeg(2 * m1 - m) - .00017 * sinDeg(o)
                        - .00007 * sinDeg(m1 + 2 * m) + .00004 * sinDeg(2 * m1 - 2 * f);
                jd += .00004 * sinDeg(3 * m) + .00003 * sinDeg(m1 + m - 2 * f) + .00003 * sinDeg(2 * m1 + 2 * f)
                        - .00003 * sinDeg(m1 + m + 2 * f) + .00003 * sinDeg(m1 - m + 2 * f)
                        - .00002 * sinDeg(m1 - m - 2 * f) - .00002 * sinDeg(3 * m1 + m);
                jd += .00002 * sinDeg(4 * m1);
                break;
            case FULL:
                jd += -.40614 * sinDeg(m1) + .17302 * e * sinDeg(m) + .01614 * sinDeg(2 * m1) + .01043 * sinDeg(2 * f)
                        + .00734 * e * sinDeg(m1 - m) - .00515 * e * sinDeg(m1 + m) + .00209 * e * e * sinDeg(2 * m)
                        - .00111 * sinDeg(m1 - 2 * f) - .00057 * sinDeg(m1 + 2 * f);
                jd += .00056 * e * sinDeg(2 * m1 + m) - .00042 * sinDeg(3 * m1) + .00042 * e * sinDeg(m + 2 * f)
                        + .00038 * e * sinDeg(m - 2 * f) - .00024 * e * sinDeg(2 * m1 - m) - .00017 * sinDeg(o)
                        - .00007 * sinDeg(m1 + 2 * m) + .00004 * sinDeg(2 * m1 - 2 * f);
                jd += .00004 * sinDeg(3 * m) + .00003 * sinDeg(m1 + m - 2 * f) + .00003 * sinDeg(2 * m1 + 2 * f)
                        - .00003 * sinDeg(m1 + m + 2 * f) + .00003 * sinDeg(m1 - m + 2 * f)
                        - .00002 * sinDeg(m1 - m - 2 * f) - .00002 * sinDeg(3 * m1 + m);
                jd += .00002 * sinDeg(4 * m1);
                break;
            default:
                jd += -.62801 * sinDeg(m1) + .17172 * e * sinDeg(m) - .01183 * e * sinDeg(m1 + m)
                        + .00862 * sinDeg(2 * m1) + .00804 * sinDeg(2 * f) + .00454 * e * sinDeg(m1 - m)
                        + .00204 * e * e * sinDeg(2 * m) - .0018 * sinDeg(m1 - 2 * f) - .0007 * sinDeg(m1 + 2 * f);
                jd += -.0004 * sinDeg(3 * m1) - .00034 * e * sinDeg(2 * m1 - m) + .00032 * e * sinDeg(m + 2 * f)
                        + .00032 * e * sinDeg(m - 2 * f) - .00028 * e * e * sinDeg(m1 + 2 * m)
                        + .00027 * e * sinDeg(2 * m1 + m) - .00017 * sinDeg(o);
                jd += -.00005 * sinDeg(m1 - m - 2 * f) + .00004 * sinDeg(2 * m1 + 2 * f)
                        - .00004 * sinDeg(m1 + m + 2 * f) + .00004 * sinDeg(m1 - 2 * m)
                        + .00003 * sinDeg(m1 + m - 2 * f) + .00003 * sinDeg(3 * m) + .00002 * sinDeg(2 * m1 - 2 * f);
                jd += .00002 * sinDeg(m1 - m + 2 * f) - .00002 * sinDeg(3 * m1 + m);
                double w = .00306 - .00038 * e * cosDeg(m) + .00026 * cosDeg(m1) - .00002 * cosDeg(m1 - m)
                        + .00002 * cosDeg(m1 + m) + .00002 * cosDeg(2 * f);
                jd += MoonPhaseName.FIRST_QUARTER.equals(phase) ? w : -w;
=======
        if (mode == NEW_MOON) {
            jd += -.4072 * sinDeg(m1) + .17241 * e * sinDeg(m) + .01608 * sinDeg(2 * m1) + .01039 * sinDeg(2 * f)
                    + .00739 * e * sinDeg(m1 - m) - .00514 * e * sinDeg(m1 + m) + .00208 * e * e * sinDeg(2 * m)
                    - .00111 * sinDeg(m1 - 2 * f) - .00057 * sinDeg(m1 + 2 * f);
            jd += .00056 * e * sinDeg(2 * m1 + m) - .00042 * sinDeg(3 * m1) + .00042 * e * sinDeg(m + 2 * f)
                    + .00038 * e * sinDeg(m - 2 * f) - .00024 * e * sinDeg(2 * m1 - m) - .00017 * sinDeg(o)
                    - .00007 * sinDeg(m1 + 2 * m) + .00004 * sinDeg(2 * m1 - 2 * f);
            jd += .00004 * sinDeg(3 * m) + .00003 * sinDeg(m1 + m - 2 * f) + .00003 * sinDeg(2 * m1 + 2 * f)
                    - .00003 * sinDeg(m1 + m + 2 * f) + .00003 * sinDeg(m1 - m + 2 * f)
                    - .00002 * sinDeg(m1 - m - 2 * f) - .00002 * sinDeg(3 * m1 + m);
            jd += .00002 * sinDeg(4 * m1);
        } else if (mode == FULL_MOON) {
            jd += -.40614 * sinDeg(m1) + .17302 * e * sinDeg(m) + .01614 * sinDeg(2 * m1) + .01043 * sinDeg(2 * f)
                    + .00734 * e * sinDeg(m1 - m) - .00515 * e * sinDeg(m1 + m) + .00209 * e * e * sinDeg(2 * m)
                    - .00111 * sinDeg(m1 - 2 * f) - .00057 * sinDeg(m1 + 2 * f);
            jd += .00056 * e * sinDeg(2 * m1 + m) - .00042 * sinDeg(3 * m1) + .00042 * e * sinDeg(m + 2 * f)
                    + .00038 * e * sinDeg(m - 2 * f) - .00024 * e * sinDeg(2 * m1 - m) - .00017 * sinDeg(o)
                    - .00007 * sinDeg(m1 + 2 * m) + .00004 * sinDeg(2 * m1 - 2 * f);
            jd += .00004 * sinDeg(3 * m) + .00003 * sinDeg(m1 + m - 2 * f) + .00003 * sinDeg(2 * m1 + 2 * f)
                    - .00003 * sinDeg(m1 + m + 2 * f) + .00003 * sinDeg(m1 - m + 2 * f)
                    - .00002 * sinDeg(m1 - m - 2 * f) - .00002 * sinDeg(3 * m1 + m);
            jd += .00002 * sinDeg(4 * m1);
        } else {
            jd += -.62801 * sinDeg(m1) + .17172 * e * sinDeg(m) - .01183 * e * sinDeg(m1 + m) + .00862 * sinDeg(2 * m1)
                    + .00804 * sinDeg(2 * f) + .00454 * e * sinDeg(m1 - m) + .00204 * e * e * sinDeg(2 * m)
                    - .0018 * sinDeg(m1 - 2 * f) - .0007 * sinDeg(m1 + 2 * f);
            jd += -.0004 * sinDeg(3 * m1) - .00034 * e * sinDeg(2 * m1 - m) + .00032 * e * sinDeg(m + 2 * f)
                    + .00032 * e * sinDeg(m - 2 * f) - .00028 * e * e * sinDeg(m1 + 2 * m)
                    + .00027 * e * sinDeg(2 * m1 + m) - .00017 * sinDeg(o);
            jd += -.00005 * sinDeg(m1 - m - 2 * f) + .00004 * sinDeg(2 * m1 + 2 * f) - .00004 * sinDeg(m1 + m + 2 * f)
                    + .00004 * sinDeg(m1 - 2 * m) + .00003 * sinDeg(m1 + m - 2 * f) + .00003 * sinDeg(3 * m)
                    + .00002 * sinDeg(2 * m1 - 2 * f);
            jd += .00002 * sinDeg(m1 - m + 2 * f) - .00002 * sinDeg(3 * m1 + m);
            double w = .00306 - .00038 * e * cosDeg(m) + .00026 * cosDeg(m1) - .00002 * cosDeg(m1 - m)
                    + .00002 * cosDeg(m1 + m) + .00002 * cosDeg(2 * f);
            jd += (mode == FIRST_QUARTER) ? w : -w;
=======
        double e = var_e(t);
        double m = var_m(kMod, t);
        double m1 = var_m1(kMod, t);
        double f = var_f(kMod, t);
        double o = var_o(kMod, t);
        double jd = var_jde(kMod, t);
        switch (phase) {
            case NEW:
                jd += -.4072 * sinDeg(m1) + .17241 * e * sinDeg(m) + .01608 * sinDeg(2 * m1) + .01039 * sinDeg(2 * f)
                        + .00739 * e * sinDeg(m1 - m) - .00514 * e * sinDeg(m1 + m) + .00208 * e * e * sinDeg(2 * m)
                        - .00111 * sinDeg(m1 - 2 * f) - .00057 * sinDeg(m1 + 2 * f);
                jd += .00056 * e * sinDeg(2 * m1 + m) - .00042 * sinDeg(3 * m1) + .00042 * e * sinDeg(m + 2 * f)
                        + .00038 * e * sinDeg(m - 2 * f) - .00024 * e * sinDeg(2 * m1 - m) - .00017 * sinDeg(o)
                        - .00007 * sinDeg(m1 + 2 * m) + .00004 * sinDeg(2 * m1 - 2 * f);
                jd += .00004 * sinDeg(3 * m) + .00003 * sinDeg(m1 + m - 2 * f) + .00003 * sinDeg(2 * m1 + 2 * f)
                        - .00003 * sinDeg(m1 + m + 2 * f) + .00003 * sinDeg(m1 - m + 2 * f)
                        - .00002 * sinDeg(m1 - m - 2 * f) - .00002 * sinDeg(3 * m1 + m);
                jd += .00002 * sinDeg(4 * m1);
                break;
            case FULL:
                jd += -.40614 * sinDeg(m1) + .17302 * e * sinDeg(m) + .01614 * sinDeg(2 * m1) + .01043 * sinDeg(2 * f)
                        + .00734 * e * sinDeg(m1 - m) - .00515 * e * sinDeg(m1 + m) + .00209 * e * e * sinDeg(2 * m)
                        - .00111 * sinDeg(m1 - 2 * f) - .00057 * sinDeg(m1 + 2 * f);
                jd += .00056 * e * sinDeg(2 * m1 + m) - .00042 * sinDeg(3 * m1) + .00042 * e * sinDeg(m + 2 * f)
                        + .00038 * e * sinDeg(m - 2 * f) - .00024 * e * sinDeg(2 * m1 - m) - .00017 * sinDeg(o)
                        - .00007 * sinDeg(m1 + 2 * m) + .00004 * sinDeg(2 * m1 - 2 * f);
                jd += .00004 * sinDeg(3 * m) + .00003 * sinDeg(m1 + m - 2 * f) + .00003 * sinDeg(2 * m1 + 2 * f)
                        - .00003 * sinDeg(m1 + m + 2 * f) + .00003 * sinDeg(m1 - m + 2 * f)
                        - .00002 * sinDeg(m1 - m - 2 * f) - .00002 * sinDeg(3 * m1 + m);
                jd += .00002 * sinDeg(4 * m1);
                break;
            default:
                jd += -.62801 * sinDeg(m1) + .17172 * e * sinDeg(m) - .01183 * e * sinDeg(m1 + m)
                        + .00862 * sinDeg(2 * m1) + .00804 * sinDeg(2 * f) + .00454 * e * sinDeg(m1 - m)
                        + .00204 * e * e * sinDeg(2 * m) - .0018 * sinDeg(m1 - 2 * f) - .0007 * sinDeg(m1 + 2 * f);
                jd += -.0004 * sinDeg(3 * m1) - .00034 * e * sinDeg(2 * m1 - m) + .00032 * e * sinDeg(m + 2 * f)
                        + .00032 * e * sinDeg(m - 2 * f) - .00028 * e * e * sinDeg(m1 + 2 * m)
                        + .00027 * e * sinDeg(2 * m1 + m) - .00017 * sinDeg(o);
                jd += -.00005 * sinDeg(m1 - m - 2 * f) + .00004 * sinDeg(2 * m1 + 2 * f)
                        - .00004 * sinDeg(m1 + m + 2 * f) + .00004 * sinDeg(m1 - 2 * m)
                        + .00003 * sinDeg(m1 + m - 2 * f) + .00003 * sinDeg(3 * m) + .00002 * sinDeg(2 * m1 - 2 * f);
                jd += .00002 * sinDeg(m1 - m + 2 * f) - .00002 * sinDeg(3 * m1 + m);
                double w = .00306 - .00038 * e * cosDeg(m) + .00026 * cosDeg(m1) - .00002 * cosDeg(m1 - m)
                        + .00002 * cosDeg(m1 + m) + .00002 * cosDeg(2 * f);
                jd += MoonPhaseName.FIRST_QUARTER.equals(phase) ? w : -w;
>>>>>>> 24ede3e Initial commit for Moon phase revamp
>>>>>>> 810a1e9 Initial commit for Moon phase revamp
        }
        return moonCorrection(jd, t, kMod);
    }

    /**
     * Calculates the illumination.
     */
    private double getIllumination(double jd) {
        double t = DateTimeUtils.toJulianCenturies(jd);
<<<<<<< Upstream, based on main
        double t2 = t * t;
        double t3 = t2 * t;
        double t4 = t3 * t;
        double d = 297.8502042 + 445267.11151686 * t - .00163 * t2 + t3 / 545868 - t4 / 113065000;
        double m = AstroConstants.E05_0 + 35999.0502909 * t - .0001536 * t2 + t3 / 24490000;
        double m1 = 134.9634114 + 477198.8676313 * t + .008997 * t2 + t3 / 69699 - t4 / 14712000;
=======
        double d = 297.8502042 + 445267.11151686 * t - .00163 * t * t + t * t * t / 545868 - t * t * t * t / 113065000;
        double m = 357.5291092 + 35999.0502909 * t - .0001536 * t * t + t * t * t / 24490000;
        double m1 = 134.9634114 + 477198.8676313 * t + .008997 * t * t + t * t * t / 69699 - t * t * t * t / 14712000;
>>>>>>> 810a1e9 Initial commit for Moon phase revamp
        double i = 180 - d - 6.289 * sinDeg(m1) + 2.1 * sinDeg(m) - 1.274 * sinDeg(2 * d - m1) - .658 * sinDeg(2 * d)
                - .241 * sinDeg(2 * m1) - .110 * sinDeg(d);
        return (1 + cosDeg(i)) / 2 * 100.0;
    }

    /**
     * Searches the next moon phase in a given direction
     */
<<<<<<< Upstream, based on main
    private double getPhase(double jd, MoonPhaseName phase, boolean forward) {
=======
    private double getNextPhase(Calendar cal, double midnightJd, MoonPhaseName phase) {
>>>>>>> 810a1e9 Initial commit for Moon phase revamp
        double tz = 0;
        double phaseJd = 0;
        do {
<<<<<<< Upstream, based on main
            double k = varK(jd, tz);
            tz += forward ? 1 : -1;
            phaseJd = calcMoonPhase(k, phase);
        } while (forward ? phaseJd <= jd : phaseJd > jd);
=======
            double k = varK(cal, tz);
            tz += 1;
            phaseJd = calcMoonPhase(k, phase);
        } while (phaseJd <= midnightJd);
>>>>>>> 810a1e9 Initial commit for Moon phase revamp
        return phaseJd;
    }

<<<<<<< Upstream, based on main
=======
    /**
     * Calculates the previous moon phase.
     */
    private double getPreviousPhase(Calendar cal, double jd, MoonPhaseName phase) {
        double tz = 0;
        double phaseJd = 0;
        do {
            double k = varK(cal, tz);
            tz -= 1;
            phaseJd = calcMoonPhase(k, phase);
        } while (phaseJd > jd);
        return phaseJd;
    }

    /**
     * Calculates the next eclipse.
     */
    protected double getEclipse(Calendar cal, EclipseType type, double midnightJd, EclipseKind eclipse) {
        double tz = 0;
        double eclipseJd = 0;
        do {
            double k = varK(cal, tz);
            tz += 1;
            eclipseJd = getEclipse(k, type, eclipse);
        } while (eclipseJd <= midnightJd);
        return eclipseJd;
    }

<<<<<<< Upstream, based on moon_distance
=======
    /**
     * Calculates the date, where the moon is furthest away from the earth.
     */
    private double getApogee(double julianDate, double decimalYear) {
        double k = Math.floor((decimalYear - 1999.97) * 13.2555) + .5;
        double jd = 0;
        do {
            double t = k / 1325.55;
            double d = 171.9179 + 335.9106046 * k - .010025 * t * t - .00001156 * t * t * t
                    + .000000055 * t * t * t * t;
            double m = 347.3477 + 27.1577721 * k - .0008323 * t * t - .000001 * t * t * t;
            double f = 316.6109 + 364.5287911 * k - .0125131 * t * t - .0000148 * t * t * t;
            jd = 2451534.6698 + 27.55454988 * k - .0006886 * t * t - .000001098 * t * t * t + .0000000052 * t * t
                    + .4392 * sinDeg(2 * d) + .0684 * sinDeg(4 * d) + (.0456 - .00011 * t) * sinDeg(m)
                    + (.0426 - .00011 * t) * sinDeg(2 * d - m) + .0212 * sinDeg(2 * f);
            jd += -.0189 * sinDeg(d) + .0144 * sinDeg(6 * d) + .0113 * sinDeg(4 * d - m) + .0047 * sinDeg(2 * d + 2 * f)
                    + .0036 * sinDeg(d + m) + .0035 * sinDeg(8 * d) + .0034 * sinDeg(6 * d - m)
                    - .0034 * sinDeg(2 * d - 2 * f) + .0022 * sinDeg(2 * d - 2 * m) - .0017 * sinDeg(3 * d);
            jd += .0013 * sinDeg(4 * d + 2 * f) + .0011 * sinDeg(8 * d - m) + .001 * sinDeg(4 * d - 2 * m)
                    + .0009 * sinDeg(10 * d) + .0007 * sinDeg(3 * d + m) + .0006 * sinDeg(2 * m)
                    + .0005 * sinDeg(2 * d + m) + .0005 * sinDeg(2 * d + 2 * m) + .0004 * sinDeg(6 * d + 2 * f);
            jd += .0004 * sinDeg(6 * d - 2 * m) + .0004 * sinDeg(10 * d - m) - .0004 * sinDeg(5 * d)
                    - .0004 * sinDeg(4 * d - 2 * f) + .0003 * sinDeg(2 * f + m) + .0003 * sinDeg(12 * d)
                    + .0003 * sinDeg(2 * d + 2 * f - m) - .0003 * sinDeg(d - m);
            k += 1;
        } while (jd < julianDate);
        return jd;
    }

    /**
     * Calculates the date, where the moon is closest to the earth.
     */
    private double getPerigee(double julianDate, double decimalYear) {
        double k = Math.floor((decimalYear - 1999.97) * 13.2555);
        double jd = 0;
        do {
            double t = k / 1325.55;
            double d = 171.9179 + 335.9106046 * k - .010025 * t * t - .00001156 * t * t * t
                    + .000000055 * t * t * t * t;
            double m = 347.3477 + 27.1577721 * k - .0008323 * t * t - .000001 * t * t * t;
            double f = 316.6109 + 364.5287911 * k - .0125131 * t * t - .0000148 * t * t * t;
            jd = 2451534.6698 + 27.55454988 * k - .0006886 * t * t - .000001098 * t * t * t + .0000000052 * t * t
                    - 1.6769 * sinDeg(2 * d) + .4589 * sinDeg(4 * d) - .1856 * sinDeg(6 * d) + .0883 * sinDeg(8 * d);
            jd += -(.0773 + .00019 * t) * sinDeg(2 * d - m) + (.0502 - .00013 * t) * sinDeg(m) - .046 * sinDeg(10 * d)
                    + (.0422 - .00011 * t) * sinDeg(4 * d - m) - .0256 * sinDeg(6 * d - m) + .0253 * sinDeg(12 * d)
                    + .0237 * sinDeg(d);
            jd += .0162 * sinDeg(8 * d - m) - .0145 * sinDeg(14 * d) + .0129 * sinDeg(2 * f) - .0112 * sinDeg(3 * d)
                    - .0104 * sinDeg(10 * d - m) + .0086 * sinDeg(16 * d) + .0069 * sinDeg(12 * d - m)
                    + .0066 * sinDeg(5 * d) - .0053 * sinDeg(2 * d + 2 * f);
            jd += -.0052 * sinDeg(18 * d) - .0046 * sinDeg(14 * d - m) - .0041 * sinDeg(7 * d)
                    + .004 * sinDeg(2 * d + m) + .0032 * sinDeg(20 * d) - .0032 * sinDeg(d + m)
                    + .0031 * sinDeg(16 * d - m);
            jd += -.0029 * sinDeg(4 * d + m) - .0027 * sinDeg(2 * d - 2 * m) + .0024 * sinDeg(4 * d - 2 * m)
                    - .0021 * sinDeg(6 * d - 2 * m) - .0021 * sinDeg(22 * d) - .0021 * sinDeg(18 * d - m);
            jd += .0019 * sinDeg(6 * d + m) - .0018 * sinDeg(11 * d) - .0014 * sinDeg(8 * d + m)
                    - .0014 * sinDeg(4 * d - 2 * f) - .0014 * sinDeg(6 * d - 2 * f) + .0014 * sinDeg(3 * d + m)
                    - .0014 * sinDeg(5 * d + m) + .0013 * sinDeg(13 * d);
            jd += .0013 * sinDeg(20 * d - m) + .0011 * sinDeg(3 * d + 2 * m) - .0011 * sinDeg(4 * d + 2 * f - 2 * m)
                    - .001 * sinDeg(d + 2 * m) - .0009 * sinDeg(22 * d - m) - .0008 * sinDeg(4 * f)
                    + .0008 * sinDeg(6 * d - 2 * f) + .0008 * sinDeg(2 * d - 2 * f + m);
            jd += .0007 * sinDeg(2 * m) + .0007 * sinDeg(2 * f - m) + .0007 * sinDeg(2 * d + 4 * f)
                    - .0006 * sinDeg(2 * f - 2 * m) - .0006 * sinDeg(2 * d - 2 * f + 2 * m) + .0006 * sinDeg(24 * d)
                    + .0005 * sinDeg(4 * d - 4 * f) + .0005 * sinDeg(2 * d + 2 * m) - .0004 * sinDeg(d - m)
                    + .0027 * sinDeg(9 * d) + .0027 * sinDeg(4 * d + 2 * f);
            k += 1;
        } while (jd < julianDate);
        return jd;
    }

    /**
     * Calculates the distance from the moon to earth.
     */
    private double getDistance(double jd) {
        double t = DateTimeUtils.toJulianCenturies(jd);
        double d = 297.8502042 + 445267.11151686 * t - .00163 * t * t + t * t * t / 545868 - t * t * t * t / 113065000;
        double m = 357.5291092 + 35999.0502909 * t - .0001536 * t * t + t * t * t / 24490000;
        double m1 = 134.9634114 + 477198.8676313 * t + .008997 * t * t + t * t * t / 69699 - t * t * t * t / 14712000;
        double f = 93.27209929999999 + 483202.0175273 * t - .0034029 * t * t - t * t * t / 3526000
                + t * t * t * t / 863310000;
        return 385000.56 + getCoefficient(d, m, m1, f) / 1000;
    }

>>>>>>> 98dc761 Initial commit for Moon phase revamp
>>>>>>> 810a1e9 Initial commit for Moon phase revamp
    private double[] calcMoon(double t) {
        double p2 = 6.283185307;
        double arc = 206264.8062;
        double coseps = .91748;
        double sineps = .39778;
        double lo = frac(.606433 + 1336.855225 * t);
        double l = p2 * frac(.374897 + 1325.55241 * t);
        double ls = p2 * frac(.993133 + 99.997361 * t);
        double d = p2 * frac(.827361 + 1236.853086 * t);
        double f = p2 * frac(.259086 + 1342.227825 * t);
        double dl = 22640 * Math.sin(l) - 4586 * Math.sin(l - 2 * d) + 2370 * Math.sin(2 * d) + 769 * Math.sin(2 * l)
                - 668 * Math.sin(ls) - 412 * Math.sin(2 * f) - 212 * Math.sin(2 * l - 2 * d)
                - 206 * Math.sin(l + ls - 2 * d) + 192 * Math.sin(l + 2 * d) - 165 * Math.sin(ls - 2 * d)
                - 125 * Math.sin(d) - 110 * Math.sin(l + ls) + 148 * Math.sin(l - ls) - 55 * Math.sin(2 * f - 2 * d);
        double s = f + (dl + 412 * Math.sin(2 * f) + 541 * Math.sin(ls)) / arc;
        double h = f - 2 * d;
        double n = -526 * Math.sin(h) + 44 * Math.sin(l + h) - 31 * Math.sin(-l + h) - 23 * Math.sin(ls + h)
                + 11 * Math.sin(-ls + h) - 25 * Math.sin(-2 * l + f) + 21 * Math.sin(-l + f);
        double lmoon = p2 * frac(lo + dl / 1296000);
        double bmoon = (18520 * Math.sin(s) + n) / arc;
        double cb = Math.cos(bmoon);
        double x = cb * Math.cos(lmoon);
        double v = cb * Math.sin(lmoon);
        double w = Math.sin(bmoon);
        double y = coseps * v - sineps * w;
        double z = sineps * v + coseps * w;
        double rho = Math.sqrt(1 - z * z);
        double dec = (360 / p2) * Math.atan(z / rho);
        double ra = (48 / p2) * Math.atan(y / (x + rho));
        if (ra < 0) {
            ra += 24;
        }
        return new double[] { dec, ra };
    }

    private double sinAlt(double moonJd, int hour, double lambda, double cphi, double sphi) {
        double jdo = moonJd + hour / 24.0;
        double t = (jdo - DateTimeUtils.MJD_JD2000) / DateTimeUtils.JULIAN_CENTURY_DAYS;
        double[] decra = calcMoon(t);
        double tau = 15.0 * (localMeanSiderealTime(jdo, lambda) - decra[1]);
        return sphi * sinDeg(decra[0]) + cphi * cosDeg(decra[0]) * cosDeg(tau);
    }

    private double localMeanSiderealTime(double moonJd, double lambda) {
        double moonJdo = Math.floor(moonJd);
        double ut = (moonJd - moonJdo) * 24.0;
        double t = (moonJdo - DateTimeUtils.MJD_JD2000) / DateTimeUtils.JULIAN_CENTURY_DAYS;
        double gmst = 6.697374558 + 1.0027379093 * ut + (8640184.812866 + (.093104 - .0000062 * t) * t) * t / 3600.0;
        return 24.0 * frac((gmst - lambda / 15.0) / 24.0);
    }

    private double[] quad(double yminus, double yo, double yplus) {
        double nz = 0;
        double a = .5 * (yminus + yplus) - yo;
        double b = .5 * (yplus - yminus);
        double xe = -b / (2 * a);
        double ye = (a * xe + b) * xe + yo;
        double dis = b * b - 4 * a * yo;
        double zero1 = 0;
        double zero2 = 0;
        if (dis >= 0) {
            double dx = .5 * Math.sqrt(dis) / Math.abs(a);
            zero1 = xe - dx;
            zero2 = xe + dx;
            if (Math.abs(zero1) <= 1) {
                nz += 1;
            }
            if (Math.abs(zero2) <= 1) {
                nz += 1;
            }
            if (zero1 < -1) {
                zero1 = zero2;
            }
        }
        return new double[] { ye, zero1, zero2, nz };
    }

    private double moonCorrection(double jd, double t, double k) {
        double ret = jd;
        ret += .000325 * sinDeg(299.77 + .107408 * k - .009173 * t * t) + .000165 * sinDeg(251.88 + .016321 * k)
                + .000164 * sinDeg(251.83 + 26.651886 * k) + .000126 * sinDeg(349.42 + 36.412478 * k)
                + .00011 * sinDeg(84.66 + 18.206239 * k);
        ret += .000062 * sinDeg(141.74 + 53.303771 * k) + .00006 * sinDeg(207.14 + 2.453732 * k)
                + .000056 * sinDeg(154.84 + 7.30686 * k) + .000047 * sinDeg(34.52 + 27.261239 * k)
                + .000042 * sinDeg(207.19 + .121824 * k) + .00004 * sinDeg(291.34 + 1.844379 * k);
        ret += .000037 * sinDeg(161.72 + 24.198154 * k) + .000035 * sinDeg(239.56 + 25.513099 * k)
                + .000023 * sinDeg(331.55 + 3.592518 * k);
        return ret;
    }

    /**
     * Sets the azimuth, elevation and zodiac in the moon object.
     */
    private MoonPosition getMoonPosition(double julianDate, double latitude, double longitude) {
        double lat = Math.toRadians(latitude);
        double lon = Math.toRadians(longitude);

        double gmst = DateTimeUtils.toGMST(julianDate);
        double lmst = DateTimeUtils.toLMST(gmst, lon) * Math.toRadians(15);

        double d = julianDate - 2447891.5;
        double anomalyMean = MathUtils.TWO_PI / AstroConstants.TROPICAL_YEAR_DAYS * d + 4.87650757829735
                - 4.935239984568769;
        double nu = anomalyMean + Math.PI * 0.016713 * Math.sin(anomalyMean);
        double sunLon = mod2Pi(nu + 4.935239984568769);

        double l0 = Math.toRadians(318.351648);
        double p0 = Math.toRadians(36.340410);
        double n0 = Math.toRadians(318.510107);
        double i = Math.toRadians(5.145396);
        double l = Math.toRadians(13.1763966) * d + l0;
        double mMoon = l - Math.toRadians(0.1114041) * d - p0;
        double n = n0 - Math.toRadians(0.0529539) * d;
        double c = l - sunLon;
        double ev = Math.toRadians(1.2739) * Math.sin(2 * c - mMoon);
        double ae = Math.toRadians(0.1858) * Math.sin(anomalyMean);
        double a3 = Math.toRadians(0.37) * Math.sin(anomalyMean);
        double mMoon2 = mMoon + ev - ae - a3;
        double ec = Math.toRadians(6.2886) * Math.sin(mMoon2);
        double a4 = Math.toRadians(0.214) * Math.sin(2 * mMoon2);
        double l2 = l + ev + ec - ae + a4;
        double v = Math.toRadians(0.6583) * Math.sin(2 * (l2 - sunLon));
        double l3 = l2 + v;
        double n2 = n - Math.toRadians(0.16) * Math.sin(anomalyMean);

        double moonLon = mod2Pi(n2 + Math.atan2(Math.sin(l3 - n2) * Math.cos(i), Math.cos(l3 - n2)));
        double moonLat = Math.asin(Math.sin(l3 - n2) * Math.sin(i));

        double[] raDec = ecl2Equ(moonLat, moonLon, julianDate);

        double distance = (1 - 0.00301401) / (1 + 0.054900 * Math.cos(mMoon2 + ec)) * 384401;

        double[] raDecTopo = geoEqu2TopoEqu(raDec, distance, lat, lmst);
        double[] azAlt = equ2AzAlt(raDecTopo[0], raDecTopo[1], lat, lmst);

<<<<<<< Upstream, based on main
        return new MoonPosition(Math.toDegrees(azAlt[0]), Math.toDegrees(azAlt[1]) + refraction(azAlt[1]), moonLon);
=======
        Position position = moon.getPosition();
        position.setAzimuth(Math.toDegrees(azAlt[0]));
        position.setElevation(Math.toDegrees(azAlt[1]) + refraction(azAlt[1]));

        moon.setZodiac(ZodiacCalc.calculate(moonLon, null));
>>>>>>> 810a1e9 Initial commit for Moon phase revamp
    }

    /**
     * Transform equatorial coordinates (ra/dec) to horizonal coordinates
     * (azimuth/altitude).
     */
    private double[] equ2AzAlt(double ra, double dec, double geolat, double lmst) {
        double cosdec = Math.cos(dec);
        double sindec = Math.sin(dec);
        double lha = lmst - ra;
        double coslha = Math.cos(lha);
        double coslat = Math.cos(geolat);
        double sinlat = Math.sin(geolat);

        double n = -cosdec * Math.sin(lha);
        double d = sindec * coslat - cosdec * coslha * sinlat;
        double az = mod2Pi(Math.atan2(n, d));
        double alt = Math.asin(sindec * sinlat + cosdec * coslha * coslat);

        return new double[] { az, alt };
    }

    /**
     * Transform ecliptical coordinates (lon/lat) to equatorial coordinates
     * (ra/dec)
     */
    private double[] ecl2Equ(double lat, double lon, double jd) {
        double t = DateTimeUtils.toJulianCenturies(jd);
        double eps = Math
                .toRadians(23. + (26 + 21.45 / 60.) / 60. + t * (-46.815 + t * (-0.0006 + t * 0.00181)) / 3600.);
        double coseps = Math.cos(eps);
        double sineps = Math.sin(eps);

        double sinlon = Math.sin(lon);
        double ra = mod2Pi(Math.atan2((sinlon * coseps - Math.tan(lat) * sineps), Math.cos(lon)));
        double dec = Math.asin(Math.sin(lat) * coseps + Math.cos(lat) * sineps * sinlon);

        return new double[] { ra, dec };
    }

    /**
     * Transform geocentric equatorial coordinates (rA/dec) to topocentric
     * equatorial coordinates.
     */
    private double[] geoEqu2TopoEqu(double[] raDec, double distance, double observerLat, double lmst) {
        double cosdec = Math.cos(raDec[1]);
        double coslat = Math.cos(observerLat);
        double rho = getCenterDistance(observerLat);

        double x = distance * cosdec * Math.cos(raDec[0]) - rho * coslat * Math.cos(lmst);
        double y = distance * cosdec * Math.sin(raDec[0]) - rho * coslat * Math.sin(lmst);
        double z = distance * Math.sin(raDec[1]) - rho * Math.sin(observerLat);

        double distanceTopocentric = Math.sqrt(x * x + y * y + z * z);
        double raTopo = mod2Pi(Math.atan2(y, x));
        double decTopo = Math.asin(z / distanceTopocentric);

        return new double[] { raTopo, decTopo };
    }

    /**
     * Returns geocentric distance from earth center.
     */
    private double getCenterDistance(double lat) {
        double co = Math.cos(lat);
        co = co * co;
        double si = Math.sin(lat);
        si = si * si;
        double fl = FL * FL;
        double u = 1.0 / Math.sqrt(co + fl * si);
        double a = AstroConstants.EARTH_EQUATORIAL_RADIUS * u;
        double b = a * fl;
        return Math.sqrt(a * a * co + b * b * si);
    }

    /**
     * Returns altitude increase in altitude in degrees. Rough refraction
     * formula using standard atmosphere: 1015 mbar and 10°C.
     */
    private double refraction(double alt) {
        int pressure = 1015;
        int temperature = 10;
        double altdeg = Math.toDegrees(alt);

        if (altdeg < -2 || altdeg >= 90) {
            return 0;
        }

        if (altdeg > 15) {
            return 0.00452 * pressure / ((273 + temperature) * Math.tan(alt));
        }

        double y = alt;
        double d = 0.0;
        double p = (pressure - 80.0) / 930.0;
        double q = 0.0048 * (temperature - 10.0);
        double y0 = y;
        double d0 = d;
        double n = 0.0;

        for (int i = 0; i < 3; i++) {
            n = y + (7.31 / (y + 4.4));
            n = 1.0 / Math.tan(Math.toRadians(n));
            d = n * p / (60.0 + q * (n + 39.0));
            n = y - y0;
            y0 = d - d0 - n;
            n = ((n != 0.0) && (y0 != 0.0)) ? y - n * (alt + d - y) / y0 : alt + d;
            y0 = y;
            d0 = d;
            y = n;
        }
        return d;
    }
}
