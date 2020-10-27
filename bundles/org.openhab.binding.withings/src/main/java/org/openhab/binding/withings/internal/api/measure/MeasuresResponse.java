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
package org.openhab.binding.withings.internal.api.measure;

import java.util.List;

import org.openhab.binding.withings.internal.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

/**
 * @author Sven Strohschein - Initial contribution
 */
public class MeasuresResponse extends BaseResponse {

    private MeasuresBody body;

    public MeasuresBody getBody() {
        return body;
    }

    public class MeasuresBody {

        @SerializedName("measuregrps")
        private List<MeasureGroup> measureGroups;

        public List<MeasureGroup> getMeasureGroups() {
            return measureGroups;
        }
    }

    public class MeasureGroup implements Comparable<MeasureGroup> {

        private Long date;

        private List<Measure> measures;

        public Long getDate() {
            return date;
        }

        public List<Measure> getMeasures() {
            return measures;
        }

        @Override
        public int compareTo(MeasuresResponse.MeasureGroup measureGroup) {
            return date.compareTo(measureGroup.date);
        }
    }

    public static class Measure {

        private int value;
        private int type;
        private int unit;

        public Measure() {
        }

        public Measure(int value, int type, int unit) {
            this.value = value;
            this.type = type;
            this.unit = unit;
        }

        public int getValue() {
            return value;
        }

        public int getType() {
            return type;
        }

        public int getUnit() {
            return unit;
        }
    }
}
