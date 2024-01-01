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
package org.openhab.binding.semsportal.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * POJO for mapping the SEMS portal data response /data/kpi
 *
 * @author Iwan Bron - Initial contribution
 *
 */
public class KeyPerformanceIndicators {
    @SerializedName("pac")
    private Double currentOutput;
    @SerializedName("month_generation")
    private Double monthPower;
    @SerializedName("total_power")
    private Double totalPower;
    @SerializedName("day_income")
    private Double dayIncome;
    @SerializedName("total_income")
    private Double totalIncome;
    @SerializedName("yield_rate")
    private Double yieldRate;
    private String currency;

    public Double getCurrentOutput() {
        return currentOutput;
    }

    public Double getMonthPower() {
        return monthPower;
    }

    public Double getTotalPower() {
        return totalPower;
    }

    public Double getDayIncome() {
        return dayIncome;
    }

    public Double getTotalIncome() {
        return totalIncome;
    }

    public Double getYieldRate() {
        return yieldRate;
    }

    public String getCurrency() {
        return currency;
    }
}
