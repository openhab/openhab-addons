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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SolarForecastProvider} Interface needed for Actions
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public interface SolarForecastProvider {

    /**
     * Provides List of available SolarForecast Interface implementations
     *
     * @return list of SolarForecast objects
     */
    List<SolarForecast> getSolarForecasts();
}
