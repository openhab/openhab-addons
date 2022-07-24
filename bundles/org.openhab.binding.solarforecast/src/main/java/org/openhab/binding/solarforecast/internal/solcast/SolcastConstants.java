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
package org.openhab.binding.solarforecast.internal.solcast;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SolcastConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SolcastConstants {
    public static final String FORECAST_URL = "https://api.solcast.com.au/rooftop_sites/%s/forecasts?format=json";
    public static final String CURRENT_ESTIMATE_URL = "https://api.solcast.com.au/rooftop_sites/%s/estimated_actuals?format=json";
}
