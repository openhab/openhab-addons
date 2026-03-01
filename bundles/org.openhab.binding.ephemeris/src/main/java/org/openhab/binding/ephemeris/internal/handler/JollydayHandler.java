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

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ephemeris.EphemerisManager;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;
import org.openhab.core.types.TimeSeries.Entry;
import org.openhab.core.types.UnDefType;

/**
 * The {@link JollydayHandler} handles common parts for Jollyday file based events
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public abstract class JollydayHandler extends BaseEphemerisHandler {
    private final String channelToday;
    private final String channelTomorrow;

    public JollydayHandler(Thing thing, EphemerisManager ephemerisManager, ZoneId zoneId, String channelToday,
            String channelTomorrow) {
        super(thing, ephemerisManager, zoneId);
        this.channelToday = channelToday;
        this.channelTomorrow = channelTomorrow;
    }

    @Override
    protected void internalUpdate(ZonedDateTime today) {
        TimeSeries stringTypeSeries = new TimeSeries(TimeSeries.Policy.REPLACE);
        TimeSeries onOffSeries = new TimeSeries(TimeSeries.Policy.REPLACE);

        boolean finished = false;
        for (int dayOffset = 0; !finished; dayOffset++) {
            ZonedDateTime day = today.plusDays(dayOffset);
            String event = getEvent(day);
            stringTypeSeries.add(day.toInstant(), toStringType(event));
            onOffSeries.add(day.toInstant(), OnOffType.from(event != null));
            finished = dayOffset > 365 || (dayOffset != 0 && event != null);
        }

        List<Entry> stringTypes = stringTypeSeries.getStates().toList();
        List<Entry> onOffs = onOffSeries.getStates().toList();
        updateState(channelToday, onOffs.getFirst().state());
        updateState(CHANNEL_CURRENT_EVENT, stringTypes.getFirst().state());

        updateState(channelTomorrow, onOffs.get(1).state());

        Entry last = stringTypes.getLast();
        State lastState = last.state();
        boolean lastIsEmpty = StringType.EMPTY.equals(lastState);
        updateState(CHANNEL_NEXT_EVENT, lastIsEmpty ? UnDefType.UNDEF : lastState);

        ZonedDateTime nextEventTs = last.timestamp().atZone(today.getZone());
        updateState(CHANNEL_NEXT_START, lastIsEmpty ? UnDefType.UNDEF : new DateTimeType(nextEventTs));
        updateState(CHANNEL_NEXT_REMAINING, lastIsEmpty ? UnDefType.UNDEF
                : new QuantityType<>(Duration.between(today, nextEventTs).toDays(), Units.DAY));
        sendTimeSeries(CHANNEL_CURRENT_EVENT, stringTypeSeries);
        sendTimeSeries(channelToday, onOffSeries);
    }

    protected State toStringType(@Nullable String event) {
        return event == null || event.isEmpty() ? StringType.EMPTY : new StringType(event);
    }

    protected abstract @Nullable String getEvent(ZonedDateTime day);
}
