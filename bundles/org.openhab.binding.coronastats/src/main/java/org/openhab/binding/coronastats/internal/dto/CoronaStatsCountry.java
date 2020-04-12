/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.coronastats.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link CoronaStatsCountry} class holds the internal data representation of each Country
 *
 * @author Johannes Ott - Initial contribution
 */
@NonNullByDefault
public class CoronaStatsCountry {
    public String country = "";

    public String countryCode = "";

    public int cases = 0;

    public int todayCases = 0;

    public int deaths = 0;

    public int todayDeaths = 0;

    public int recovered = 0;

    public int active = 0;

    public int critical = 0;

    public int confirmed = 0;
}
