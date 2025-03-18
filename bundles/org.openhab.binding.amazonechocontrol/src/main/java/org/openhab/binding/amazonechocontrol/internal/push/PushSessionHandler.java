/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal.push;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http2.api.Session;
import org.eclipse.jetty.http2.frames.GoAwayFrame;
import org.eclipse.jetty.http2.frames.PingFrame;
import org.eclipse.jetty.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PushSessionHandler} handles the HTTP/2 push session
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class PushSessionHandler extends Session.Listener.Adapter {
    private final Logger logger = LoggerFactory.getLogger(PushSessionHandler.class);
    private final Listener listener;

    public PushSessionHandler(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onClose(@NonNullByDefault({}) Session session, @NonNullByDefault({}) GoAwayFrame frame) {
        logger.debug("Session {} closed, reason {}", session.hashCode(), frame.getError());
        listener.onSessionClosed(session.hashCode());
    }

    @Override
    public void onFailure(@NonNullByDefault({}) Session session, @NonNullByDefault({}) Throwable failure) {
        logger.warn("Session {} failed: {}", session.hashCode(), failure.getMessage());
        listener.onSessionFailed(session.hashCode());
    }

    @Override
    public void onPing(@NonNullByDefault({}) Session session, @NonNullByDefault({}) PingFrame frame) {
        logger.trace("Session {} received pingFrame (reply={})", session.hashCode(), frame.isReply());
        if (!frame.isReply()) {
            // answer only if this is not a reply
            session.ping(new PingFrame(true), Callback.NOOP);
        } else {
            listener.onSessionPingReceived();
        }
    }

    public interface Listener {
        void onSessionClosed(int sessionHashCode);

        void onSessionFailed(int sessionHashCode);

        void onSessionPingReceived();
    }
}
