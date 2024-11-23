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
package org.openhab.binding.openweathermap.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link OpenWeatherMapOneCallConfiguration} is the class used to match the
 * {@link org.openhab.binding.openweathermap.internal.handler.OpenWeatherMapOneCallHandler}s
 * configuration.
 *
 * @author Wolfgang Klimt - Initial contribution
 * @author Christoph Weitkamp - Added weather alerts
 */
@NonNullByDefault
public class OpenWeatherMapOneCallConfiguration extends OpenWeatherMapLocationConfiguration {
    public int forecastMinutes;
    public int forecastHours;
    public int forecastDays;
    public int numberOfAlerts;
}
