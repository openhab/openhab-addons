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
package org.openhab.binding.publicholiday.internal.publicholiday;

import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.publicholiday.internal.PublicHolidayConfiguration;
import org.openhab.binding.publicholiday.internal.publicholiday.definitions.EasternRelatedHolidayDef;
import org.openhab.binding.publicholiday.internal.publicholiday.definitions.GeneralFixedHolidayDef;
import org.openhab.binding.publicholiday.internal.publicholiday.definitions.GermanHolidayDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate the list of relevant holidays, based on the configuration
 * 
 * @author Martin Güthle - Initial contribution
 */
public class HolidayFactory {

    private static final Logger logger = LoggerFactory.getLogger(HolidayFactory.class);

    public static List<Holiday> generateHolidayList(PublicHolidayConfiguration config) {
        ArrayList<Holiday> result = new ArrayList<>();

        if (config.newYear) {
            result.add(new FixedHoliday(GeneralFixedHolidayDef.NEW_YEAR));
        }
        if (config.threeKingsDay) {
            result.add(new FixedHoliday(GeneralFixedHolidayDef.THREE_KINGS_DAY));
        }
        if (config.reformationDay) {
            result.add(new FixedHoliday(GeneralFixedHolidayDef.REFORMATION_DAY));
        }
        if (config.allSaintsDay) {
            result.add(new FixedHoliday(GeneralFixedHolidayDef.ALL_SAINTS_DAY));
        }
        if (config.christmasEve) {
            result.add(new FixedHoliday(GeneralFixedHolidayDef.CHRISTMAS_EVE));
        }
        if (config.christmasDay) {
            result.add(new FixedHoliday(GeneralFixedHolidayDef.ALL_SAINTS_DAY));
        }
        if (config.secondChristmasDay) {
            result.add(new FixedHoliday(GeneralFixedHolidayDef.SECOND_CHRISTMAS_DAY));
        }
        if (config.newYearsEve) {
            result.add(new FixedHoliday(GeneralFixedHolidayDef.NEW_YEARS_EVE));
        }

        if (config.goodFriday) {
            result.add(new EasternRelatedHoliday(EasternRelatedHolidayDef.GOOD_FRIDAY));
        }
        if (config.easterSunday) {
            result.add(new EasternRelatedHoliday(EasternRelatedHolidayDef.EASTER_SUNDAY));
        }
        if (config.easterMonday) {
            result.add(new EasternRelatedHoliday(EasternRelatedHolidayDef.EASTER_MONDAY));
        }
        if (config.ascensionDay) {
            result.add(new EasternRelatedHoliday(EasternRelatedHolidayDef.ASCENSION_DAY));
        }
        if (config.whitSunday) {
            result.add(new EasternRelatedHoliday(EasternRelatedHolidayDef.WHIT_SUNDAY));
        }
        if (config.whitMonday) {
            result.add(new EasternRelatedHoliday(EasternRelatedHolidayDef.WHIT_MONDAY));
        }
        if (config.corpusChristi) {
            result.add(new EasternRelatedHoliday(EasternRelatedHolidayDef.CORPUS_CHRISTI));
        }

        if (config.tagDerArbeit) {
            result.add(new FixedHoliday(GermanHolidayDef.TAG_DER_ARBEIT));
        }
        if (config.tagDerDeutschenEinheit) {
            result.add(new FixedHoliday(GermanHolidayDef.TAG_DER_DEUTSCHEN_EINHEIT));
        }

        StringBuilder sb = new StringBuilder();
        result.forEach(holiday -> sb.append("\n\t" + holiday.getName()));
        logger.info("Configured public holidays: {}", sb.toString());

        return result;
    }
}
