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
package org.openhab.binding.energyforecast.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link EnergyForecastConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class EnergyForecastConfiguration {

    public String zone = "";
    public String token = "";
    public double fixCost = 0.0;
    public double vat = 0.0;
    public String resolution = "PT15M";
    public int refreshInterval = 180;
    public int errorLimit = 0;
}
