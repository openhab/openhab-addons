/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.groheondus.internal.api.dto;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * Response of the aggregated data endpoint, covering both Sense and Sense Guard appliances.
 *
 * @author Arne Seime - Initial contribution
 */
public class ApplianceDataResponse {
    public Integer type;
    public Data data;

    public static class Data {
        public List<Measurement> measurement;
        public List<Withdrawal> withdrawals;
    }

    public static class Measurement {
        public String date;
        public Float temperature;
        public Float humidity;
        public Float flowrate;
        public Float pressure;
        @SerializedName("temperature_guard")
        public Float temperatureGuard;
    }

    public static class Withdrawal {
        public String date;
        public Double waterconsumption;
    }
}
