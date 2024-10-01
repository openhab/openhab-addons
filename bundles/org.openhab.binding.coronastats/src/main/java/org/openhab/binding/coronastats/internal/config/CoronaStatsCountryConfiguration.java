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
package org.openhab.binding.coronastats.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration for the
 * {@link org.openhab.binding.coronastats.internal.handler.CoronaStatsCountryHandler}
 *
 * @author Johannes Ott - Initial contribution
 */
@NonNullByDefault
public class CoronaStatsCountryConfiguration {
    private String countryCode = "";

    public String getCountryCode() {
        return countryCode.toUpperCase();
    }

    public boolean isValid() {
        return !"".equals(countryCode);
    }
}
