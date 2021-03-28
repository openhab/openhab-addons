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
package org.openhab.binding.ahawastecollection.internal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.scheduler.CronJob;
import org.openhab.core.scheduler.CronScheduler;
import org.openhab.core.scheduler.ScheduledCompletableFuture;
import org.openhab.core.scheduler.SchedulerRunnable;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.State;

/**
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
public class AhaWasteCollectionHandlerTest {

    private @Nullable ThingHandler handler;
    private @Nullable ThingHandlerCallback callback;
    private @Nullable Thing thing;

    @BeforeEach
    public void setUp() {
        final Configuration config = new Configuration();
        config.put("commune", "Hannover");
        config.put("collectionPlace", "02095-0010+");
        config.put("houseNumber", "10");
        config.put("houseNumberAddon", "");
        config.put("street", "02095@Oesterleystr.+/+Südstadt@Südstadt");

        this.thing = mockThing(config);

        // Stub-Scheduler that executes the command synchronous
        @SuppressWarnings("unchecked")
        final CronScheduler scheduler = new CronScheduler() {

            @Override
            public ScheduledCompletableFuture<Void> schedule(final CronJob cronJob, final Map<String, Object> config,
                    final String cronExpression) {
                try {
                    cronJob.run(config);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
                return Mockito.mock(ScheduledCompletableFuture.class);
            }

            @Override
            public ScheduledCompletableFuture<Void> schedule(final SchedulerRunnable runnable,
                    final String cronExpression) {
                try {
                    runnable.run();
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
                return Mockito.mock(ScheduledCompletableFuture.class);
            }
        };

        this.handler = new AhaWasteCollectionHandler(this.getThing(), scheduler, ZoneId::systemDefault,
                new AhaCollectionScheduleStubFactory());
        this.callback = mock(ThingHandlerCallback.class);
        this.handler.setCallback(this.callback);
    }

    private static Thing mockThing(final Configuration config) {
        final Thing thing = mock(Thing.class);
        when(thing.getUID())
                .thenReturn(new ThingUID(AhaWasteCollectionBindingConstants.THING_TYPE_SCHEDULE, "collectionCalendar"));
        when(thing.getConfiguration()).thenReturn(config);

        final Channel channelBioWaste = mockChannel(thing.getUID(), AhaWasteCollectionBindingConstants.BIOWASTE);
        final Channel channelGeneralWaste = mockChannel(thing.getUID(),
                AhaWasteCollectionBindingConstants.GENERAL_WASTE);
        final Channel channelPaper = mockChannel(thing.getUID(), AhaWasteCollectionBindingConstants.PAPER);
        final Channel channelLeightweightPackaging = mockChannel(thing.getUID(),
                AhaWasteCollectionBindingConstants.LEIGHTWEIGHT_PACKAGING);

        when(thing.getChannels()).thenReturn(
                Arrays.asList(channelBioWaste, channelGeneralWaste, channelLeightweightPackaging, channelPaper));
        return thing;
    }

    private static Channel mockChannel(final ThingUID thingId, final String channelId) {
        final Channel channel = Mockito.mock(Channel.class);
        when(channel.getUID()).thenReturn(new ChannelUID(thingId, channelId));
        return channel;
    }

    // Needed because of NotNullCheck will fail otherwise when using attribute thing in mockito verify.
    private Thing getThing() {
        if (this.thing != null) {
            return this.thing;
        } else {
            throw new AssertionError();
        }
    }

    @AfterEach
    public void tearDown() {
        this.handler.dispose();
    }

    @Test
    public void testUpdateChannels() {
        this.handler.initialize();
        verify(this.callback).statusUpdated(eq(this.getThing()),
                argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
        verify(this.callback, timeout(1000)).statusUpdated(eq(this.thing),
                argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));
        verify(this.callback, timeout(1000)).stateUpdated(
                new ChannelUID(this.thing.getUID(), AhaWasteCollectionBindingConstants.BIOWASTE),
                getDateTime(AhaCollectionScheduleStub.BIO_WASTE_DATE));
        verify(this.callback, timeout(1000)).stateUpdated(
                new ChannelUID(this.thing.getUID(), AhaWasteCollectionBindingConstants.GENERAL_WASTE),
                getDateTime(AhaCollectionScheduleStub.GENERAL_WASTE_DATE));
        verify(this.callback, timeout(1000)).stateUpdated(
                new ChannelUID(this.thing.getUID(), AhaWasteCollectionBindingConstants.LEIGHTWEIGHT_PACKAGING),
                getDateTime(AhaCollectionScheduleStub.LEIGHTWEIGHT_PACKAGING_DATE));
        verify(this.callback, timeout(1000)).stateUpdated(
                new ChannelUID(this.thing.getUID(), AhaWasteCollectionBindingConstants.PAPER),
                getDateTime(AhaCollectionScheduleStub.PAPER_DATE));
    }

    private static State getDateTime(final Date day) {
        final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(day.toInstant(), ZoneId.systemDefault());
        return new DateTimeType(zonedDateTime);
    }
}
