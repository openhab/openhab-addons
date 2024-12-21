/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.openhab.binding.linky.internal.dto.ConsumptionReport.Data;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link UserInfo} holds informations about energy delivery point
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
    public IntervalReading[] dayValue;
    public IntervalReading[] weekValue;
    public IntervalReading[] monthValue;
    public IntervalReading[] yearValue;

    public static MeterReading convertFromComsumptionReport(ConsumptionReport comsumptionReport) {
        MeterReading result = new MeterReading();
        result.readingType = new ReadingType();

        if (comsumptionReport.consumptions.aggregats != null) {
            result.dayValue = fromAgregat(comsumptionReport.consumptions.aggregats.days);
        } else {
            // result.dayValue = fromLabelsAndDatas(comsumptionReport.consumptions.labels,
            // comsumptionReport.consumptions.data);
        }

        return result;
    }

    public static IntervalReading[] fromAgregat(ConsumptionReport.Datas agregat) {
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

    public static IntervalReading[] fromLabelsAndDatas(List<String> labels, List<Double> datas) {
        int size = datas.size();
        IntervalReading[] result = new IntervalReading[size];

        for (int i = 0; i < size; i++) {
            Double data = datas.get(i);
            String label = labels.get(i);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS[X]");
            ZonedDateTime dt = ZonedDateTime.parse(label, formatter);

            result[i] = new IntervalReading();
            result[i].value = data;
            result[i].date = dt.toLocalDateTime();
        }

        return result;
    }
}
