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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.InstantSource;
import java.util.Calendar;
import java.util.Set;
import java.util.TimeZone;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.astro.internal.model.DistanceType;
import org.openhab.binding.astro.internal.model.EclipseSet;
import org.openhab.binding.astro.internal.model.Moon;
import org.openhab.binding.astro.internal.model.MoonPosition;
import org.openhab.binding.astro.internal.model.Range;
import org.openhab.binding.astro.internal.util.AstroConstants;
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

    /**
     * Calculates all moon data at the specified coordinates
     */
    public Moon getMoonInfo(Calendar calendar, double latitude, double longitude) {
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
    public void setPositionalInfo(Calendar calendar, double latitude, double longitude, Moon moon, TimeZone zone) {
        double julianDate = DateTimeUtils.dateToJulianDate(calendar);

        moon.setPhaseSet(MoonPhaseCalc.calculate(instantSource, julianDate, moon.getPhaseSet(), zone.toZoneId()));

        MoonPosition moonPosition = getMoonPosition(julianDate, latitude, longitude);
        moon.setPosition(moonPosition);
        moon.setZodiac(ZodiacCalc.calculate(moonPosition.getLongitude(), null));
        moon.setDistance(DistanceType.CURRENT, MoonDistanceCalc.calculate(julianDate));
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

        return new MoonPosition(Math.toDegrees(azAlt[0]), Math.toDegrees(azAlt[1]) + refraction(azAlt[1]), moonLon);
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
