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
package org.openhab.binding.ocpp.internal.transport;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import eu.chargetime.ocpp.ISession;
import eu.chargetime.ocpp.model.Confirmation;
import eu.chargetime.ocpp.model.core.HeartbeatConfirmation;

/**
 * Tests the timeout decorator around a library that removes a promise only when an answer arrives and
 * never times one out on its own.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class TimingOutPromiseRepositoryTest {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @AfterEach
    void tearDown() {
        scheduler.shutdownNow();
    }

    @Test
    void anUnansweredPromiseFailsWithinTheConfiguredTimeoutAndIsRemoved() throws Exception {
        TimingOutPromiseRepository repository = new TimingOutPromiseRepository(scheduler, 1, new ConcurrentHashMap<>());

        CompletableFuture<Confirmation> promise = repository.createPromise("call-1");

        ExecutionException thrown = assertThrows(ExecutionException.class, () -> promise.get(5, TimeUnit.SECONDS));
        assertInstanceOf(TimeoutException.class, thrown.getCause());
        // The library would retain the abandoned promise forever; the decorator must not.
        assertFalse(repository.getPromise("call-1").isPresent(), "a timed-out promise must be removed");
    }

    @Test
    void anAnsweredPromiseIsUntouchedByTheTimeout() throws Exception {
        TimingOutPromiseRepository repository = new TimingOutPromiseRepository(scheduler, 1, new ConcurrentHashMap<>());

        CompletableFuture<Confirmation> promise = repository.createPromise("call-2");
        promise.complete(new HeartbeatConfirmation(java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC)));

        Thread.sleep(1500);
        assertTrue(promise.isDone());
        assertFalse(promise.isCompletedExceptionally(), "the timeout must not fire on an answered promise");
    }

    @Test
    void aTimedOutRequestIsRemovedFromItsSessionQueue() {
        // The library keeps a queued request until answered; on timeout the repository must remove it from its session.
        Map<String, ISession> requestSessions = new ConcurrentHashMap<>();
        ISession session = mock(ISession.class);
        requestSessions.put("call-3", session);
        TimingOutPromiseRepository repository = new TimingOutPromiseRepository(scheduler, 1, requestSessions);

        assertThrows(ExecutionException.class, () -> repository.createPromise("call-3").get(5, TimeUnit.SECONDS));

        verify(session, timeout(2000)).removeRequest("call-3");
        assertFalse(requestSessions.containsKey("call-3"), "the tracking entry must be cleaned up");
    }
}
