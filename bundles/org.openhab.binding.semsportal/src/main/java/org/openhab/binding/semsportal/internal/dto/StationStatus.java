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
package org.openhab.binding.semsportal.internal.dto;

import java.time.ZonedDateTime;

/**
 * Facade for easy access to the status of a power station. The data is distributed over different responses of the
 * portal.
 *
 * @author Iwan Bron - Initial contribution
 * @author Julio Vilmar Gesser - Migrated to the SEMS+ API
 */
public class StationStatus {
    private final Double currentOutput;
    private final Double dayTotal;
    private final Double monthTotal;
    private final Double overallTotal;
    private final Double dayIncome;
    private final Double totalIncome;
    private final boolean operational;
    private final ZonedDateTime lastUpdate;

    /**
     * All values are null if the portal does not know them.
     *
     * @param currentOutput current output in Watt
     * @param dayTotal energy generated today in kWh
     * @param monthTotal energy generated this month in kWh
     * @param overallTotal energy generated since the installation in kWh
     * @param dayIncome income of today
     * @param totalIncome income since the installation
     * @param operational whether an inverter of the station is producing
     * @param lastUpdate the time of the information
     */
    public StationStatus(Double currentOutput, Double dayTotal, Double monthTotal, Double overallTotal,
            Double dayIncome, Double totalIncome, boolean operational, ZonedDateTime lastUpdate) {
        this.currentOutput = currentOutput;
        this.dayTotal = dayTotal;
        this.monthTotal = monthTotal;
        this.overallTotal = overallTotal;
        this.dayIncome = dayIncome;
        this.totalIncome = totalIncome;
        this.operational = operational;
        this.lastUpdate = lastUpdate;
    }

    public Double getCurrentOutput() {
        return currentOutput;
    }

    public Double getDayTotal() {
        return dayTotal;
    }

    public Double getMonthTotal() {
        return monthTotal;
    }

    public Double getOverallTotal() {
        return overallTotal;
    }

    public Double getDayIncome() {
        return dayIncome;
    }

    public Double getTotalIncome() {
        return totalIncome;
    }

    public boolean isOperational() {
        return operational;
    }

    public ZonedDateTime getLastUpdate() {
        return lastUpdate;
    }
}
