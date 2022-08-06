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
package org.openhab.binding.solarforecast.internal;

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
     * Returns electric energy production for one day
     *
     * @param dateString
     * @return QuaatityType<Energy> in kW/h
     */
    public State getDay(LocalDate localDate);

    /**
     * Returns electric energy between two timestamps
     *
     * @param dateTimeFrom
     * @param dateTimeTo
     * @return QuantityType<Energy> in kW/h
     */
    public State getEnergy(LocalDateTime localDateTimeBegin, LocalDateTime localDateTimeEnd);

    /**
     * Returns electric power at one specific timepoint
     *
     * @param dateTimeString
     * @return QuantityType<Power> in kW
     */
    public State getPower(LocalDateTime localDateTime);

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
