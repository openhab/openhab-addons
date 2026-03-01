/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.ephemeris.internal.handler;

import static org.openhab.binding.ephemeris.internal.EphemerisBindingConstants.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ephemeris.internal.configuration.WeekendConfiguration;
import org.openhab.core.ephemeris.EphemerisManager;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;
import org.openhab.core.types.TimeSeries.Entry;

/**
 * The {@link WeekendHandler} delivers system default Weekend data.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class WeekendHandler extends BaseEphemerisHandler {
    private int days;

    public WeekendHandler(Thing thing, EphemerisManager ephemerisManager, ZoneId zoneId) {
        super(thing, ephemerisManager, zoneId);
    }

    @Override
    public void initialize() {
        WeekendConfiguration config = getConfigAs(WeekendConfiguration.class);
        days = Math.max(1, config.days);
        super.initialize();
    }

    @Override
    protected void internalUpdate(ZonedDateTime today) {
        TimeSeries weekendSeries = new TimeSeries(TimeSeries.Policy.REPLACE);
        for (int dayOffset = 0; dayOffset <= days; dayOffset++) {
            ZonedDateTime day = today.plusDays(dayOffset);
            weekendSeries.add(day.toInstant(), getDayStatus(day));
        }
        sendTimeSeries(CHANNEL_TODAY, weekendSeries);

        List<Entry> statuses = weekendSeries.getStates().toList();
        updateState(CHANNEL_TODAY, statuses.getFirst().state());
        updateState(CHANNEL_TOMORROW, statuses.get(1).state());
    }

    protected State getDayStatus(ZonedDateTime day) {
        return OnOffType.from(ephemeris.isWeekend(day));
    }
}
