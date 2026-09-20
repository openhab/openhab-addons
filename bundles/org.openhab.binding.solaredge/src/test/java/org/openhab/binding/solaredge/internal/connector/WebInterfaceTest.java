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
package org.openhab.binding.solaredge.internal.connector;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.solaredge.internal.command.SolarEdgeCommand;
import org.openhab.binding.solaredge.internal.config.SolarEdgeConfiguration;
import org.openhab.binding.solaredge.internal.handler.SolarEdgeHandler;

/**
 * Tests that connector restarts drop queued commands without blocking new ones.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class WebInterfaceTest {

    @Test
    public void dropsQueuedCommandsOnRestartAndAcceptsNewCommands() {
        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        SolarEdgeHandler handler = mock(SolarEdgeHandler.class);
        HttpClient httpClient = mock(HttpClient.class);
        Request request = mock(Request.class);
        Response response = mock(Response.class);
        SolarEdgeConfiguration config = new SolarEdgeConfiguration();
        config.setTokenOrApiKey("api-key");
        when(handler.getConfiguration()).thenReturn(config);
        when(scheduler.scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), eq(TimeUnit.MILLISECONDS)))
                .thenReturn(mock(ScheduledFuture.class));
        when(httpClient.newRequest(anyString())).thenReturn(request);
        when(request.timeout(anyLong(), eq(TimeUnit.SECONDS))).thenReturn(request);
        when(request.param(anyString(), anyString())).thenReturn(request);
        when(response.getStatus()).thenReturn(200);
        doAnswer(invocation -> {
            Response.CompleteListener listener = invocation.getArgument(0);
            if (listener instanceof org.openhab.binding.solaredge.internal.command.AbstractCommand command) {
                command.onSuccess(response);
                command.onComplete(mock(Result.class));
            }
            return null;
        }).when(request).send(any(Response.CompleteListener.class));

        WebInterface connector = new WebInterface(scheduler, handler, httpClient);
        SolarEdgeCommand stale = mock(SolarEdgeCommand.class);
        connector.enqueueCommand(stale);
        connector.start();

        ArgumentCaptor<Runnable> jobs = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).scheduleWithFixedDelay(jobs.capture(), anyLong(), anyLong(), eq(TimeUnit.MILLISECONDS));
        Runnable job = jobs.getValue();
        job.run();
        verify(stale, never()).performAction(httpClient);

        SolarEdgeCommand staleOnRestart = mock(SolarEdgeCommand.class);
        connector.enqueueCommand(staleOnRestart);
        connector.start();
        verify(scheduler, times(2)).scheduleWithFixedDelay(jobs.capture(), anyLong(), anyLong(),
                eq(TimeUnit.MILLISECONDS));
        job = jobs.getValue();
        job.run();
        verify(staleOnRestart, never()).performAction(httpClient);

        SolarEdgeCommand current = mock(SolarEdgeCommand.class);
        connector.enqueueCommand(current);
        job.run();
        verify(current).performAction(httpClient);

        SolarEdgeCommand staleOnDispose = mock(SolarEdgeCommand.class);
        connector.enqueueCommand(staleOnDispose);
        connector.dispose();
        connector.start();
        verify(scheduler, times(3)).scheduleWithFixedDelay(jobs.capture(), anyLong(), anyLong(),
                eq(TimeUnit.MILLISECONDS));
        jobs.getValue().run();
        verify(staleOnDispose, never()).performAction(httpClient);
    }
}
