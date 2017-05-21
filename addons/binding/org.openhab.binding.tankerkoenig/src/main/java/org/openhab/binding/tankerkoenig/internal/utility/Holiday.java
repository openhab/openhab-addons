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
        // String holidayName = null;
        LocalDate easterSunday = LocalDate.parse(year + "-" + month + "-" + day);
        LocalDate stAdvent = LocalDate.parse(year + "-12-25")
                .minusDays(((LocalDate.parse(year + "-12-25").getDayOfWeek()) + 21));
        int dayOfYear = now.getDayOfYear();
        // bundesweiter Feiertag
        if (dayOfYear == LocalDate.parse(year + "-01-01").getDayOfYear()) {
            // holidayName = "new_years_day"; // Neujahr
            holiday = true;
        }
        // Baden-Wï¿½rttemberg, Bayern, Sachsen-Anhalt
        else if (dayOfYear == LocalDate.parse(year + "-01-06").getDayOfYear()) {
            // holidayName = "holy_trinity";// Heilige 3 Kï¿½nige
            holiday = false;
        }
        // Carnival ;-)
        else if (dayOfYear == easterSunday.getDayOfYear() - 48) {
            // holidayName = "carnival_monday"; // Rosenmontag
            holiday = false;
        }
        // bundesweiter Feiertag
        else if (dayOfYear == easterSunday.getDayOfYear() - 2) {
            // holidayName = "good_friday"; // Karfreitag
            holiday = true;
        }
        // Brandenburg
        else if (dayOfYear == easterSunday.getDayOfYear()) {
            // holidayName = "easter_sunday"; // Ostersonntag
            holiday = false;
        }
        // bundesweiter Feiertag
        else if (dayOfYear == easterSunday.getDayOfYear() + 1) {
            // holidayName = "easter_monday"; // Ostermontag
            holiday = true;
        }
        // bundesweiter Feiertag
        else if (dayOfYear == LocalDate.parse(year + "-05-01").getDayOfYear()) {
            // holidayName = "1st_may";// Tag der Arbeit
            holiday = true;
        }
        // bundesweiter Feiertag
        else if (dayOfYear == easterSunday.getDayOfYear() + 39) {
            // holidayName = "ascension_day"; // Christi Himmelfahrt
            holiday = true;
        }
        // Brandenburg
        else if (dayOfYear == easterSunday.getDayOfYear() + 49) {
            // holidayName = "whit_sunday"; // Pfingstsonntag
            holiday = false;
        }
        // bundesweiter Feiertag
        else if (dayOfYear == easterSunday.getDayOfYear() + 50) {
            // holidayName = "whit_monday"; // Pfingstmontag
            holiday = true;
        }
        // Baden-Wï¿½rttemberg, Bayern, Hessen, NRW, Rheinland-Pfalz, Saarland sowie regional in Sachsen, Thï¿½ringen
        else if (dayOfYear == easterSunday.getDayOfYear() + 60) {
            // holidayName = "corpus_christi"; // Frohnleichnahm
            holiday = false;
        }
        // Saarland sowie regional in Bayern
        else if (dayOfYear == LocalDate.parse(year + "-08-15").getDayOfYear()) {
            // holidayName = "assumption_day"; // Mariï¿½ Himmelfahrt
            holiday = false;
        }
        // bundesweiter Feiertag
        else if (dayOfYear == LocalDate.parse(year + "-10-03").getDayOfYear()) {
            // holidayName = "reunification"; // Tag der deutschen Einheit
            holiday = true;
        }
        // Brandenburg, Mecklenburg-Vorpommern, Sachsen, Sachsen-Anhalt, Thï¿½ringen
        else if (dayOfYear == LocalDate.parse(year + "-10-31").getDayOfYear()) {
            // holidayName = "reformation_day"; // Reformationstag
            holiday = false;
        }
        // Baden-Wï¿½rttemberg, Bayern, NRW, Rheinland-Pfalz, Saarland
        else if (dayOfYear == LocalDate.parse(year + "-11-01").getDayOfYear()) {
            // holidayName = "all_saints_day"; // Allerheiligen
            holiday = false;
        }
        // religiï¿½ser Tag
        else if (dayOfYear == stAdvent.getDayOfYear() - 14) {
            // holidayName = "remembrance_day"; // Volkstrauertag
            holiday = false;
        }
        // religiï¿½ser Tag
        else if (dayOfYear == stAdvent.getDayOfYear() - 7) {
            // holidayName = "sunday_in_commemoration_of_the_dead"; // Totensonntag
            holiday = false;
        }
        // Sachsen
        else if (dayOfYear == stAdvent.getDayOfYear() - 11) {
            // holidayName = "day_of_repentance"; // Buï¿½- und Bettag
            holiday = false;
        }
        // kann auch der 4te Advent sein
        else if (dayOfYear == LocalDate.parse(year + "-12-24").getDayOfYear()) {
            // holidayName = "christmas_eve"; // Heiligabend
            holiday = false;
        }
        // bundesweiter Feiertag
        else if (dayOfYear == LocalDate.parse(year + "-12-25").getDayOfYear()) {
            // holidayName = "1st_christmas_day"; // 1. Weihnachtstag
            holiday = true;
        }
        // bundesweiter Feiertag
        else if (dayOfYear == LocalDate.parse(year + "-12-26").getDayOfYear()) {
            // holidayName = "2nd_christmas_day"; // 2. Weihnachtstag
            holiday = true;
        }
        // Silvester
        else if (dayOfYear == LocalDate.parse(year + "-12-31").getDayOfYear()) {
            // holidayName = "new_years_eve"; // Silvester
            holiday = false;
        }
        return holiday;
    }

}
