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
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;

import eu.chargetime.ocpp.Communicator;
import eu.chargetime.ocpp.IFeatureRepository;
import eu.chargetime.ocpp.ISession;
import eu.chargetime.ocpp.ISessionFactory;
import eu.chargetime.ocpp.OccurenceConstraintException;
import eu.chargetime.ocpp.SessionEvents;
import eu.chargetime.ocpp.UnsupportedFeatureException;
import eu.chargetime.ocpp.model.Confirmation;
import eu.chargetime.ocpp.model.Request;

/**
 * A session factory that records, per outbound request unique id, the session that queued it.
 * {@link TimingOutPromiseRepository} uses this mapping to remove a request from its session's queue on
 * timeout: the library removes a queued request only when a response arrives, so without this an
 * ignored request is retained for the session's lifetime.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public class TrackingSessionFactory implements ISessionFactory {

    private final ISessionFactory delegate;
    private final Map<String, ISession> requestSessions;

    public TrackingSessionFactory(ISessionFactory delegate, Map<String, ISession> requestSessions) {
        this.delegate = delegate;
        this.requestSessions = requestSessions;
    }

    @Override
    @NonNullByDefault({})
    public ISession createSession(Communicator communicator) {
        return new TrackingSession(delegate.createSession(communicator), requestSessions);
    }

    @NonNullByDefault({})
    private static final class TrackingSession implements ISession {

        private final ISession delegate;
        private final Map<String, ISession> requestSessions;

        TrackingSession(ISession delegate, Map<String, ISession> requestSessions) {
            this.delegate = delegate;
            this.requestSessions = requestSessions;
        }

        @Override
        public String storeRequest(Request request) {
            String uniqueId = delegate.storeRequest(request);
            requestSessions.put(uniqueId, delegate);
            return uniqueId;
        }

        @Override
        public IFeatureRepository getFeatureRepository() {
            return delegate.getFeatureRepository();
        }

        @Override
        public UUID getSessionId() {
            return delegate.getSessionId();
        }

        @Override
        public void open(String uri, SessionEvents eventHandler) {
            delegate.open(uri, eventHandler);
        }

        @Override
        public void accept(SessionEvents eventHandler) {
            delegate.accept(eventHandler);
        }

        @Override
        public void removeRequest(String uniqueId) {
            delegate.removeRequest(uniqueId);
        }

        @Override
        public void sendRequest(String action, Request payload, String uuid) {
            delegate.sendRequest(action, payload, uuid);
        }

        @Override
        public boolean completePendingPromise(String uniqueId, Confirmation confirmation)
                throws UnsupportedFeatureException, OccurenceConstraintException {
            return delegate.completePendingPromise(uniqueId, confirmation);
        }

        @Override
        public void close() {
            delegate.close();
        }
    }
}
