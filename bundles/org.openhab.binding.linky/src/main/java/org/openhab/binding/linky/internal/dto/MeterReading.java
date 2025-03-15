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

    public static MeterReading convertFromComsumptionReport(ConsumptionReport comsumptionReport) {
        MeterReading result = new MeterReading();
        result.readingType = new ReadingType();

        if (comsumptionReport.consumptions.aggregats != null) {
            if (comsumptionReport.consumptions.aggregats.days != null) {
                result.baseValue = fromAgregat(comsumptionReport.consumptions.aggregats.days);
            } else if (comsumptionReport.consumptions.aggregats.heure != null) {
                result.baseValue = fromAgregat(comsumptionReport.consumptions.aggregats.heure);
            }
        }

        return result;
    }

    public static IntervalReading[] fromAgregat(ConsumptionReport.Aggregate agregat) {
        int size = agregat.datas.size();
        IntervalReading[] result = new IntervalReading[size];

        for (int i = 0; i < size; i++) {
            Data dataObj = agregat.datas.get(i);
            result[i] = new IntervalReading();
            result[i].value = Double.valueOf(dataObj.valeur);
            result[i].date = dataObj.dateDebut;
        }

        return result;
    }
}
