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

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;

import eu.chargetime.ocpp.IPromiseRepository;
import eu.chargetime.ocpp.ISession;
import eu.chargetime.ocpp.PromiseRepository;
import eu.chargetime.ocpp.model.Confirmation;

/**
 * An {@link IPromiseRepository} that bounds every outbound request with a timeout.
 *
 * <p>
 * The embedded ChargeTime library never times out an outbound request: a promise is completed only
 * when a CALLRESULT/CALLERROR arrives, and closing a session leaves its promises incomplete. This
 * decorator completes each promise exceptionally after the timeout and removes it, so callers always
 * get an outcome. On timeout it also removes the request from its session's queue (tracked by
 * {@link TrackingSessionFactory}); the library removes that entry only on a response, so an ignored
 * request would otherwise be retained for the session's life.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public class TimingOutPromiseRepository implements IPromiseRepository {

    private final PromiseRepository delegate = new PromiseRepository();
    private final ScheduledExecutorService scheduler;
    private final long timeoutSeconds;
    private final Map<String, ISession> requestSessions;

    public TimingOutPromiseRepository(ScheduledExecutorService scheduler, long timeoutSeconds,
            Map<String, ISession> requestSessions) {
        this.scheduler = scheduler;
        this.timeoutSeconds = timeoutSeconds;
        this.requestSessions = requestSessions;
    }

    @Override
    @NonNullByDefault({})
    public CompletableFuture<Confirmation> createPromise(String uniqueId) {
        CompletableFuture<Confirmation> promise = delegate.createPromise(uniqueId);
        ScheduledFuture<?> reaper = scheduler.schedule(() -> {
            ISession session = requestSessions.get(uniqueId);
            if (promise.completeExceptionally(
                    new TimeoutException("no response from charge point within " + timeoutSeconds + "s"))) {
                delegate.removePromise(uniqueId);
                if (session != null) {
                    session.removeRequest(uniqueId);
                }
            }
        }, timeoutSeconds, TimeUnit.SECONDS);
        promise.whenComplete((confirmation, ex) -> {
            reaper.cancel(false);
            requestSessions.remove(uniqueId);
        });
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
