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

import static org.openhab.binding.coronastats.internal.CoronaStatsBindingConstants.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.annotations.SerializedName;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.types.State;

/**
 * The {@link CoronaStats} class is internal CoronaStats structure.
 *
 * @author Johannes Ott - Initial contribution
 */
@NonNullByDefault
public class CoronaStats {
    private final Date created = new Date();

    @SerializedName("data")
    private @Nullable Set<CoronaStatsCountry> countries;

    @SerializedName("worldStats")
    private @Nullable CoronaStatsCountry world;

    public Map<String, State> getChannelsStateMap() {
        Map<String, State> map = new HashMap<>();

        ZonedDateTime zoned = ZonedDateTime.ofInstant(created.toInstant(), ZoneId.systemDefault());
        map.put(CHANNEL_REFRESHED, new DateTimeType(zoned));

        return Collections.unmodifiableMap(map);
    }

    public @Nullable CoronaStatsCountry getCountry(String countryCodeKey) {
        final Set<CoronaStatsCountry> localCountries = countries;
        if (localCountries != null) {
            for (CoronaStatsCountry country : localCountries) {
                if (country.countryCode.equals(countryCodeKey)) {
                    return country;
                }
            }
        }

        return null;
    }
}
