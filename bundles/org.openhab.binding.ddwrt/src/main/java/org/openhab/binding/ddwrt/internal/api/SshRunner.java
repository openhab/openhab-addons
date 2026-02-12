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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.EnumSet;
import java.util.Objects;

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

    private final Logger logger = Objects.requireNonNull(LoggerFactory.getLogger(SshRunner.class));

    public SshRunner(ClientSession session, Duration defaultTimeout) {
        this.session = session;
        this.defaultTimeout = defaultTimeout;
    }

    /**
     * Execute a command and return the full result (exit code, stdout, stderr).
     * Throws {@link IOException} only for session/channel-level failures, never for non-zero exit codes.
     */
    public CommandResult execResult(String command, Duration timeout) throws IOException {
        logger.debug("{} executing command: {}", session.getRemoteAddress(), command);
        try (ChannelExec ch = session.createExecChannel(command)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            ch.setOut(out);
            ch.setErr(err);
            ch.open().verify();
            ch.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), timeout.toMillis());
            Integer rc = ch.getExitStatus();
            String stdout = out.toString(StandardCharsets.UTF_8);
            String stderr = err.toString(StandardCharsets.UTF_8);
            logger.debug("rc={} stdout={} stderr={}", rc, stdout, stderr);
            return new CommandResult(rc, stdout, stderr);
        }
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
