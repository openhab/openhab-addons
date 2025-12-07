/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.sedif.internal.api.helpers;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sedif.internal.dto.MeterReading;
import org.openhab.binding.sedif.internal.dto.MeterReading.Data;
import org.openhab.binding.sedif.internal.dto.MeterReading.Data.Consommation;
import org.openhab.binding.sedif.internal.types.SedifException;

/**
 * {@link MeterReadingHelper} helper methods for MeterReading
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class MeterReadingHelper {
    public static MeterReading merge(MeterReading currentMeterReading, MeterReading incomingMeterReading)
            throws SedifException {
        Data.Consommation[] incomingConso = incomingMeterReading.data.consommation;
        if (incomingConso == null) {
            throw new SedifException("Invalid meterReading data: no day period");
        }

        // Normalize dateIndex
        for (int idx = 0; idx < incomingConso.length; idx++) {
            incomingConso[idx].dateIndex = incomingConso[idx].dateIndex.withHour(0).withMinute(0).withSecond(0);
        }

        if (currentMeterReading.data.consommation == null) {
            currentMeterReading.data.consommation = incomingConso;
        } else {
            Data.Consommation[] existingConso = currentMeterReading.data.consommation;
            if (existingConso == null) {
                throw new SedifException("Invalid meterReading data: no day period");
            }

            LocalDate firstDateExistingConso = existingConso[0].dateIndex.toLocalDate();
            LocalDate lastDateExistingConso = existingConso[existingConso.length - 1].dateIndex.toLocalDate();

            LocalDate firstDateIncomingConso = incomingConso[0].dateIndex.toLocalDate();
            LocalDate lastDateIncomingConso = incomingConso[incomingConso.length - 1].dateIndex.toLocalDate();

            Consommation[] newConso = null;

            // The new block of data is before existing data
            if (firstDateIncomingConso.isBefore(firstDateExistingConso)) {
                // We browse the incoming data backward from the end to find first mergeable index
                int idx = incomingConso.length - 1;

                // While go backward until we find a date in incomingConso before the firstDate of existing Conso
                while (idx > 0 && incomingConso[idx].dateIndex.toLocalDate().isAfter(firstDateExistingConso)) {
                    idx--;
                }

                // We need idx element from incomingConso, and full existingConso in new tab
                newConso = new Consommation[idx + existingConso.length];
                System.arraycopy(incomingConso, 0, newConso, 0, idx);
                System.arraycopy(existingConso, 0, newConso, idx, existingConso.length);
            }
            // The new block of data is after existing Data
            else if (lastDateIncomingConso.isAfter(lastDateExistingConso)) {
                // We browse the incoming data forward from the start to find first mergeable index
                int idx = 0;

                while (idx < incomingConso.length
                        && incomingConso[idx].dateIndex.toLocalDate().compareTo(lastDateExistingConso) <= 0) {
                    idx++;
                }

                // We need full existingConso and incomingConsol.length - idx element from incomingConso in the new tab
                newConso = new Consommation[existingConso.length + incomingConso.length - idx];
                System.arraycopy(existingConso, 0, newConso, 0, existingConso.length);
                System.arraycopy(incomingConso, idx, newConso, existingConso.length, incomingConso.length - idx);
            }
            // The new block of data is in middle of existing data
            else {
                // We browse the incoming data forward from the start to find first mergeable index
                int idxStart = 0;

                while (idxStart < existingConso.length
                        && existingConso[idxStart].dateIndex.toLocalDate().compareTo(firstDateIncomingConso) < 0) {
                    idxStart++;
                }
                idxStart--;

                int idxEnd = existingConso.length - 1;

                // While go backward until we find a date in incomingConso before the firstDate of existing Conso
                while (idxEnd > 0 && existingConso[idxEnd].dateIndex.toLocalDate().isAfter(lastDateIncomingConso)) {
                    idxEnd--;
                }
                idxEnd++;

                int len = idxStart + 1 + incomingConso.length + existingConso.length - idxEnd;
                newConso = new Consommation[len];
                System.arraycopy(existingConso, 0, newConso, 0, idxStart + 1);
                System.arraycopy(incomingConso, 0, newConso, idxStart + 1, incomingConso.length);
                if (idxEnd < existingConso.length) {
                    System.arraycopy(existingConso, idxEnd, newConso, idxEnd + incomingConso.length - 1,
                            existingConso.length - idxEnd);
                }
            }

            currentMeterReading.data.consommation = newConso;
        }

        return currentMeterReading;
    }

    public static void check(MeterReading meterReading) throws SedifException {
        if (meterReading.data.consommation == null) {
            throw new SedifException("Invalid meterReading data: no day period");
        }

        if (meterReading.data.consommation.length == 0) {
            throw new SedifException("Invalid meterReading data: no day period");
        }
    }

    public static void calcAgregat(MeterReading meterReading) throws SedifException {
        Data.Consommation[] lcConso = meterReading.data.consommation;
        if (lcConso == null) {
            throw new SedifException("Invalid meterReading data: no day period");
        }

        for (int idx = 0; idx < lcConso.length; idx++) {
            Data.Consommation conso = lcConso[idx];
            LocalDate dt = conso.dateIndex.toLocalDate();
            meterReading.data.putEntries(dt.toString(), lcConso[idx]);
            conso.consommation *= 1000.00;
        }

        LocalDate startDate = lcConso[0].dateIndex.toLocalDate();
        LocalDate endDate = lcConso[meterReading.data.consommation.length - 1].dateIndex.toLocalDate();

        LocalDate realStartDate = startDate.atStartOfDay().with(TemporalAdjusters.previous(DayOfWeek.MONDAY))
                .toLocalDate();
        LocalDate realEndDate = endDate.atStartOfDay().with(TemporalAdjusters.next(DayOfWeek.SUNDAY)).toLocalDate();

        int yearsNum = realEndDate.getYear() - realStartDate.getYear() + 1;
        int monthsNum = (realEndDate.getYear() - realStartDate.getYear()) * 12 + realEndDate.getMonthValue()
                - realStartDate.getMonthValue() + 1;

        int weeksNum = (int) ChronoUnit.WEEKS.between(realStartDate, realEndDate) + 1;

        meterReading.data.weekConso = new Consommation[weeksNum];
        meterReading.data.monthConso = new Consommation[monthsNum];
        meterReading.data.yearConso = new Consommation[yearsNum];

        for (int idxWeek = 0; idxWeek < weeksNum; idxWeek++) {
            LocalDate startOfWeek = realStartDate.plusWeeks(idxWeek);
            LocalDate endOfWeek = startOfWeek.plusDays(6);

            Consommation weekConso = meterReading.data.new Consommation();
            meterReading.data.weekConso[idxWeek] = weekConso;

            float indexDeb = getIndex(meterReading, startOfWeek.minusDays(1), startDate, endDate);
            float indexFin = getIndex(meterReading, endOfWeek, startDate, endDate);
            float indexDiff = indexFin - indexDeb;

            weekConso.consommation = indexDiff;
            weekConso.dateIndex = LocalDateTime.of(startOfWeek.getYear(), startOfWeek.getMonth(),
                    startOfWeek.getDayOfMonth(), 0, 0, 0);
        }

        for (int idxMonth = 0; idxMonth < monthsNum; idxMonth++) {
            LocalDate startOfMonth = realStartDate.with(TemporalAdjusters.firstDayOfMonth()).plusMonths(idxMonth);
            LocalDate endOfMonth = startOfMonth.with(TemporalAdjusters.lastDayOfMonth());

            Consommation monthConso = meterReading.data.new Consommation();
            meterReading.data.monthConso[idxMonth] = monthConso;

            float indexDeb = getIndex(meterReading, startOfMonth.minusDays(1), startDate, endDate);
            float indexFin = getIndex(meterReading, endOfMonth, startDate, endDate);
            float indexDiff = indexFin - indexDeb;

            monthConso.consommation = indexDiff;
            monthConso.dateIndex = LocalDateTime.of(startOfMonth.getYear(), startOfMonth.getMonth(), 1, 0, 0, 0);
        }

        for (int idxYear = 0; idxYear < yearsNum; idxYear++) {
            LocalDate startOfYear = realStartDate.with(TemporalAdjusters.firstDayOfYear()).plusYears(idxYear);
            LocalDate endOfYear = startOfYear.with(TemporalAdjusters.lastDayOfYear());

            Consommation yearConso = meterReading.data.new Consommation();
            meterReading.data.yearConso[idxYear] = yearConso;

            float indexDeb = getIndex(meterReading, startOfYear.minusDays(1), startDate, endDate);
            float indexFin = getIndex(meterReading, endOfYear, startDate, endDate);
            float indexDiff = indexFin - indexDeb;

            yearConso.consommation = indexDiff;
            yearConso.dateIndex = LocalDateTime.of(startOfYear.getYear(), 1, 1, 0, 0, 0);
        }
    }

    public static float getIndex(MeterReading meterReading, LocalDate date, LocalDate startDate, LocalDate endDate) {
        LocalDate dateToGet = date;
        if (dateToGet.isBefore(startDate)) {
            dateToGet = startDate;
        } else if (dateToGet.isAfter(endDate)) {
            dateToGet = endDate;
        }
        Consommation result = meterReading.data.getEntries(dateToGet.toString());
        if (result != null) {
            return result.valeurIndex;
        } else {
            Consommation r1 = meterReading.data.getEntries(dateToGet.minusDays(1).toString());
            Consommation r2 = meterReading.data.getEntries(dateToGet.plusDays(1).toString());

            if (r1 != null && r2 != null) {
                return (r1.valeurIndex + r2.valeurIndex) / 2;
            }
        }
        return 0;
    }
}
