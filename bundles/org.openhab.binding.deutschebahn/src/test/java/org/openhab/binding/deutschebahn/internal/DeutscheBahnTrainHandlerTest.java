/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.deutschebahn.internal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.deutschebahn.internal.timetable.dto.Event;
import org.openhab.binding.deutschebahn.internal.timetable.dto.TimetableStop;
import org.openhab.binding.deutschebahn.internal.timetable.dto.TripLabel;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.internal.BridgeImpl;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Tests for {@link DeutscheBahnTrainHandler}.
 * 
 * @author Sönke Küper - initial contribution.
 */
@NonNullByDefault
public class DeutscheBahnTrainHandlerTest {

    private static final String SAMPLE_PATH = "Bielefeld Hbf|Herford|Löhne(Westf)|Bad Oeynhausen|Porta Westfalica|Minden(Westf)|Bückeburg|Stadthagen|Haste|Wunstorf|Hannover Hbf|Lehrte";

    private static Configuration createConfig(int position) {
        final Configuration config = new Configuration();
        config.put("position", String.valueOf(position));
        return config;
    }

    static Thing mockThing(int id) {
        final Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(new ThingUID(DeutscheBahnBindingConstants.TRAIN_TYPE, "train-" + id));
        when(thing.getThingTypeUID()).thenReturn(DeutscheBahnBindingConstants.TRAIN_TYPE);
        when(thing.getConfiguration()).thenReturn(createConfig(id));
        ThingUID bridgeId = new ThingUID(DeutscheBahnBindingConstants.TIMETABLE_TYPE, "timetable");
        when(thing.getBridgeUID()).thenReturn(bridgeId);

        final Channel tripLabelCategory = mockChannel(thing.getUID(), "trip#category");

        final Channel arrivalPlannedTime = mockChannel(thing.getUID(), "arrival#planned-time");
        final Channel arrivalLine = mockChannel(thing.getUID(), "arrival#line");
        final Channel arrivalChangedTime = mockChannel(thing.getUID(), "arrival#changed-time");

        final Channel departurePlannedTime = mockChannel(thing.getUID(), "departure#planned-time");
        final Channel departurePlannedPlatform = mockChannel(thing.getUID(), "departure#planned-platform");
        final Channel departureTargetStation = mockChannel(thing.getUID(), "departure#planned-final-station");

        when(thing.getChannelsOfGroup("trip")).thenReturn(Arrays.asList(tripLabelCategory));
        when(thing.getChannelsOfGroup("arrival"))
                .thenReturn(Arrays.asList(arrivalPlannedTime, arrivalLine, arrivalChangedTime));
        when(thing.getChannelsOfGroup("departure"))
                .thenReturn(Arrays.asList(departurePlannedTime, departurePlannedPlatform, departureTargetStation));
        when(thing.getChannels()).thenReturn(Arrays.asList( //
                tripLabelCategory, //
                arrivalPlannedTime, arrivalLine, arrivalChangedTime, //
                departurePlannedTime, departurePlannedPlatform, departureTargetStation));

        return thing;
    }

    private static Channel mockChannel(final ThingUID thingId, final String channelId) {
        final Channel channel = Mockito.mock(Channel.class);
        when(channel.getUID()).thenReturn(new ChannelUID(thingId, channelId));
        return channel;
    }

    private static DeutscheBahnTrainHandler createAndInitHandler(final ThingHandlerCallback callback,
            final Thing thing) {
        final DeutscheBahnTrainHandler handler = new DeutscheBahnTrainHandler(thing);
        handler.setCallback(callback);
        handler.initialize();
        return handler;
    }

    private static State getDateTime(final Date day) {
        final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(day.toInstant(), ZoneId.systemDefault());
        return new DateTimeType(zonedDateTime);
    }

    @Test
    public void testUpdateChannels() {
        final Thing thing = mockThing(1);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        ThingUID bridgeId = new ThingUID(DeutscheBahnBindingConstants.TIMETABLE_TYPE, "timetable");
        when(callback.getBridge(bridgeId))
                .thenReturn(new BridgeImpl(DeutscheBahnBindingConstants.TIMETABLE_TYPE, bridgeId));
        final DeutscheBahnTrainHandler handler = createAndInitHandler(callback, thing);

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback, timeout(1000)).statusUpdated(eq(thing),
                    argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));

            // Provide data that will update the channels
            TimetableStop stop = new TimetableStop();

            TripLabel label = new TripLabel();
            label.setC("WFB");
            stop.setTl(label);

            Event arrival = new Event();
            arrival.setPt("2108161434");
            arrival.setL("RE60");
            stop.setAr(arrival);
            Event departure = new Event();
            departure.setPt("2108161435");
            departure.setPp("2");
            departure.setPpth(SAMPLE_PATH);
            stop.setDp(departure);

            handler.updateChannels(stop);

            final Date arrivalTime = new GregorianCalendar(2021, 7, 16, 14, 34).getTime();
            final Date departureTime = new GregorianCalendar(2021, 7, 16, 14, 35).getTime();

            verify(callback, timeout(1000)).stateUpdated(new ChannelUID(thing.getUID(), "trip#category"),
                    new StringType("WFB"));
            verify(callback, timeout(1000)).stateUpdated(new ChannelUID(thing.getUID(), "arrival#planned-time"),
                    getDateTime(arrivalTime));
            verify(callback, timeout(1000)).stateUpdated(new ChannelUID(thing.getUID(), "arrival#line"),
                    new StringType("RE60"));
            verify(callback, timeout(1000)).stateUpdated(new ChannelUID(thing.getUID(), "arrival#changed-time"),
                    UnDefType.NULL);
            verify(callback, timeout(1000)).stateUpdated(new ChannelUID(thing.getUID(), "departure#planned-time"),
                    getDateTime(departureTime));
            verify(callback, timeout(1000)).stateUpdated(new ChannelUID(thing.getUID(), "departure#planned-platform"),
                    new StringType("2"));
            verify(callback, timeout(1000)).stateUpdated(
                    new ChannelUID(thing.getUID(), "departure#planned-final-station"), new StringType("Lehrte"));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testUpdateChannelsWithEventNotPresent() {
        final Thing thing = mockThing(1);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        ThingUID bridgeId = new ThingUID(DeutscheBahnBindingConstants.TIMETABLE_TYPE, "timetable");
        when(callback.getBridge(bridgeId))
                .thenReturn(new BridgeImpl(DeutscheBahnBindingConstants.TIMETABLE_TYPE, bridgeId));
        final DeutscheBahnTrainHandler handler = createAndInitHandler(callback, thing);

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback, timeout(1000)).statusUpdated(eq(thing),
                    argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));

            // Provide data that will update the channels
            TimetableStop stop = new TimetableStop();

            Event arrival = new Event();
            arrival.setPt("2108161434");
            arrival.setL("RE60");
            stop.setAr(arrival);

            handler.updateChannels(stop);

            final Date arrivalTime = new GregorianCalendar(2021, 7, 16, 14, 34).getTime();

            verify(callback, timeout(1000)).stateUpdated(new ChannelUID(thing.getUID(), "trip#category"),
                    UnDefType.UNDEF);

            verify(callback, timeout(1000)).stateUpdated(new ChannelUID(thing.getUID(), "arrival#planned-time"),
                    getDateTime(arrivalTime));
            verify(callback, timeout(1000)).stateUpdated(new ChannelUID(thing.getUID(), "arrival#line"),
                    new StringType("RE60"));
            verify(callback, timeout(1000)).stateUpdated(new ChannelUID(thing.getUID(), "arrival#changed-time"),
                    UnDefType.NULL);

            verify(callback, timeout(1000)).stateUpdated(new ChannelUID(thing.getUID(), "departure#planned-time"),
                    UnDefType.UNDEF);
            verify(callback, timeout(1000)).stateUpdated(new ChannelUID(thing.getUID(), "departure#planned-platform"),
                    UnDefType.UNDEF);
            verify(callback, timeout(1000))
                    .stateUpdated(new ChannelUID(thing.getUID(), "departure#planned-final-station"), UnDefType.UNDEF);
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testWithoutBridgeStateUpdatesToOffline() {
        final Thing thing = mockThing(1);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final DeutscheBahnTrainHandler handler = createAndInitHandler(callback, thing);

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback, timeout(1000)).statusUpdated(eq(thing),
                    argThat(arg -> arg.getStatus().equals(ThingStatus.OFFLINE)));
        } finally {
            handler.dispose();
        }
    }
}
