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
package org.openhab.binding.coronastats.internal.dto;

import static org.openhab.binding.coronastats.internal.CoronaStatsBindingConstants.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link CoronaStatsCountry} class holds the internal data representation of each Country
 *
 * @author Johannes Ott - Initial contribution
 */
@NonNullByDefault
public class CoronaStatsCountry extends CoronaStatsCases {
    private String country = "";
    private String countryCode = "";
    private int tests = -1;
    private long updated = -1;

    public String getCountryCode() {
        return countryCode;
    }

    public Map<String, String> getProperties() {
        Map<String, String> map = new HashMap<>();
        map.put(PROPERTY_COUNTRY, country);
        return Collections.unmodifiableMap(map);
    }

    public Map<String, State> getChannelsStateMap() {
        Map<String, State> map = super.getCaseChannelsStateMap();

        map.put(CHANNEL_TESTS, parseToState(tests));

        if (updated == -1) {
            map.put(CHANNEL_UPDATED, UnDefType.NULL);
        } else {
            Date date = new Date(updated);
            ZonedDateTime zoned = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
            map.put(CHANNEL_UPDATED, new DateTimeType(zoned));
        }

        return Collections.unmodifiableMap(map);
    }
}
