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
package org.openhab.binding.samsungac.handler;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * The {@link PowerUsage} class handles the Power Usage
 *
 * @author Jan Grønlien - Initial contribution
 * @author Kai Kreuzer - Refactoring as preparation for openHAB contribution
 */

@NonNullByDefault
public class PowerUsage {
    LocalDateTime currentDate = LocalDateTime.now();
    BigDecimal currentPowerUsage = new BigDecimal(0);
    BigDecimal currentRunningTime = new BigDecimal(0);

    BigDecimal ten = new BigDecimal("10");

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHH");

    /**
     * @return the date
     */
    public LocalDateTime getCurrentDate() {
        return currentDate;
    }

    /**
     * @param date the date to set
     * @throws ParseException
     */
    public void setCurrentDate(String date) throws ParseException {
        this.currentDate = LocalDateTime.parse(date, DATE_FORMAT);
    }

    /**
     * @return the powerUsage
     */
    public BigDecimal getCurrentPowerUsage() {
        return currentPowerUsage;
    }

    /**
     * @param powerUsage the powerUsage to set
     */
    public void setCurrentPowerUsage(BigDecimal powerUsage) {
        this.currentPowerUsage = powerUsage.divide(new BigDecimal(10)); // KWh
    }

    /**
     * @param powerUsage the powerUsage to set
     */
    public void setCurrentPowerUsage(String powerUsage) {
        this.setCurrentPowerUsage(new BigDecimal(powerUsage));
    }

    /**
     * @return the runningTime
     */
    public BigDecimal getCurrentRunningTime() {
        return currentRunningTime;
    }

    /**
     * @param runningTime the runningTime to set
     */
    public void setCurrentRunningTime(BigDecimal runningTime) {
        this.currentRunningTime = runningTime.divide(ten);
    }

    /**
     * @param runningTime the runningTime to set
     */
    public void setCurrentRunningTime(String runningTime) {
        setCurrentRunningTime(new BigDecimal(runningTime));
    }

    @Override
    public String toString() {
        return (currentDate.toString() + " - " + currentPowerUsage.toString() + " - " + currentRunningTime.toString());
    }
}
