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

package org.openhab.binding.ddwrt.internal.api;

import java.io.IOException;
import java.time.Duration;

import org.apache.sshd.client.session.ClientSession;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an already-connected, already-authenticated SSH session plus metadata (e.g. welcome banner).
 *
 * Owns the underlying {@link ClientSession}. Call {@link #close()} to close the session.
 *
 * The same authenticated session can be used to open multiple channels concurrently (e.g. exec + log follower).
 * 
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class SshAuthSession implements AutoCloseable {
    private final Logger logger = LoggerFactory.getLogger(SshAuthSession.class);
    private final ClientSession session;
    private final Duration defaultTimeout;
    private final @Nullable String welcomeBanner;

    public SshAuthSession(ClientSession session, Duration defaultTimeout, @Nullable String welcomeBanner) {
        this.session = session;
        this.defaultTimeout = defaultTimeout;
        this.welcomeBanner = welcomeBanner;
    }

    /** Exposes the underlying SSHD session for creating additional channels (e.g. log follow). */
    public ClientSession getClientSession() {
        return session;
    }

    public Duration getDefaultTimeout() {
        return defaultTimeout;
    }

    public @Nullable String getWelcomeBanner() {
        return welcomeBanner;
    }

    /** Convenience: create a runner bound to this authenticated session (runner does not own session lifecycle). */
    public SshRunner createRunner() {
        return new SshRunner(session, defaultTimeout);
    }

    /** Convenience passthrough for one-off commands. */
    public String exec(String command) throws IOException {
        return createRunner().exec(command);
    }

    public String exec(String command, Duration timeout) throws IOException {
        return createRunner().exec(command, timeout);
    }

    @Override
    public void close() {
        // Close the session; this will terminate any open channels (runner/log follower)
        logger.debug("Closing session {}", session.getRemoteAddress());
        session.close(false);
    }
}
