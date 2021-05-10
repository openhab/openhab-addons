package org.openhab.binding.ems.utils;

import java.util.Calendar;

public class Formulas {

    private static final double SC = 1367; // Solar constant in W/m²
    public static final double DEG2RAD = Math.PI / 180;
    public static final double RAD2DEG = 180. / Math.PI;

    public static Calendar getCalendar(int year, int month, int day, int hour, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, day, hour, minute);
        return c;
    }

    /**
     * https://www.volker-quaschning.de/datserv/sunpos/sunpos.js
     * https://wiki.energie-m.de/Sonnenstandsberechnung
     * (GMT=0, MEZ=1, MESZ(Sommerzeit)=2)
     */
    public static double sunPositionDIN(int year, int month, int day, int hour, int min, int sec, double lat,
            double lon, int timezone) {
        double J, J2;
        double Zgl, MOZ, WOZ, w;
        double decl;
        double sunaz, sunhi;
        double asinGs;
        double acosAs;

        J2 = 365;
        if (year % 4 == 0) {
            J2++;
        }
        J = dayOfYear(year, month, day);
        MOZ = hour + 1.0 / 60 * min + 1.0 / 3600 * sec - timezone + 1;
        MOZ = MOZ - 4 * (15 - lon) / 60;
        J = J * 360 / J2 + MOZ / 24;
        decl = 0.3948 - 23.2559 * Math.cos(rad(J + 9.1)) - 0.3915 * Math.cos(rad(2 * J + 5.4))
                - 0.1764 * Math.cos(rad(3 * J + 26.0));
        Zgl = 0.0066 + 7.3525 * Math.cos(rad(J + 85.9)) + 9.9359 * Math.cos(rad(2 * J + 108.9))
                + 0.3387 * Math.cos(rad(3 * J + 105.2));
        WOZ = MOZ + Zgl / 60;
        w = (12 - WOZ) * 15;
        asinGs = Math.cos(rad(w)) * Math.cos(rad(lat)) * Math.cos(rad(decl)) + Math.sin(rad(lat)) * Math.sin(rad(decl));
        if (asinGs > 1) {
            asinGs = 1;
        }
        if (asinGs < -1) {
            asinGs = -1;
        }
        sunhi = grad(Math.asin(asinGs));
        acosAs = (Math.sin(rad(sunhi)) * Math.sin(rad(lat)) - Math.sin(rad(decl)))
                / (Math.cos(rad(sunhi)) * Math.cos(rad(lat)));
        if (acosAs > 1) {
            acosAs = 1;
        }
        if (acosAs < -1) {
            acosAs = -1;
        }
        sunaz = grad(Math.acos(acosAs));
        if ((WOZ > 12) || (WOZ < 0)) {
            sunaz = 180 + sunaz;
        } else {
            sunaz = 180 - sunaz;
        }
        ;
        double azimuth = sunaz;// * 1000) / 1000;
        double height = sunhi;// * 1000) / 1000;
        return round(height, 3);
    }

    public static int dayOfYear(int year, int month, int day) {
        int x;
        int[] monthday = new int[] { 0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334 };

        x = monthday[month - 1] + day;
        if ((year % 4 == 0) && ((year % 100 != 0) || (year % 400 == 0)) && (month > 2)) {
            x++;
        }
        return x;
    }

    public static double rad(double grad) {
        return (grad * Math.PI / 180);
    }

    public static double grad(double rad) {
        return (rad * 180 / Math.PI);
    }

    public static double airMass(double height) {
        if (height > 0) {
            double airmass = 1000 / Math.sin(rad(height)) / 1000;
            double zenithangle = Math.round((90 - height) * 1000) / 1000;
            return round(airmass, 3);
        }
        return 0;
    }

    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        long factor = (long) Math.pow(10, places);
        long tmp = Math.round(value * factor);
        return (double) tmp / factor;
    }

    /**
     * https://en.wikipedia.org/wiki/Air_mass_(solar_energy)
     */
    public static double solarIntensity(double airMass) {
        double si = 1.1 * 1367 * (Math.pow(0.7, Math.pow(airMass, 0.678)));
        return round(si, 3);
        // {\displaystyle I=1.1\times I_{\mathrm {o} }\times 0.7^{(AM^{0.678})}\,}
    }

    /**
     * Calculates sun radiation data.
     */
    public static double getRadiationInfo(Calendar calendar, double elevation, Double altitude) {
        double sinAlpha = Math.sin(DEG2RAD * elevation);

        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        int daysInYear = calendar.getActualMaximum(Calendar.DAY_OF_YEAR);

        // Direct Solar Radiation (in W/m²) at the atmosphere entry
        // At sunrise/sunset - calculations limits are reached
        double rOut = (elevation > 3) ? SC * (0.034 * Math.cos(DEG2RAD * (360 * dayOfYear / daysInYear)) + 1) : 0;
        double altitudeRatio = (altitude != null) ? 1 / Math.pow((1 - (6.5 / 288) * (altitude / 1000.0)), 5.256) : 1;
        double m = (Math.sqrt(1229 + Math.pow(614 * sinAlpha, 2)) - 614 * sinAlpha) * altitudeRatio;

        // Direct radiation after atmospheric layer
        // 0.6 = Coefficient de transmissivité
        double rDir = rOut * Math.pow(0.6, m) * sinAlpha;

        // Diffuse Radiation
        double rDiff = rOut * (0.271 - 0.294 * Math.pow(0.6, m)) * sinAlpha;
        double rTot = rDir + rDiff;

        return rTot;
    }

}
