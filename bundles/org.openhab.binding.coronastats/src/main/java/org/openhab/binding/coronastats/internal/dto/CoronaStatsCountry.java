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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * The {@link CoronaStatsCountry} class holds the internal data representation of each Country
 *
 * @author Johannes Ott - Initial contribution
 */
@NonNullByDefault
public class CoronaStatsCountry {
    public String country = "";

    public String countryCode = "";

    public int cases = -1;

    public int todayCases = -1;

    public int deaths = -1;

    public int todayDeaths = -1;

    public int recovered = -1;

    public int active = -1;

    public int critical = -1;

    public int tests = -1;

    public long updated = -1;

    public Map<String, String> getProperties() {
        Map<String, String> map = new HashMap<>();
        map.put(PROPERTY_COUNTRY, country);
        return Collections.unmodifiableMap(map);
    }

    public Map<String, State> getChannelsStateMap() {
        Map<String, State> map = new HashMap<>();

        map.put(CHANNEL_CASES, parseToState(cases));
        map.put(CHANNEL_NEW_CASES, parseToState(todayCases));
        map.put(CHANNEL_DEATHS, parseToState(deaths));
        map.put(CHANNEL_NEW_DEATHS, parseToState(todayDeaths));
        map.put(CHANNEL_RECOVERED, parseToState(recovered));
        map.put(CHANNEL_ACTIVE, parseToState(active));
        map.put(CHANNEL_CRITICAL, parseToState(critical));

        if (!"World".equals(countryCode)) {
            map.put(CHANNEL_TESTS, parseToState(cases));
        }

        return Collections.unmodifiableMap(map);
    }

    private State parseToState(int count) {
        if (count == -1) {
            return UnDefType.NULL;
        } else {
            return new DecimalType(count);
        }
    }
}
