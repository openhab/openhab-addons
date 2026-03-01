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
package org.openhab.binding.energyforecast.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link EnergyForecastBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class EnergyForecastBindingConstants {

    public static final String BINDING_ID = "energyforecast";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ENERGY_FORECAST = new ThingTypeUID(BINDING_ID, "energyforecast");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ENERGY_FORECAST);

    public static final String ENERGY_FORECAST_URL = "https://www.energyforecast.de/api/v1/predictions/next_96_hours";

    public static final String CHANNEL_GROUP_PRICE = "price";
    public static final String CHANNEL_GROUP_METRIC = "metric";

    public static final String CHANNEL_PRICE_SERIES = "series";
    public static final String CHANNEL_PRICE_ORIGIN = "origin";

    public static final String CHANNEL_METRIC_FORECAST = "forecast";
    public static final String CHANNEL_METRIC_FORECAST_ERROR = "forecast-error";
    public static final String CHANNEL_METRIC_PERCENT_ERROR = "percent-error";
    public static final String CHANNEL_METRIC_MAE = "mean-abs";
    public static final String CHANNEL_METRIC_MAPE = "mean-abs-percent";
}
