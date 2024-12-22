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
package org.openhab.binding.ephemeris.internal.handler;

import static org.openhab.binding.ephemeris.internal.EphemerisBindingConstants.*;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ephemeris.internal.EphemerisException;
import org.openhab.core.ephemeris.EphemerisManager;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link JollydayHandler} handles common parts for Jollyday file based events
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public abstract class JollydayHandler extends BaseEphemerisHandler {

    public JollydayHandler(Thing thing, EphemerisManager ephemerisManager, ZoneId zoneId) {
        super(thing, ephemerisManager, zoneId);
    }

    @Override
    protected void internalUpdate(ZonedDateTime today) throws EphemerisException {
        String todayEvent = getEvent(today);
        updateState(CHANNEL_CURRENT_EVENT, toStringType(todayEvent));

        String nextEvent = null;
        ZonedDateTime nextDay = today;

        for (int offset = 1; offset < 366 && (nextEvent == null || nextEvent.isEmpty()); offset++) {
            nextDay = today.plusDays(offset);
            nextEvent = getEvent(nextDay);
        }

        updateState(CHANNEL_NEXT_EVENT, toStringType(nextEvent));
        updateState(CHANNEL_NEXT_REMAINING,
                nextEvent != null ? new QuantityType<>(Duration.between(today, nextDay).toDays(), Units.DAY)
                        : UnDefType.UNDEF);
        updateState(CHANNEL_NEXT_START, nextEvent != null ? new DateTimeType(nextDay) : UnDefType.UNDEF);
    }

    protected abstract @Nullable String getEvent(ZonedDateTime day) throws EphemerisException;

    protected State toStringType(@Nullable String event) {
        return event == null || event.isEmpty() ? UnDefType.NULL : new StringType(event);
    }
}
