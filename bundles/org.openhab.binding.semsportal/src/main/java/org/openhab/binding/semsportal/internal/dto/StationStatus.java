/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;

import org.openhab.binding.semsportal.internal.SEMSPortalBindingConstants;

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
    @SerializedName("info")
    private StationInfo info;

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
        DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern(info.getDateFormat())
                .appendLiteral(" ").appendPattern(SEMSPortalBindingConstants.TIME_FORMAT).toFormatter()
                .withZone(ZoneId.systemDefault());
        Instant instant = formatter.parse(stations.get(0).getDetails().getLastUpdate(), Instant::from);
        return ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
