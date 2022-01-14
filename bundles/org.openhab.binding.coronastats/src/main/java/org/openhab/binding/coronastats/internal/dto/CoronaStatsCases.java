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
package org.openhab.binding.coronastats.internal.dto;

import static org.openhab.binding.coronastats.internal.CoronaStatsBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import javax.measure.quantity.Dimensionless;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link CoronaStatsCountry} class holds the internal data representation of each Country
 *
 * @author Johannes Ott - Initial contribution
 */
@NonNullByDefault
public class CoronaStatsCases {
    private int cases = -1;
    private int todayCases = -1;
    private int deaths = -1;
    private int todayDeaths = -1;
    private int recovered = -1;
    private int active = -1;
    private int critical = -1;

    protected Map<String, State> getCaseChannelsStateMap() {
        Map<String, State> map = new HashMap<>();

        map.put(CHANNEL_CASES, parseToState(cases));
        map.put(CHANNEL_NEW_CASES, parseToState(todayCases));
        map.put(CHANNEL_DEATHS, parseToState(deaths));
        map.put(CHANNEL_NEW_DEATHS, parseToState(todayDeaths));
        map.put(CHANNEL_RECOVERED, parseToState(recovered));
        map.put(CHANNEL_ACTIVE, parseToState(active));
        map.put(CHANNEL_CRITICAL, parseToState(critical));

        return map;
    }

    protected State parseToState(int count) {
        if (count == -1) {
            return UnDefType.NULL;
        } else {
            return new QuantityType<Dimensionless>(count, Units.ONE);
        }
    }
}
