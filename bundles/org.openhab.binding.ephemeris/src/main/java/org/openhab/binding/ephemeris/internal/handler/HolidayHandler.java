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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ephemeris.internal.EphemerisException;
import org.openhab.binding.ephemeris.internal.providers.EphemerisDescriptionProvider;
import org.openhab.core.ephemeris.EphemerisManager;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.StateOption;

/**
 * The {@link HolidayHandler} delivers system default Holidays data.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class HolidayHandler extends JollydayHandler {

    public HolidayHandler(Thing thing, EphemerisManager ephemerisManager, ZoneId zoneId,
            EphemerisDescriptionProvider descriptionProvider) {
        super(thing, ephemerisManager, zoneId);

        // Search all holidays in the coming year, using a map to avoid duplicates
        Map<String, StateOption> events = new HashMap<>();
        ZonedDateTime now = ZonedDateTime.now();
        // Scans 13 monthes to be sure to catch mobile holidays
        for (int offset = 0; offset < 398; offset++) {
            String event = getEvent(now.plusDays(offset));
            if (event != null) {
                String description = ephemeris.getHolidayDescription(event);
                events.put(event, new StateOption(event, description == null ? event : description));
            }
        }

        // Set descriptions for these events
        List<StateOption> stateOptions = events.values().stream().toList();
        descriptionProvider.setStateOptions(new ChannelUID(thing.getUID(), CHANNEL_CURRENT_EVENT), stateOptions);
        descriptionProvider.setStateOptions(new ChannelUID(thing.getUID(), CHANNEL_NEXT_EVENT), stateOptions);
    }

    @Override
    protected void internalUpdate(ZonedDateTime today) throws EphemerisException {
        updateState(CHANNEL_HOLIDAY_TODAY, OnOffType.from(getEvent(today) != null));
        updateState(CHANNEL_HOLIDAY_TOMORROW, OnOffType.from(getEvent(today.plusDays(1)) != null));
        super.internalUpdate(today);
    }

    @Override
    protected @Nullable String getEvent(ZonedDateTime day) {
        return ephemeris.getBankHolidayName(day);
    }
}
