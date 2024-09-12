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
package org.openhab.binding.ahawastecollection.internal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
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
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.State;

/**
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
public class AhaWasteCollectionHandlerTest {

    private static final Configuration CONFIG = createConfig();

    private static Configuration createConfig() {
        final Configuration config = new Configuration();
        config.put("commune", "Hannover");
        config.put("collectionPlace", "02095-0010+");
        config.put("houseNumber", "10");
        config.put("houseNumberAddon", "");
        config.put("street", "02095@Oesterleystr.+/+Südstadt@Südstadt");
        return config;
    }

    /**
     * Exception indicating that the execution of a script within the stub-Scheduler failed.
     */
    private static class SchedulerRuntimeException extends RuntimeException {

        private static final long serialVersionUID = -1262671065082256315L;

        public SchedulerRuntimeException(@Nullable final Throwable cause) {
            super(cause);
        }
    }

    /**
     * Creates a {@link CronScheduler} that executes all commands synchronous.
     */
    @SuppressWarnings("unchecked")
    private static CronScheduler createStubScheduler() {
        return new CronScheduler() {

            @Override
            public ScheduledCompletableFuture<Void> schedule(final CronJob cronJob, final Map<String, Object> config,
                    final String cronExpression) {
                try {
                    cronJob.run(config);
                } catch (final Exception e) {
                    throw new SchedulerRuntimeException(e);
                }
                return Mockito.mock(ScheduledCompletableFuture.class);
            }

            @Override
            public ScheduledCompletableFuture<Void> schedule(final SchedulerRunnable runnable,
                    final String cronExpression) {
                try {
                    runnable.run();
                } catch (final Exception e) {
                    throw new SchedulerRuntimeException(e);
                }
                return Mockito.mock(ScheduledCompletableFuture.class);
            }
        };
    }

    private static Thing mockThing() {
        final Thing thing = mock(Thing.class);
        when(thing.getUID())
                .thenReturn(new ThingUID(AhaWasteCollectionBindingConstants.THING_TYPE_SCHEDULE, "collectionCalendar"));
        when(thing.getConfiguration()).thenReturn(CONFIG);

        final Channel channelBioWaste = mockChannel(thing.getUID(), AhaWasteCollectionBindingConstants.BIOWASTE);
        final Channel channelGeneralWaste = mockChannel(thing.getUID(),
                AhaWasteCollectionBindingConstants.GENERAL_WASTE);
        final Channel channelPaper = mockChannel(thing.getUID(), AhaWasteCollectionBindingConstants.PAPER);
        final Channel channelLightweightPackaging = mockChannel(thing.getUID(),
                AhaWasteCollectionBindingConstants.LEIGHTWEIGHT_PACKAGING);

        when(thing.getChannels()).thenReturn(
                Arrays.asList(channelBioWaste, channelGeneralWaste, channelLightweightPackaging, channelPaper));
        return thing;
    }

    private static Channel mockChannel(final ThingUID thingId, final String channelId) {
        final Channel channel = Mockito.mock(Channel.class);
        when(channel.getUID()).thenReturn(new ChannelUID(thingId, channelId));
        return channel;
    }

    private static AhaWasteCollectionHandler createAndInitHandler(final ThingHandlerCallback callback,
            final Thing thing) {
        // Executor that executes all commands synchronous.
        final ScheduledExecutorService executorStub = Mockito.mock(ScheduledExecutorService.class);
        doAnswer((InvocationOnMock invocation) -> {
            ((Runnable) invocation.getArguments()[0]).run();
            return null;
        }).when(executorStub).execute(any(Runnable.class));

        final AhaWasteCollectionHandler handler = new AhaWasteCollectionHandler(thing, createStubScheduler(),
                ZoneId::systemDefault, new AhaCollectionScheduleStubFactory(), executorStub);
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
        final Thing thing = mockThing();
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final AhaWasteCollectionHandler handler = createAndInitHandler(callback, thing);

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));
            verify(callback).stateUpdated(new ChannelUID(thing.getUID(), AhaWasteCollectionBindingConstants.BIOWASTE),
                    getDateTime(AhaCollectionScheduleStub.BIO_WASTE_DATE));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), AhaWasteCollectionBindingConstants.GENERAL_WASTE),
                    getDateTime(AhaCollectionScheduleStub.GENERAL_WASTE_DATE));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), AhaWasteCollectionBindingConstants.LEIGHTWEIGHT_PACKAGING),
                    getDateTime(AhaCollectionScheduleStub.LEIGHTWEIGHT_PACKAGING_DATE));
            verify(callback).stateUpdated(new ChannelUID(thing.getUID(), AhaWasteCollectionBindingConstants.PAPER),
                    getDateTime(AhaCollectionScheduleStub.PAPER_DATE));
        } finally {
            handler.dispose();
        }
    }
}
