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
package org.openhab.binding.linky.internal.dto;

import org.openhab.binding.linky.internal.dto.ConsumptionReport.Data;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link MeterReading} holds informations about energy consumption
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Arnal - Rewrite addon to use official dataconect API
 */

public class MeterReading {
    @SerializedName("usage_point_id")
    public String usagePointId;

    @SerializedName("start")
    public String startDate;

    @SerializedName("end")
    public String endDate;

    public String quality;

    @SerializedName("reading_type")
    public ReadingType readingType;

    @SerializedName("interval_reading")
    public IntervalReading[] baseValue;
    public IntervalReading[] weekValue;
    public IntervalReading[] monthValue;
    public IntervalReading[] yearValue;

    public static MeterReading convertFromComsumptionReport(ConsumptionReport comsumptionReport, boolean useIndex) {
        MeterReading result = new MeterReading();
        result.readingType = new ReadingType();

        if (comsumptionReport.consumptions.aggregats != null) {
            if (comsumptionReport.consumptions.aggregats.days != null) {
                result.baseValue = fromAgregat(comsumptionReport.consumptions.aggregats.days, useIndex);
            } else if (comsumptionReport.consumptions.aggregats.heure != null) {
                result.baseValue = fromAgregat(comsumptionReport.consumptions.aggregats.heure, useIndex);
            }
        }

        return result;
    }

    /**
     * This method will get data from old ConsumptionReport.Aggregate that is the format use by the Web API.
     * And will result and IntervalReading[] that is the format of the new Endis API
     *
     * @param agregat
     * @param useIndex : tell if we are reading value from raw consumption or from index value
     * @return IntervalReading[] : the data structure of new API
     */
    public static IntervalReading[] fromAgregat(ConsumptionReport.Aggregate agregat, boolean useIndex) {
        int size = agregat.datas.size();
        IntervalReading[] result = null;

        // For some unknown reason, index API don't return the index for day N-1.
        // So array length is cut off 1
        if (useIndex) {
            result = new IntervalReading[size - 1];
        } else {
            result = new IntervalReading[size];
        }

        if (!useIndex) {
            for (int i = 0; i < size; i++) {
                Data dataObj = agregat.datas.get(i);
                result[i] = new IntervalReading();
                result[i].value = dataObj.valeur;
                result[i].date = dataObj.dateDebut;
            }
        } else {
            double lastVal = 0.0;
            double[] lastValueSupplier = new double[10];
            double[] lastValueDistributor = new double[4];
            String lastCalendrierSupplier = "";
            String lastCalendrierDistributor = "";

            for (int idx = 0; idx < size; idx++) {
                Data dataObj = agregat.datas.get(idx);
                double value = dataObj.valeur;
                String calendrierDistributor = "";
                String calendrierSupplier = "";

                if (dataObj.calendrier == null) {
                    dataObj.calendrier = agregat.datas.get(idx - 1).calendrier;
                }

                calendrierDistributor = dataObj.calendrier[0].idCalendrier;
                calendrierSupplier = dataObj.calendrier[1].idCalendrier;

                if (idx > 0) {
                    result[idx - 1] = new IntervalReading();
                    result[idx - 1].value = value - lastVal;
                    // The index in on nextDay N, but index difference give consumption for day N-1
                    result[idx - 1].date = dataObj.dateDebut.minusDays(1);
                    result[idx - 1].initIndexInfo();

                    if (dataObj.classesTemporellesSupplier == null) {
                        dataObj.classesTemporellesSupplier = agregat.datas.get(idx - 1).classesTemporellesSupplier;
                    }

                    if (dataObj.classesTemporellesDistributor == null) {
                        dataObj.classesTemporellesDistributor = agregat.datas
                                .get(idx - 1).classesTemporellesDistributor;
                    }
                }

                initIndexValue(IndexMode.Supplier, dataObj.classesTemporellesSupplier, result, idx, calendrierSupplier,
                        lastCalendrierSupplier, lastValueSupplier);

                initIndexValue(IndexMode.Distributor, dataObj.classesTemporellesDistributor, result, idx,
                        calendrierDistributor, lastCalendrierDistributor, lastValueDistributor);

                lastVal = value;
                lastCalendrierDistributor = calendrierDistributor;
                lastCalendrierSupplier = calendrierSupplier;
            }
        }

        return result;
    }

    public static void initIndexValue(IndexMode indexMode, ClassesTemporelles[] classTp, IntervalReading[] ir, int idx,
            String calendrier, String lastCalendrier, double[] lastValue) {
        if (classTp != null) {
            for (int idxClTp = 0; idxClTp < classTp.length; idxClTp++) {
                ClassesTemporelles ct = classTp[idxClTp];

                if (idx > 0) {
                    // We check if calendar are the same that previous iteration
                    // If not, we are not able to reconciliate index, and so set index value to 0 !

                    if (calendrier.equals(lastCalendrier)) {
                        ir[idx - 1].indexInfo[indexMode.getIdx()].value[idxClTp] = (ct.valeur - lastValue[idxClTp]);
                    } else {
                        ir[idx - 1].indexInfo[indexMode.getIdx()].value[idxClTp] = 0;
                    }

                    ir[idx - 1].indexInfo[indexMode.getIdx()].label[idxClTp] = ct.libelle;
                }

                lastValue[idxClTp] = ct.valeur;
            }
        }
    }
}
