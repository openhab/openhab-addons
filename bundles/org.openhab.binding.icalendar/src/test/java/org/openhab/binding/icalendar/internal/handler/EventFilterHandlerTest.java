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
package org.openhab.binding.icalendar.internal.handler;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.verify;

import java.time.ZoneId;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.icalendar.internal.logic.AbstractPresentableCalendar;
import org.openhab.binding.icalendar.internal.logic.EventTimeFilter;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.binding.builder.ThingStatusInfoBuilder;

/**
 * Tests for {@link EventFilterHandler}.
 *
 * @author Christian Heinemann - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class EventFilterHandlerTest {

    private @Mock @NonNullByDefault({}) TimeZoneProvider timeZoneProvider;
    private @Mock @NonNullByDefault({}) ThingHandlerCallback thingHandlerCallback;
    private @Mock @NonNullByDefault({}) ICalendarHandler iCalendarHandler;
    private @Mock @NonNullByDefault({}) AbstractPresentableCalendar calendar;

    private @NonNullByDefault({}) EventFilterHandler eventFilterHandler;

    @BeforeEach
    public void setUp() {
        Configuration configuration = new Configuration();
        configuration.put("maxEvents", "1");
        configuration.put("datetimeStart", "0");
        configuration.put("datetimeEnd", "1");
        configuration.put("datetimeRound", "true");
        configuration.put("datetimeUnit", "DAY");

        doReturn(ZoneId.of("UTC")).when(timeZoneProvider).getTimeZone();
        doReturn(calendar).when(iCalendarHandler).getRuntimeCalendar();

        Bridge iCalendarBridge = BridgeBuilder.create(new ThingTypeUID("icalendar", "calendar"), "test").build();
        iCalendarBridge.setStatusInfo(ThingStatusInfoBuilder.create(ThingStatus.ONLINE).build());
        iCalendarBridge.setHandler(iCalendarHandler);

        Thing eventFilterThing = ThingBuilder.create(new ThingTypeUID("icalendar", "eventfilter"), "test")
                .withBridge(iCalendarBridge.getUID()).withConfiguration(configuration).build();

        eventFilterHandler = new EventFilterHandler(eventFilterThing, timeZoneProvider);
        eventFilterHandler.setCallback(thingHandlerCallback);

        doReturn(iCalendarBridge).when(thingHandlerCallback).getBridge(iCalendarBridge.getUID());
    }

    @Test
    public void testSearchWithDefaultMode() {
        eventFilterHandler.getThing().getConfiguration().remove("datetimeMode");
        doCalendarUpdate();
        verify(calendar).getFilteredEventsBetween(any(), any(), eq(EventTimeFilter.searchByStart()), isNull(), eq(1));
    }

    @Test
    public void testSearchByStart() {
        eventFilterHandler.getThing().getConfiguration().put("datetimeMode", "START");
        doCalendarUpdate();
        verify(calendar).getFilteredEventsBetween(any(), any(), eq(EventTimeFilter.searchByStart()), isNull(), eq(1));
    }

    @Test
    public void testSearchByEnd() {
        eventFilterHandler.getThing().getConfiguration().put("datetimeMode", "END");
        doCalendarUpdate();
        verify(calendar).getFilteredEventsBetween(any(), any(), eq(EventTimeFilter.searchByEnd()), isNull(), eq(1));
    }

    @Test
    public void testSearchByActive() {
        eventFilterHandler.getThing().getConfiguration().put("datetimeMode", "ACTIVE");
        doCalendarUpdate();
        verify(calendar).getFilteredEventsBetween(any(), any(), eq(EventTimeFilter.searchByActive()), isNull(), eq(1));
    }

    @Test
    public void testInvalidDatetimeModeConfigValue() {
        eventFilterHandler.getThing().getConfiguration().put("datetimeMode", "DUMMY");
        doCalendarUpdate();

        ThingStatusInfo expectedThingStatusInfo = ThingStatusInfoBuilder.create(ThingStatus.OFFLINE)
                .withStatusDetail(ThingStatusDetail.CONFIGURATION_ERROR)
                .withDescription("datetimeMode is not set properly.").build();
        verify(thingHandlerCallback).statusUpdated(eventFilterHandler.getThing(), expectedThingStatusInfo);
    }

    private void doCalendarUpdate() {
        eventFilterHandler.initialize();
        eventFilterHandler.getThing().setStatusInfo(ThingStatusInfoBuilder.create(ThingStatus.ONLINE).build());
        eventFilterHandler.onCalendarUpdated();
    }
}
