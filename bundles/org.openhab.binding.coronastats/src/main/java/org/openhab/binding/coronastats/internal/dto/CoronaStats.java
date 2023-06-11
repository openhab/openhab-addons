/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link CoronaStats} class is internal CoronaStats structure.
 *
 * @author Johannes Ott - Initial contribution
 */
@NonNullByDefault
public class CoronaStats {
    @SerializedName("data")
    private @Nullable Set<CoronaStatsCountry> countries;

    @SerializedName("worldStats")
    private @Nullable CoronaStatsWorld world;

    public @Nullable CoronaStatsCountry getCountry(String countryCodeKey) {
        final Set<CoronaStatsCountry> localCountries = countries;
        if (localCountries != null) {
            for (CoronaStatsCountry country : localCountries) {
                if (country.getCountryCode().equals(countryCodeKey)) {
                    return country;
                }
            }
        }

        return null;
    }

    public @Nullable CoronaStatsWorld getWorld() {
        return world;
    }
}
