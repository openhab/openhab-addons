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
            double[] lastValueSupplier = new double[6];
            double[] lastValueDistributor = new double[6];
            String lastCalendrierSupplier = "";
            String lastCalendrierDistributor = "";

            for (int i = 0; i < size; i++) {
                Data dataObj = agregat.datas.get(i);
                double value = dataObj.valeur;
                String calendrierDistributor = "";
                String calendrierSupplier = "";

                if (dataObj.calendrier == null) {
                    dataObj.calendrier = agregat.datas.get(i - 1).calendrier;
                }

                calendrierDistributor = dataObj.calendrier[0].idCalendrier;
                calendrierSupplier = dataObj.calendrier[1].idCalendrier;

                if (i > 0) {
                    result[i - 1] = new IntervalReading();
                    if (i == 1) {
                        result[i - 1].value = 0.0;
                    } else {
                        result[i - 1].value = value - lastVal;
                    }
                    // The index in on nextDay N, but index difference give consumption for day N-1
                    result[i - 1].date = dataObj.dateDebut.minusDays(1);

                    result[i - 1].valueSupplier = new double[10];
                    result[i - 1].valueDistributor = new double[4];
                    result[i - 1].supplierLabel = new String[10];
                    result[i - 1].distributorLabel = new String[4];

                    if (dataObj.classesTemporellesSupplier == null) {
                        dataObj.classesTemporellesSupplier = agregat.datas.get(i - 1).classesTemporellesSupplier;
                    }

                    if (dataObj.classesTemporellesDistributor == null) {
                        dataObj.classesTemporellesDistributor = agregat.datas.get(i - 1).classesTemporellesDistributor;
                    }

                    if (dataObj.classesTemporellesSupplier != null) {
                        for (int idxSupplier = 0; idxSupplier < dataObj.classesTemporellesSupplier.length; idxSupplier++) {
                            ClassesTemporelles ct = dataObj.classesTemporellesSupplier[idxSupplier];

                            if (i == 1 || !calendrierSupplier.equals(lastCalendrierSupplier)) {
                                result[i - 1].valueSupplier[idxSupplier] = 0.00;
                            } else {
                                result[i - 1].valueSupplier[idxSupplier] = (ct.valeur - lastValueSupplier[idxSupplier]);
                            }
                            result[i - 1].supplierLabel[idxSupplier] = ct.libelle;

                            lastValueSupplier[idxSupplier] = ct.valeur;
                        }
                    }

                    if (dataObj.classesTemporellesDistributor != null) {
                        for (int idxDistributor = 0; idxDistributor < dataObj.classesTemporellesDistributor.length; idxDistributor++) {
                            ClassesTemporelles ct = dataObj.classesTemporellesDistributor[idxDistributor];

                            if (i == 1 || !calendrierDistributor.equals(lastCalendrierDistributor)) {
                                result[i - 1].valueDistributor[idxDistributor] = 0.0;
                            } else {
                                result[i - 1].valueDistributor[idxDistributor] = (ct.valeur
                                        - lastValueDistributor[idxDistributor]);
                            }
                            result[i - 1].distributorLabel[idxDistributor] = ct.libelle;

                            lastValueDistributor[idxDistributor] = ct.valeur;
                        }
                    }
                }
                lastVal = value;
                lastCalendrierDistributor = calendrierDistributor;
                lastCalendrierSupplier = calendrierSupplier;
            }

        }

        return result;
    }
}
