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
package org.openhab.binding.sedif.internal.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sedif.internal.dto.MeterReading.Data.Consommation;
import org.openhab.binding.sedif.internal.types.SedifException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link MeterReading} holds Meter reading values
 *
 * @author Laurent Arnal - Initial contribution
 */
public class MeterReading extends Value {
    private transient final Logger logger = LoggerFactory.getLogger(MeterReading.class);

    public class Data {
        public Data() {
            hasModifications = false;
            consoMap = new HashMap<String, Consommation>();
        }

        public class Consommation {
            @SerializedName("CONSOMMATION")
            public float consommation;

            @SerializedName("DATE_INDEX")
            public LocalDateTime dateIndex;

            @SerializedName("DEBIT_PERMANENT")
            public String debitPermanent;

            @SerializedName("FLAG_ESTIMATION")
            public boolean flagEstimation;

            @SerializedName("FLAG_ESTIMATION_INDEX")
            public boolean flagEstimationIndex;

            @SerializedName("VALEUR_INDEX")
            public float valeurIndex;
        }

        private transient Map<String, Consommation> consoMap;

        public void putEntries(String key, Consommation conso) {
            if (consoMap == null) {
                consoMap = new HashMap<String, MeterReading.Data.Consommation>();
            }
            consoMap.put(key, conso);
        }

        public Consommation getEntries(String key) {
            return consoMap.get(key);
        }

        @SerializedName("CONSOMMATION")
        public @Nullable Consommation[] consommation;
        public @Nullable Consommation[] weekConso;
        public @Nullable Consommation[] monthConso;
        public @Nullable Consommation[] yearConso;

        @SerializedName("CONSOMMATION_MAX")
        public float consommationMax;

        @SerializedName("CONSOMMATION_MOYENNE")
        public float consommationMoyenne;

        @SerializedName("DATE_CONSOMMATION_MAX")
        public String dateConsommationMax;

        @SerializedName("DATE_DEBUT")
        public String dateDebut;

        @SerializedName("DATE_FIN")
        public String dateFin;

        @SerializedName("ID_PDS")
        public String idPds;

        @SerializedName("NUMERO_COMPTEUR")
        public String numeroCompteur;

        public boolean hasModifications;

        public boolean hasModifications() {
            return hasModifications;
        }
    }

    public Data data;
    public boolean showDebitPermanent;
    public float seuilDebitPermanet;
    public float prixMoyenEau;
    public boolean canCompareMonth;
    public String numeroCompteur;

    public MeterReading() {
        data = new Data();
    }

    public MeterReading merge(MeterReading incomingMeterReading) throws SedifException {
        Data.Consommation[] incomingConso = incomingMeterReading.data.consommation;
        if (incomingConso == null) {
            throw new SedifException("Invalid meterReading data: no day period");
        }

        // Normalize dateIndex
        for (int idx = 0; idx < incomingConso.length; idx++) {
            incomingConso[idx].dateIndex = incomingConso[idx].dateIndex.withHour(0).withMinute(0).withSecond(0);
        }

        if (this.data.consommation == null) {
            this.data.consommation = incomingConso;
        } else {
            Data.Consommation[] existingConso = this.data.consommation;
            if (existingConso == null) {
                throw new SedifException("Invalid meterReading data: no day period");
            }

            LocalDate firstDateExistingCoso = existingConso[0].dateIndex.toLocalDate();
            LocalDate lastDateExistingConso = existingConso[existingConso.length - 1].dateIndex.toLocalDate();

            LocalDate firstDateIncomingCoso = incomingConso[0].dateIndex.toLocalDate();
            LocalDate lastDateIncomingConso = incomingConso[incomingConso.length - 1].dateIndex.toLocalDate();

            Consommation[] newConso = null;

            // The new block of data is before existing data
            if (firstDateIncomingCoso.isBefore(firstDateExistingCoso)) {

                // We browse the incoming data backward from the end to find first mergeable index
                int idx = incomingConso.length - 1;

                // While go backward until we find a date in incomingConso before the firstDate of existing Conso
                while (idx > 0 && incomingConso[idx].dateIndex.toLocalDate().isAfter(firstDateExistingCoso)) {
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
                        && incomingConso[idx].dateIndex.toLocalDate().isBefore(lastDateExistingConso)) {
                    idx++;
                }

                // We need full existingConso and incomingConsol.length - idx element from incomingConso in the new tab
                newConso = new Consommation[existingConso.length + incomingConso.length - idx];
                System.arraycopy(existingConso, 0, newConso, 0, existingConso.length);
                System.arraycopy(incomingConso, idx, newConso, existingConso.length, incomingConso.length - idx);
            }

            if (newConso != null) {
                this.data.consommation = newConso;
            }
        }

        return this;
    }

    public void check() throws SedifException {
        if (data.consommation == null) {
            throw new SedifException("Invalid meterReading data: no day period");
        }

        if (data.consommation.length == 0) {
            throw new SedifException("Invalid meterReading data: no day period");
        }
    }

    public void calcAgregat() throws SedifException {
        Data.Consommation[] lcConso = data.consommation;
        if (lcConso == null) {
            throw new SedifException("Invalid meterReading data: no day period");
        }

        for (int idx = 0; idx < lcConso.length; idx++) {
            LocalDate dt = lcConso[idx].dateIndex.toLocalDate();
            this.data.putEntries(dt.toString(), lcConso[idx]);
        }

        LocalDate startDate = lcConso[0].dateIndex.toLocalDate();
        LocalDate endDate = lcConso[data.consommation.length - 1].dateIndex.toLocalDate();

        LocalDate realStartDate = startDate.atStartOfDay().with(TemporalAdjusters.previous(DayOfWeek.MONDAY))
                .toLocalDate();
        LocalDate realEndDate = endDate.atStartOfDay().with(TemporalAdjusters.next(DayOfWeek.SUNDAY)).toLocalDate();

        int startWeek = realStartDate.get(WeekFields.of(Locale.FRANCE).weekOfYear());
        int endWeek = realEndDate.get(WeekFields.of(Locale.FRANCE).weekOfYear());

        int yearsNum = realEndDate.getYear() - realStartDate.getYear() + 1;
        int monthsNum = (realEndDate.getYear() - realStartDate.getYear()) * 12 + realEndDate.getMonthValue()
                - realStartDate.getMonthValue() + 1;

        int weeksNum = (realEndDate.getYear() - realStartDate.getYear()) * 52 + endWeek - startWeek + 1;

        data.weekConso = new Consommation[weeksNum];
        data.monthConso = new Consommation[monthsNum];
        data.yearConso = new Consommation[yearsNum];

        for (int idxWeek = 0; idxWeek < weeksNum; idxWeek++) {
            LocalDate startOfWeek = realStartDate.plusWeeks(idxWeek);
            LocalDate endOfWeek = startOfWeek.plusDays(6);

            Consommation weekConso = data.new Consommation();
            data.weekConso[idxWeek] = weekConso;

            float indexDeb = getIndex(startOfWeek.minusDays(1), startDate, endDate);
            float indexFin = getIndex(endOfWeek, startDate, endDate);
            float indexDiff = indexFin - indexDeb;

            weekConso.consommation = indexDiff;
            weekConso.dateIndex = LocalDateTime.of(startOfWeek.getYear(), startOfWeek.getMonth(),
                    startOfWeek.getDayOfMonth(), 0, 0, 0);
        }

        for (int idxMonth = 0; idxMonth < monthsNum; idxMonth++) {
            LocalDate startOfMonth = realStartDate.with(TemporalAdjusters.firstDayOfMonth()).plusMonths(idxMonth);
            LocalDate endOfMonth = startOfMonth.with(TemporalAdjusters.lastDayOfMonth());

            Consommation monthConso = data.new Consommation();
            data.monthConso[idxMonth] = monthConso;

            float indexDeb = getIndex(startOfMonth.minusDays(1), startDate, endDate);
            float indexFin = getIndex(endOfMonth, startDate, endDate);
            float indexDiff = indexFin - indexDeb;

            monthConso.consommation = indexDiff;
            monthConso.dateIndex = LocalDateTime.of(startOfMonth.getYear(), startOfMonth.getMonth(), 1, 0, 0, 0);
        }

        for (int idxYear = 0; idxYear < yearsNum; idxYear++) {
            LocalDate startOfYear = realStartDate.with(TemporalAdjusters.firstDayOfYear()).plusYears(idxYear);
            LocalDate endOfYear = startOfYear.with(TemporalAdjusters.lastDayOfYear());

            Consommation yearConso = data.new Consommation();
            data.yearConso[idxYear] = yearConso;

            float indexDeb = getIndex(startOfYear.minusDays(1), startDate, endDate);
            float indexFin = getIndex(endOfYear, startDate, endDate);
            float indexDiff = indexFin - indexDeb;

            yearConso.consommation = indexDiff;
            yearConso.dateIndex = LocalDateTime.of(startOfYear.getYear(), 1, 1, 0, 0, 0);
        }
    }

    public float getIndex(LocalDate date, LocalDate startDate, LocalDate endDate) {
        LocalDate dateToGet = date;
        if (dateToGet.isBefore(startDate)) {
            dateToGet = startDate;
        } else if (dateToGet.isAfter(endDate)) {
            dateToGet = endDate;
        }
        Consommation result = data.getEntries(dateToGet.toString());
        if (result != null) {
            return result.valeurIndex;
        } else {
            Consommation r1 = data.getEntries(dateToGet.minusDays(1).toString());
            Consommation r2 = data.getEntries(dateToGet.plusDays(1).toString());

            if (r1 != null && r2 != null) {
                return (r1.valeurIndex + r2.valeurIndex) / 2;
            }
        }
        return 0;
    }
}
