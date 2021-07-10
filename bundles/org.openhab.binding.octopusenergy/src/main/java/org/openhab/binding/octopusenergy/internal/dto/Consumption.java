/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.octopusenergy.internal.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.octopusenergy.internal.OctopusEnergyBindingConstants;

/**
 * The {@link Consumption} is a DTO class representing an agreement or contract.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class Consumption {
    // {
    // "consumption":0.283,
    // "interval_start":"2021-01-17T20:30:00Z",
    // "interval_end":"2021-01-17T21:00:00Z"
    // }

    public static final Comparator<Consumption> CONSUMPTION_ORDER_ASC = new Comparator<Consumption>() {
        @Override
        public int compare(Consumption c1, Consumption c2) {
            return c1.consumption.compareTo(c2.consumption);
        }
    };
    public static final Comparator<Consumption> INTERVAL_START_ORDER_ASC = new Comparator<Consumption>() {
        @Override
        public int compare(Consumption c1, Consumption c2) {
            return c1.intervalStart.compareTo(c2.intervalStart);
        }
    };

    /**
     * Aggregates two lists of consumption data into a single list. If energy data is available in both lists for the
     * same interval, this will be added together for the result list.
     *
     * The code assumes that there are no duplicate intervals within a single list and all intervals are the same size.
     *
     * @param list1
     * @param list2
     * @return
     *         the aggregated list.
     */
    public static final List<Consumption> aggregate(List<Consumption> list1, List<Consumption> list2) {
        List<Consumption> slist = new ArrayList<>(list1);
        slist.addAll(list2);
        Collections.sort(slist, INTERVAL_START_ORDER_ASC);
        List<Consumption> result = new ArrayList<>();
        int i;
        for (i = 0; i < (slist.size() - 1); i++) {
            Consumption c1 = slist.get(i);
            Consumption c2 = slist.get(i + 1);
            if (c1.intervalStart.equals(c2.intervalStart)) {
                c1.consumption = c1.consumption.add(c2.consumption);
                i++;
            }
            result.add(c1);
        }
        if (i < slist.size()) {
            result.add(slist.get(i));
        }
        return result;
    }

    public Consumption(BigDecimal consumption, ZonedDateTime intervalStart, ZonedDateTime intervalEnd) {
        this.consumption = consumption;
        this.intervalStart = intervalStart;
        this.intervalEnd = intervalEnd;
    }

    public BigDecimal consumption = BigDecimal.ZERO;
    public ZonedDateTime intervalStart = OctopusEnergyBindingConstants.UNDEFINED_TIME;
    public ZonedDateTime intervalEnd = OctopusEnergyBindingConstants.UNDEFINED_TIME;

    @Override
    public String toString() {
        return "Consumption(" + intervalStart + "," + intervalEnd + "," + consumption + ")";
    }
}
