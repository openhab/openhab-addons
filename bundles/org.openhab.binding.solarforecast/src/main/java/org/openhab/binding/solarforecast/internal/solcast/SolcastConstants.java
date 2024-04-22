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
package org.openhab.binding.solarforecast.internal.solcast;

import javax.measure.Unit;
import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.Units;

/**
 * The {@link SolcastConstants} class defines common constants for Solcast Service
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SolcastConstants {
    private static final String BASE_URL = "https://api.solcast.com.au/rooftop_sites/";
    public static final String FORECAST_URL = BASE_URL + "%s/forecasts?format=json&hours=168";
    public static final String CURRENT_ESTIMATE_URL = BASE_URL + "%s/estimated_actuals?format=json";
    public static final String BEARER = "Bearer ";
    public static final Unit<Power> KILOWATT_UNIT = MetricPrefix.KILO(Units.WATT);
}
