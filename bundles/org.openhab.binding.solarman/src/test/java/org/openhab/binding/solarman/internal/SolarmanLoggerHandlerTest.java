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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.openhab.binding.solarman.internal.SolarmanBindingConstants.THING_TYPE_SOLARMAN_LOGGER;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
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
    void pollWaitingForMonitorDoesNothingAfterDispose() throws InterruptedException {
        SolarmanLoggerHandler handler = createHandler();

        // Holding the handler monitor keeps the initial poll waiting in queryLogger() until after dispose()
        synchronized (handler) {
            handler.initialize();
            Thread.sleep(500);
            handler.dispose();
        }

        verify(callback, after(2000).never()).statusUpdated(any(), argThat(this::isCommunicationError));
    }

    private SolarmanLoggerHandler createHandler() {
        Thing thing = ThingBuilder.create(THING_TYPE_SOLARMAN_LOGGER, "test")
                .withConfiguration(new Configuration(CONFIG)).build();
        SolarmanLoggerHandler handler = new SolarmanLoggerHandler(thing);
        handler.setCallback(callback);
        return handler;
    }

    private boolean isCommunicationError(ThingStatusInfo info) {
        return info.getStatus() == ThingStatus.OFFLINE;
    }
}
