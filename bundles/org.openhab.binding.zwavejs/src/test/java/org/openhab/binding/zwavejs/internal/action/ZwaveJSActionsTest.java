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
package org.openhab.binding.zwavejs.internal.action;

import static org.mockito.Mockito.*;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.zwavejs.internal.handler.ZwaveJSBridgeHandler;

/**
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class ZwaveJSActionsTest {

    @Test
    public void staleTimeoutDoesNotStopNewInvocationOfSameAction() {
        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        ScheduledFuture<?> firstStopJob = mock(ScheduledFuture.class);
        ScheduledFuture<?> secondStopJob = mock(ScheduledFuture.class);
        ArgumentCaptor<Runnable> timeoutCaptor = ArgumentCaptor.forClass(Runnable.class);
        doReturn(firstStopJob, secondStopJob).when(scheduler).schedule(timeoutCaptor.capture(), eq(30L),
                eq(TimeUnit.SECONDS));

        ZwaveJSBridgeHandler handler = mock(ZwaveJSBridgeHandler.class);
        ZwaveJSActions actions = new ZwaveJSActions(scheduler);
        actions.setThingHandler(handler);

        actions.startInclusion();
        actions.startInclusion();

        verify(handler, times(2)).startInclusion();
        verify(handler).stopInclusion();
        verify(firstStopJob).cancel(false);

        List<Runnable> timeouts = timeoutCaptor.getAllValues();
        timeouts.get(0).run();

        verify(handler).stopInclusion();
        verify(secondStopJob, never()).cancel(false);

        timeouts.get(1).run();

        verify(handler, times(2)).stopInclusion();
        verify(secondStopJob).cancel(false);
    }
}
