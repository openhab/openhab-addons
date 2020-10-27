/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.withings.internal.api.sleep;

import java.util.List;

import org.openhab.binding.withings.internal.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

/**
 * @author Sven Strohschein - Initial contribution
 */
public class SleepResponse extends BaseResponse {

    private SleepBody body;

    public SleepBody getBody() {
        return body;
    }

    public class SleepBody {

        private List<SleepSeries> series;

        public List<SleepSeries> getSeries() {
            return series;
        }
    }

    public class SleepSeries implements Comparable<SleepSeries> {

        private String date;
        @SerializedName("startdate")
        private Long startDate;
        @SerializedName("enddate")
        private Long endDate;
        private SleepData data;

        public Long getStartDate() {
            return startDate;
        }

        public Long getEndDate() {
            return endDate;
        }

        public SleepData getData() {
            return data;
        }

        @Override
        public int compareTo(SleepSeries series) {
            return date.compareTo(series.date);
        }
    }

    public class SleepData {

        @SerializedName("sleep_score")
        private Integer sleepScore;

        public Integer getSleepScore() {
            return sleepScore;
        }
    }
}
