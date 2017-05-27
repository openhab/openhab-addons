/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tankerkoenig.internal.utility;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

/**
 * Utility static class to calculate if a given date is a german holiday.
 * Presently only those holidays that are valid in whole germany (Bundesweit) are used!
 * Code copied from Openhab1-Addons Samples-Rules
 *
 * @author Jürgen Baginski
 *
 */
public final class Holiday {

    private Holiday() {
        // avoid instantiation.
    }

    public static boolean isHoliday(DateTime now) {
        // Checks if today is a German holiday (Feiertag im ganzen Bundesgebiet!)
        // Code from Openhab1-Addons Samples-Rules
        int year = now.getYear();
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        int day = ((h + l - 7 * m + 114) % 31) + 1;
        boolean holiday = false;
        LocalDate easterSunday = LocalDate.parse(year + "-" + month + "-" + day);
        LocalDate stAdvent = LocalDate.parse(year + "-12-25")
                .minusDays(((LocalDate.parse(year + "-12-25").getDayOfWeek()) + 21));
        int dayOfYear = now.getDayOfYear();
        // bundesweiter Feiertag "Neujahr"
        if (dayOfYear == LocalDate.parse(year + "-01-01").getDayOfYear()) {
            holiday = true;
        }
        // Baden-Württemberg, Bayern, Sachsen-Anhalt, Heilige 3 Könige
        else if (dayOfYear == LocalDate.parse(year + "-01-06").getDayOfYear()) {
            holiday = false;
        }
        // Carnival ;-)
        else if (dayOfYear == easterSunday.getDayOfYear() - 48) {
            holiday = false;
        }
        // bundesweiter Feiertag,"good_friday"or "Karfreitag"
        else if (dayOfYear == easterSunday.getDayOfYear() - 2) {
            holiday = true;
        }
        // Brandenburg, "easter_sunday" or "Ostersonntag"
        else if (dayOfYear == easterSunday.getDayOfYear()) {
            holiday = false;
        }
        // bundesweiter Feiertag, "easter_monday" or "Ostermontag"
        else if (dayOfYear == easterSunday.getDayOfYear() + 1) {
            holiday = true;
        }
        // bundesweiter Feiertag,"1st_may" or "Tag der Arbeit"
        else if (dayOfYear == LocalDate.parse(year + "-05-01").getDayOfYear()) {
            holiday = true;
        }
        // bundesweiter Feiertag, "ascension_day" or "Christi Himmelfahrt"
        else if (dayOfYear == easterSunday.getDayOfYear() + 39) {
            holiday = true;
        }
        // Brandenburg, "whit_sunday" or "Pfingstsonntag"
        else if (dayOfYear == easterSunday.getDayOfYear() + 49) {
            holiday = false;
        }
        // bundesweiter Feiertag, "whit_monday" or "Pfingstmontag"
        else if (dayOfYear == easterSunday.getDayOfYear() + 50) {
            holiday = true;
        }
        // Baden-Württemberg, Bayern, Hessen, NRW, Rheinland-Pfalz, Saarland sowie regional in Sachsen, Thï¿½ringen
        // "corpus_christi"or "Frohnleichnahm"
        else if (dayOfYear == easterSunday.getDayOfYear() + 60) {
            holiday = false;
        }
        // Saarland sowie regional in Bayern, "assumption_day"or "Mariä Himmelfahrt"
        else if (dayOfYear == LocalDate.parse(year + "-08-15").getDayOfYear()) {
            holiday = false;
        }
        // bundesweiter Feiertag, "reunification"or "Tag der deutschen Einheit"
        else if (dayOfYear == LocalDate.parse(year + "-10-03").getDayOfYear()) {
            holiday = true;
        }
        // Brandenburg, Mecklenburg-Vorpommern, Sachsen, Sachsen-Anhalt, Thüringen, "reformation_day"or
        // "Reformationstag"
        else if (dayOfYear == LocalDate.parse(year + "-10-31").getDayOfYear()) {
            holiday = false;
        }
        // Baden-Württemberg, Bayern, NRW, Rheinland-Pfalz, Saarland, "all_saints_day"or "Allerheiligen"
        else if (dayOfYear == LocalDate.parse(year + "-11-01").getDayOfYear()) {
            holiday = false;
        }
        // religiöser Tag, "remembrance_day"or "Volkstrauertag"
        else if (dayOfYear == stAdvent.getDayOfYear() - 14) {
            holiday = false;
        }
        // religiöser Tag, "sunday_in_commemoration_of_the_dead"or "Totensonntag"
        else if (dayOfYear == stAdvent.getDayOfYear() - 7) {
            holiday = false;
        }
        // Sachsen, "day_of_repentance"or "Buß- und Bettag"
        else if (dayOfYear == stAdvent.getDayOfYear() - 11) {
            holiday = false;
        }
        // kann auch der 4te Advent sein, "christmas_eve" or "Heiligabend"
        else if (dayOfYear == LocalDate.parse(year + "-12-24").getDayOfYear()) {
            holiday = false;
        }
        // bundesweiter Feiertag, "1st_christmas_day"or "1. Weihnachtstag"
        else if (dayOfYear == LocalDate.parse(year + "-12-25").getDayOfYear()) {
            holiday = true;
        }
        // bundesweiter Feiertag, , "2nd_christmas_day"or "2. Weihnachtstag"
        else if (dayOfYear == LocalDate.parse(year + "-12-26").getDayOfYear()) {
            holiday = true;
        }
        // "new_years_eve" or "Silvester"
        else if (dayOfYear == LocalDate.parse(year + "-12-31").getDayOfYear()) {
            holiday = false;
        }
        return holiday;
    }

}
