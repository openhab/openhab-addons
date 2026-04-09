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

package org.openhab.binding.ddwrt.internal.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.EnumSet;
import java.util.Objects;

import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
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
    private final Logger logger;
    private final ClientSession session;
    private final Duration defaultTimeout;
    private final @Nullable String welcomeBanner;
    private final String hostname;

    public SshAuthSession(ClientSession session, Duration defaultTimeout, @Nullable String welcomeBanner,
            String hostname) {
        this.session = session;
        this.defaultTimeout = defaultTimeout;
        this.welcomeBanner = welcomeBanner;
        this.hostname = hostname;
        this.logger = LoggerFactory.getLogger(SshAuthSession.class.getName() + "." + hostname);
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
        return new SshRunner(session, defaultTimeout, hostname);
    }

    /** Convenience passthrough for one-off commands. */
    public String exec(String command) throws IOException {
        return createRunner().exec(command);
    }

    public String exec(String command, Duration timeout) throws IOException {
        return createRunner().exec(command, timeout);
    }

    /**
     * Create an interactive shell channel and capture the MOTD (Message of the Day).
     * This is used to detect firmware and chipset information before running commands.
     * 
     * @return The MOTD output, or empty string if capture fails
     */
    public String captureMotd() {
        try {
            ClientChannel channel = session.createShellChannel();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();

            channel.setOut(out);
            channel.setErr(err);
            channel.open().verify(defaultTimeout.toMillis());

            // Wait a moment for MOTD to appear
            channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED, ClientChannelEvent.EOF),
                    defaultTimeout.toMillis() / 2);

            // Send exit to close the shell
            OutputStream shellIn = channel.getInvertedIn();
            if (shellIn != null) {
                shellIn.write("exit\n".getBytes(StandardCharsets.UTF_8));
                shellIn.flush();
            }

            // Wait for channel to close
            channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), defaultTimeout.toMillis());

            String motd = Objects.requireNonNull(out.toString(StandardCharsets.UTF_8).trim());
            logger.debug("Captured MOTD: {}", motd);
            return motd;

        } catch (Exception e) {
            logger.debug("Failed to capture MOTD: {}", e.getMessage());
            return "";
        }
    }

    @Override
    public void close() {
        // Close the session; this will terminate any open channels (runner/log follower)
        logger.debug("Closing session {}", session.getRemoteAddress());
        session.close(false);
    }
}
