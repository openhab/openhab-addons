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
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ephemeris.internal.providers.EphemerisDescriptionProvider;
import org.openhab.core.ephemeris.EphemerisManager;
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
        super(thing, ephemerisManager, zoneId, CHANNEL_HOLIDAY_TODAY, CHANNEL_HOLIDAY_TOMORROW);

        ZonedDateTime now = ZonedDateTime.now(zoneId);

        // Search all holidays in the coming year on 13 months to be sure to catch mobile holidays
        List<StateOption> stateOptions = IntStream.range(0, 398).mapToObj(offset -> now.plusDays(offset))
                .map(this::getEvent).filter(String.class::isInstance).map(String.class::cast).distinct().map(event -> {
                    String description = ephemeris.getHolidayDescription(event);
                    return new StateOption(event, description == null ? event : description);
                }).toList();

        // Set descriptions for these events
        descriptionProvider.setStateOptions(new ChannelUID(thing.getUID(), CHANNEL_CURRENT_EVENT), stateOptions);
        descriptionProvider.setStateOptions(new ChannelUID(thing.getUID(), CHANNEL_NEXT_EVENT), stateOptions);
    }

    @Override
    protected @Nullable String getEvent(ZonedDateTime day) {
        return ephemeris.getBankHolidayName(day);
    }
}
