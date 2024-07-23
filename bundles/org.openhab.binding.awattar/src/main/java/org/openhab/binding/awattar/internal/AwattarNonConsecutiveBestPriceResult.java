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
package org.openhab.binding.awattar.internal;

import static org.openhab.binding.awattar.internal.AwattarUtil.*;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
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
    private boolean sorted = true;

    public AwattarNonConsecutiveBestPriceResult(ZoneId zoneId) {
        super();
        this.zoneId = zoneId;
        members = new ArrayList<>();
    }

    public void addMember(AwattarPrice member) {
        sorted = false;
        members.add(member);
        updateStart(member.timerange().start());
        updateEnd(member.timerange().end());
    }

    @Override
    public boolean isActive() {
        return members.stream().anyMatch(x -> x.timerange().contains(Instant.now().toEpochMilli()));
    }

    @Override
    public String toString() {
        return String.format("NonConsecutiveBestpriceResult with %s", members.toString());
    }

    private void sort() {
        if (!sorted) {
            members.sort(Comparator.comparingLong(p -> p.timerange().start()));
        }
    }

    @Override
    public String getHours() {
        boolean second = false;
        sort();
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
