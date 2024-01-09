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
package org.openhab.binding.solarforecast.internal.actions;

import java.time.Instant;
import java.time.LocalDate;

import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.QuantityType;

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
     * @param date
     * @param args possible arguments from this interface
     * @return QuantityType<Energy> in kW/h
     */
    public QuantityType<Energy> getDay(LocalDate date, String... args);

    /**
     * Returns electric energy between two timestamps
     *
     * @param start
     * @param end
     * @param args possible arguments from this interface
     * @return QuantityType<Energy> in kW/h
     */
    public QuantityType<Energy> getEnergy(Instant start, Instant end, String... args);

    /**
     * Returns electric power at one specific point of time
     *
     * @param timestamp
     * @param args possible arguments from this interface
     * @return QuantityType<Power> in kW
     */
    public QuantityType<Power> getPower(Instant timestamp, String... args);

    /**
     * Get the first date and time of forecast data
     *
     * @return date time
     */
    public Instant getForecastBegin();

    /**
     * Get the last date and time of forecast data
     *
     * @return date time
     */
    public Instant getForecastEnd();
}
