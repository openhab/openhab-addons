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
package org.openhab.binding.solarman.internal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.solarman.internal.SolarmanBindingConstants.THING_TYPE_SOLARMAN_LOGGER;

import java.time.ZoneOffset;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.solarman.internal.util.SolarmanWorkQueue;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * Tests for {@link SolarmanLoggerHandler}.
 *
 * @author Oleksandr Mishchuk - Initial contribution
 */
@NonNullByDefault
class SolarmanLoggerHandlerTest {
    // Nothing listens on port 1, so a poll fails fast and reports COMMUNICATION_ERROR
    private static final Map<String, Object> CONFIG = Map.of("hostname", "127.0.0.1", "port", 1, "serialNumber",
            "1234567890", "inverterType", "deye_sg04lp3", "refreshInterval", 3600);

    private final ThingHandlerCallback callback = (@NonNull ThingHandlerCallback) mock(ThingHandlerCallback.class);

    @Test
    void pollReportsCommunicationError() {
        SolarmanLoggerHandler handler = createHandler();

        handler.initialize();
        try {
            verify(callback, timeout(5000)).statusUpdated(any(), argThat(this::isCommunicationError));
        } finally {
            handler.dispose();
        }
    }

    @Test
    void queuedPollDoesNothingAfterDispose() {
        ScheduledExecutorService testScheduler = mock(ScheduledExecutorService.class);
        ScheduledFuture<?> future = mock(ScheduledFuture.class);
        AtomicReference<Runnable> cycle = new AtomicReference<>();
        when(testScheduler.scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class)))
                .thenAnswer(invocation -> {
                    cycle.set(invocation.getArgument(0));
                    return future;
                });
        Thing thing = ThingBuilder.create(THING_TYPE_SOLARMAN_LOGGER, "test")
                .withConfiguration(new Configuration(CONFIG)).build();
        SolarmanLoggerHandler handler = new SolarmanLoggerHandler(thing, () -> ZoneOffset.UTC) {
            @Override
            protected SolarmanWorkQueue createWorkQueue(Runnable poll, int interval, Consumer<Exception> errors) {
                return new SolarmanWorkQueue(testScheduler, poll, interval, errors);
            }
        };
        handler.setCallback(callback);
        handler.initialize();
        handler.dispose();
        Objects.requireNonNull(cycle.get()).run();
        verify(future).cancel(true);
        verify(callback, never()).statusUpdated(any(), argThat(this::isCommunicationError));
    }

    private SolarmanLoggerHandler createHandler() {
        Thing thing = ThingBuilder.create(THING_TYPE_SOLARMAN_LOGGER, "test")
                .withConfiguration(new Configuration(CONFIG)).build();
        SolarmanLoggerHandler handler = new SolarmanLoggerHandler(thing, () -> ZoneOffset.UTC);
        handler.setCallback(callback);
        return handler;
    }

    private boolean isCommunicationError(ThingStatusInfo info) {
        return info.getStatus() == ThingStatus.OFFLINE;
    }
}
