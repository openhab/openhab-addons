/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.awattar.internal;

import static org.openhab.binding.awattar.internal.AwattarUtil.*;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Stores a non consecutive bestprice result
 *
 * @author Wolfgang Klimt - initial contribution
 */
@NonNullByDefault
public class AwattarNonConsecutiveBestPriceResult extends AwattarBestPriceResult {
    private final List<AwattarPrice> members;
    private final ZoneId zoneId;

    public AwattarNonConsecutiveBestPriceResult(List<AwattarPrice> prices, int length, boolean inverted,
            ZoneId zoneId) {
        this.zoneId = zoneId;
        members = new ArrayList<>();

        prices.sort(Comparator.naturalOrder());

        // sort in descending order when inverted
        if (inverted) {
            Collections.reverse(prices);
        }

        // take up to config.length prices
        for (int i = 0; i < Math.min(length, prices.size()); i++) {
            addMember(prices.get(i));
        }

        // sort the members
        members.sort(Comparator.comparing(AwattarPrice::timerange));
    }

    private void addMember(AwattarPrice member) {
        members.add(member);
        updateStart(member.timerange().start());
        updateEnd(member.timerange().end());
    }

    @Override
    public boolean isActive(Instant pointInTime) {
        return members.stream().anyMatch(x -> x.timerange().contains(pointInTime.toEpochMilli()));
    }

    @Override
    public String toString() {
        return String.format("NonConsecutiveBestpriceResult with %s", members.toString());
    }

    @Override
    public String getHours() {
        boolean second = false;
        StringBuilder res = new StringBuilder();

        for (AwattarPrice price : members) {
            if (second) {
                res.append(',');
            }
            res.append(getHourFrom(price.timerange().start(), zoneId));
            second = true;
        }
        return res.toString();
    }
}
