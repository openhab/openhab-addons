/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.semsportal.internal.dto;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * Facade for easy access to the SEMS portal data response. Data is distributed over different parts of the response
 * object
 *
 * @author Iwan Bron - Initial contribution
 */
public class StationStatus {
    @SerializedName("kpi")
    private KeyPerformanceIndicators keyPerformanceIndicators;
    @SerializedName("inverter")
    private List<Station> stations;

    public Double getCurrentOutput() {
        return keyPerformanceIndicators.getCurrentOutput();
    }

    public Double getDayTotal() {
        return stations.isEmpty() ? null : stations.get(0).getDayTotal();
    }

    public Double getMonthTotal() {
        return stations.isEmpty() ? null : stations.get(0).getMonthTotal();
    }

    public Double getOverallTotal() {
        return stations.isEmpty() ? null : stations.get(0).getOverallTotal();
    }

    public Double getDayIncome() {
        return keyPerformanceIndicators.getDayIncome();
    }

    public Double getTotalIncome() {
        return keyPerformanceIndicators.getTotalIncome();
    }

    public boolean isOperational() {
        return stations.isEmpty() ? false : stations.get(0).getStatus() == 1;
    }

    public ZonedDateTime getLastUpdate() {
        if (stations.isEmpty()) {
            return null;
        }
        return ZonedDateTime.ofInstant(stations.get(0).getDetails().getLastUpdate().toInstant(),
                ZoneId.systemDefault());
    }
}
