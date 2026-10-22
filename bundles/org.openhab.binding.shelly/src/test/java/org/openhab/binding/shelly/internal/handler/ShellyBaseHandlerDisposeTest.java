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
package org.openhab.binding.shelly.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.THING_TYPE_SHELLYPLUS1PM;

import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyApiInterface;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1CoapHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.LoggerFactory;

/**
 * Verifies that {@link ShellyBaseHandler#refreshStatus} does not publish thing status or channels for a handler
 * that was disposed while the blocking status refresh was in flight.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault({})
@SuppressWarnings("null")
class ShellyBaseHandlerDisposeTest {

    @Test
    void refreshStatusDisposedDuringApiCallDoesNotPublish() throws Exception {
        ShellyBaseHandler handler = mock(ShellyBaseHandler.class, CALLS_REAL_METHODS);
        ShellyApiInterface api = mock(ShellyApiInterface.class);
        Thing thing = mock(Thing.class);
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUS1PM);
        profile.initialized = true;
        profile.alwaysOn = false;

        setField(handler, "api", api);
        setField(handler, "logger", LoggerFactory.getLogger(ShellyBaseHandler.class));
        setField(handler, "vibrationFilter", new AtomicInteger());
        setField(handler, "initLock", new Object());
        handler.profile = profile;
        handler.scheduledUpdates = 1;
        doReturn(thing).when(handler).getThing();
        when(thing.getStatus()).thenReturn(ThingStatus.ONLINE);
        doReturn(ThingStatusDetail.NONE).when(handler).getThingStatusDetail();
        when(api.getStatus()).thenAnswer(invocation -> {
            setField(handler, "stopping", true);
            return new ShellySettingsStatus();
        });

        handler.refreshStatus();

        verify(handler, never()).setThingOnline();
    }

    @Test
    void refreshStatusDisposedDuringApiCallExceptionDoesNotSetOffline() throws Exception {
        ShellyBaseHandler handler = mock(ShellyBaseHandler.class, CALLS_REAL_METHODS);
        ShellyApiInterface api = mock(ShellyApiInterface.class);
        Thing thing = mock(Thing.class);
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUS1PM);
        profile.initialized = true;
        profile.alwaysOn = false;

        setField(handler, "api", api);
        setField(handler, "logger", LoggerFactory.getLogger(ShellyBaseHandler.class));
        setField(handler, "vibrationFilter", new AtomicInteger());
        setField(handler, "initLock", new Object());
        handler.profile = profile;
        handler.scheduledUpdates = 1;
        doReturn(thing).when(handler).getThing();
        when(thing.getStatus()).thenReturn(ThingStatus.ONLINE);
        doReturn(ThingStatusDetail.NONE).when(handler).getThingStatusDetail();
        when(api.getStatus()).thenAnswer(invocation -> {
            setField(handler, "stopping", true);
            throw new ShellyApiException("simulated failure during shutdown");
        });

        handler.refreshStatus();

        verify(handler, never()).setThingOfflineAndDisconnect(any(), any(), any());
    }

    @Test
    void initializerWaitingForTheInitLockDuringDisposeDoesNotInitialize() throws Exception {
        ShellyBaseHandler handler = mock(ShellyBaseHandler.class, CALLS_REAL_METHODS);
        ShellyApiInterface api = mock(ShellyApiInterface.class);
        Object initLock = new Object();
        setField(handler, "api", api);
        setField(handler, "logger", LoggerFactory.getLogger(ShellyBaseHandler.class));
        setField(handler, "initLock", initLock);

        CompletableFuture<Boolean> initialized;
        Thread initializer;
        synchronized (initLock) {
            CompletableFuture<Thread> started = new CompletableFuture<>();
            initialized = CompletableFuture.supplyAsync(() -> {
                started.complete(Thread.currentThread());
                try {
                    return handler.initializeThing();
                } catch (ShellyApiException e) {
                    throw new AssertionError(e);
                }
            });
            initializer = started.get(5, TimeUnit.SECONDS);
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (initializer.getState() != Thread.State.BLOCKED && System.nanoTime() < deadline) {
                Thread.onSpinWait();
            }
            assertEquals(Thread.State.BLOCKED, initializer.getState());
            setField(handler, "stopping", true);
        }

        assertFalse(initialized.get(5, TimeUnit.SECONDS));
        verify(api, never()).initialize();
    }

    @Test
    void stopRemovesTheCoiotListener() throws Exception {
        ShellyBaseHandler handler = mock(ShellyBaseHandler.class, CALLS_REAL_METHODS);
        Shelly1CoapHandler coap = mock(Shelly1CoapHandler.class);
        setField(handler, "api", mock(ShellyApiInterface.class));
        setField(handler, "logger", LoggerFactory.getLogger(ShellyBaseHandler.class));
        setField(handler, "coap", coap);
        handler.profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUS1PM);

        handler.stop();

        verify(coap).stop();
    }

    @Test
    void initializeAfterDisposeOfTheSameInstanceIsNotStopping() throws Exception {
        ShellyBaseHandler handler = mock(ShellyBaseHandler.class, CALLS_REAL_METHODS);
        setField(handler, "scheduler", mock(ScheduledExecutorService.class));
        setField(handler, "stopping", true);

        handler.initialize();

        assertFalse(handler.isStopping());
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Class<?> type = target.getClass().getSuperclass();
        while (type != null) {
            try {
                Field f = type.getDeclaredField(fieldName);
                f.setAccessible(true);
                f.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                type = type.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }
}
