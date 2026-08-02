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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;

import eu.chargetime.ocpp.IPromiseRepository;
import eu.chargetime.ocpp.PromiseRepository;
import eu.chargetime.ocpp.model.Confirmation;

/**
 * An {@link IPromiseRepository} that bounds every outbound request with a timeout.
 *
 * <p>
 * The embedded ChargeTime library applies no timeout to outbound requests: a promise is removed
 * only when a CALLRESULT or CALLERROR arrives, and closing a session does not complete or remove
 * its outstanding promises. An unanswered request therefore stays incomplete forever, the caller
 * waits forever, and the abandoned promise is retained. This decorator completes each promise
 * exceptionally after the configured timeout and removes it from the repository, so callers always
 * get an outcome and nothing accumulates at the library boundary. A confirmation that arrives after
 * the timeout finds no promise and is dropped by the library, which is the correct late-answer
 * behaviour.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public class TimingOutPromiseRepository implements IPromiseRepository {

    private final PromiseRepository delegate = new PromiseRepository();
    private final ScheduledExecutorService scheduler;
    private final long timeoutSeconds;

    public TimingOutPromiseRepository(ScheduledExecutorService scheduler, long timeoutSeconds) {
        this.scheduler = scheduler;
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override
    @NonNullByDefault({})
    public CompletableFuture<Confirmation> createPromise(String uniqueId) {
        CompletableFuture<Confirmation> promise = delegate.createPromise(uniqueId);
        ScheduledFuture<?> reaper = scheduler.schedule(() -> {
            if (promise.completeExceptionally(
                    new TimeoutException("no response from charge point within " + timeoutSeconds + "s"))) {
                delegate.removePromise(uniqueId);
            }
        }, timeoutSeconds, TimeUnit.SECONDS);
        promise.whenComplete((confirmation, ex) -> reaper.cancel(false));
        return promise;
    }

    @Override
    @NonNullByDefault({})
    public Optional<CompletableFuture<Confirmation>> getPromise(String uniqueId) {
        return delegate.getPromise(uniqueId);
    }

    @Override
    @NonNullByDefault({})
    public void removePromise(String uniqueId) {
        delegate.removePromise(uniqueId);
    }
}
