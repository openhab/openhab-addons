/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.solarforecast.internal.actions;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.types.State;

/**
 * The {@link SolarForecast} Interface needed for Actions
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public interface SolarForecast {
    /**
     * Argument can be used to query an optimistic forecast scenario
     */
    public static final String OPTIMISTIC = "optimistic";
    /**
     * Argument can be used to query a pessimistic forecast scenario
     */
    public static final String PESSIMISTIC = "pessimistic";

    /**
     * Returns electric energy production for one day
     *
     * @param localDate
     * @param args possible arguments from this interface
     * @return QuantityType<Energy> in kW/h
     */
    public State getDay(LocalDate localDate, String... args);

    /**
     * Returns electric energy between two timestamps
     *
     * @param localDateTimeBegin
     * @param localDateTimeEnd
     * @param args possible arguments from this interface
     * @return QuantityType<Energy> in kW/h
     */
    public State getEnergy(LocalDateTime localDateTimeBegin, LocalDateTime localDateTimeEnd, String... args);

    /**
     * Returns electric power at one specific point of time
     *
     * @param localDateTime
     * @param args possible arguments from this interface
     * @return QuantityType<Power> in kW
     */
    public State getPower(LocalDateTime localDateTime, String... args);

    /**
     * Get the first date and time of forecast data
     *
     * @return your localized date time
     */
    public LocalDateTime getForecastBegin();

    /**
     * Get the last date and time of forecast data
     *
     * @return your localized date time
     */
    public LocalDateTime getForecastEnd();
}
