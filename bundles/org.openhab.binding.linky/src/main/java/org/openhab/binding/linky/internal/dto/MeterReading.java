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
        IntervalReading[] result = new IntervalReading[size];

        if (!useIndex) {
            for (int i = 0; i < size; i++) {
                Data dataObj = agregat.datas.get(i);
                result[i] = new IntervalReading();
                result[i].value = dataObj.valeur;
                result[i].date = dataObj.dateDebut;
            }
        } else {
            double lastVal = 0.0;
            double[] lastValFournisseur = new double[6];

            for (int i = 0; i < size; i++) {
                Data dataObj = agregat.datas.get(i);
                double value = dataObj.valeur;
                double[] valueFournisseur = new double[6];

                if (i > 0) {
                    result[i - 1] = new IntervalReading();
                    result[i - 1].value = value - lastVal;
                    result[i - 1].date = dataObj.dateDebut;
                    result[i - 1].valueFromFournisseur = new double[6];
                    result[i - 1].valueFromDistributeur = new double[4];

                    if (dataObj.classesTemporellesFournisseur != null) {
                        for (ClassesTemporelles ct : dataObj.classesTemporellesFournisseur) {
                            int idxFournisseur = -1;
                            if ("Base".equals(ct.libelle)) {
                                idxFournisseur = 0;
                            } else if ("Blanc Heures Creuses".equals(ct.libelle)) {
                                idxFournisseur = 3;
                            } else if ("Blanc Heures Pleines".equals(ct.libelle)) {
                                idxFournisseur = 2;
                            } else if ("Bleu Heures Creuses".equals(ct.libelle)) {
                                idxFournisseur = 1;
                            } else if ("Bleu Heures Pleines".equals(ct.libelle)) {
                                idxFournisseur = 0;
                            } else if ("Rouge Heures Creuses".equals(ct.libelle)) {
                                idxFournisseur = 5;
                            } else if ("Rouge Heures Pleines".equals(ct.libelle)) {
                                idxFournisseur = 4;
                            } else if ("Heures Pleines".equals(ct.libelle)) {
                                idxFournisseur = 0;
                            } else if ("Heures Creuses".equals(ct.libelle)) {
                                idxFournisseur = 1;
                            } else if ("Heures Creuses Hiver / Saison Haute".equals(ct.libelle)) {
                                idxFournisseur = 4;
                            } else if ("Heures Creuses Saison Basse".equals(ct.libelle)) {
                                idxFournisseur = 4;
                            } else if ("Heures Pleines Hiver / Saison Haute".equals(ct.libelle)) {
                                idxFournisseur = 4;
                            } else if ("Heures Pleines Saison Basse".equals(ct.libelle)) {
                                idxFournisseur = 4;
                            }

                            if (idxFournisseur == -1) {
                                continue;
                            }

                            valueFournisseur[idxFournisseur] = ct.valeur;
                            result[i - 1].valueFromFournisseur[idxFournisseur] = (valueFournisseur[idxFournisseur]
                                    - lastValFournisseur[idxFournisseur]);

                        }
                    }

                }
                lastVal = value;
                lastValFournisseur = valueFournisseur;
            }

        }

        return result;
    }
}
