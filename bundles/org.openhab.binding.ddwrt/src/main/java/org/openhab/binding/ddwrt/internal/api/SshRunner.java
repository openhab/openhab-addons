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
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes commands on an existing authenticated {@link ClientSession}.
 *
 * Important: This class does NOT own the session lifecycle. It opens and closes only per-command channels.
 * Session lifecycle is owned by {@link SshAuthSession}.
 * 
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class SshRunner {

    /** Result of a remote command execution. */
    public static class CommandResult {
        private final @Nullable Integer exitCode;
        private final String stdout;
        private final String stderr;

        public CommandResult(@Nullable Integer exitCode, String stdout, String stderr) {
            this.exitCode = exitCode;
            this.stdout = stdout;
            this.stderr = stderr;
        }

        public @Nullable Integer getExitCode() {
            return exitCode;
        }

        public String getStdout() {
            return stdout;
        }

        public String getStderr() {
            return stderr;
        }

        public boolean isSuccess() {
            return exitCode != null && exitCode == 0;
        }
    }

    private final ClientSession session;
    private final Duration defaultTimeout;

    private final Logger logger;

    public SshRunner(ClientSession session, Duration defaultTimeout, String hostname) {
        this.session = session;
        this.defaultTimeout = defaultTimeout;
        this.logger = LoggerFactory.getLogger(SshRunner.class.getName() + "." + hostname);
    }

    /**
     * Execute a command and return the full result (exit code, stdout, stderr).
     * Throws {@link IOException} only for session/channel-level failures, never for non-zero exit codes.
     */
    public CommandResult execResult(String command, Duration timeout) throws IOException {
        int attempts = 0;
        final int maxAttempts = 3;

        while (attempts < maxAttempts) {
            try (ChannelExec ch = session.createExecChannel(command)) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ByteArrayOutputStream err = new ByteArrayOutputStream();
                ch.setOut(out);
                ch.setErr(err);
                ch.open().verify();
                Set<ClientChannelEvent> events = ch.waitFor(
                        EnumSet.of(ClientChannelEvent.CLOSED, ClientChannelEvent.EXIT_STATUS), timeout.toMillis());
                Integer rc = ch.getExitStatus();
                String stdout = out.toString(StandardCharsets.UTF_8);
                String stderr = err.toString(StandardCharsets.UTF_8);

                if (events.contains(ClientChannelEvent.TIMEOUT)) {
                    logger.debug("{} TIMEOUT rc={} stdout={} stderr={}", command, rc, stdout, stderr);
                    return new CommandResult(rc, stdout, stderr);
                } else if (rc == null) {
                    attempts++;
                    if (attempts < maxAttempts) {
                        logger.warn("{} rc=null (attempt {}/{}) - retrying in 100ms. stdout='{}' stderr='{}'", command,
                                attempts, maxAttempts, stdout, stderr);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                        continue;
                    } else {
                        logger.error(
                                "{} rc=null after {} attempts - giving up. stdout='{}' stderr='{}' - SSH session may be corrupted or concurrent channels are interfering",
                                command, maxAttempts, stdout, stderr);
                    }
                } else {
                    logger.debug("{} rc={} stdout={} stderr={}", command, rc, stdout, stderr);
                }
                return new CommandResult(rc, stdout, stderr);
            }
        }

        // If we get here, all attempts failed
        return new CommandResult(null, "", "Failed after " + maxAttempts + " attempts");
    }

    public CommandResult execResult(String command) throws IOException {
        return execResult(command, defaultTimeout);
    }

    /**
     * Execute a command, returning stdout on success or throwing on failure.
     * This is a convenience wrapper around {@link #execResult(String, Duration)}.
     */
    public String exec(String command, Duration timeout) throws IOException {
        CommandResult result = execResult(command, timeout);
        if (result.isSuccess()) {
            return result.getStdout();
        }
        throw new IOException("Command failed rc=" + result.getExitCode() + ", stderr=" + result.getStderr());
    }

    public String exec(String command) throws IOException {
        return exec(command, defaultTimeout);
    }

    /**
     * Execute a command, returning trimmed stdout regardless of exit code.
     * Returns "" if the command produces no output or on channel-level failure.
     */
    public String execStdout(String command, Duration timeout) {
        try {
            CommandResult result = execResult(command, timeout);
            return Objects.requireNonNull(result.getStdout().trim());
        } catch (IOException e) {
            logger.debug("Channel error (non-fatal): {}: {}", command, e.getMessage());
            return "";
        }
    }

    public String execStdout(String command) {
        return execStdout(command, defaultTimeout);
    }
}
