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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Year;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.session.ClientSession;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ddwrt.internal.api.SyslogParser.SyslogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Follows remote syslog via SSH and dispatches classified events to a {@link SyslogListener}.
 * Uses BufferedReader.readLine() for clean line-at-a-time reads with auto-reconnect on failure.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class SshLogFollower implements Runnable, AutoCloseable {

    // DHCP process names
    private static final Pattern DHCP_PROCESS = Pattern.compile("dnsmasq-dhcp|dnsmasq\\b|dhcpd|dhclient|udhcpc|odhcpd",
            Pattern.CASE_INSENSITIVE);

    // Wireless process names
    private static final Pattern WIRELESS_PROCESS = Pattern.compile("hostapd|wpa_supplicant|nas|wl\\b|iwinfo",
            Pattern.CASE_INSENSITIVE);

    // Wireless event keywords in message body (for kernel or generic processes)
    private static final Pattern WIRELESS_MESSAGE = Pattern.compile(
            "associated|disassociated|authenticated|deauthenticated|IEEE 802\\.11|\\bSTA\\b.*\\bMLME\\b",
            Pattern.CASE_INSENSITIVE);

    // DHCP event keywords in message body (to distinguish from dnsmasq warnings/errors)
    private static final Pattern DHCP_MESSAGE = Pattern.compile(
            "DHCPACK|DHCPREQUEST|DHCPDISCOVER|DHCPOFFER|DHCPDECLINE|DHCPNAK|DHCPINFORM|DHCPRELEASE|lease|renew|rebind",
            Pattern.CASE_INSENSITIVE);

    // Error-level process/message indicators
    private static final Pattern ERROR_MESSAGE = Pattern.compile("panic|segfault|Oops|out of memory|kernel BUG",
            Pattern.CASE_INSENSITIVE);

    private Logger logger;
    private final Supplier<@Nullable ClientSession> sessionSupplier;
    private final SyslogParser parser;
    private final String command;
    private final @Nullable Pattern devicePattern;
    private final boolean isLogreadCommand;

    private final Object sessionLock = new Object();

    private @Nullable SyslogListener listener;
    private volatile boolean running = true;
    private volatile @Nullable ChannelExec current;
    private long backoffMs = 1000;

    public SshLogFollower(Supplier<@Nullable ClientSession> sessionSupplier, String command,
            @Nullable Pattern devicePattern, String hostname) {
        this.sessionSupplier = Objects.requireNonNull(sessionSupplier);
        this.command = Objects.requireNonNull(command);
        this.devicePattern = devicePattern;
        this.logger = LoggerFactory.getLogger(SshLogFollower.class.getName() + "." + hostname);
        this.parser = new SyslogParser(this.logger);
        this.isLogreadCommand = command.contains("logread") || command.contains("tail -F");
    }

    /**
     * Register a listener for classified syslog events.
     */
    public void setListener(@Nullable SyslogListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        while (running) {
            ClientSession session = sessionSupplier.get();
            if (session == null || !session.isOpen()) {
                synchronized (sessionLock) {
                    try {
                        sessionLock.wait();
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
                continue;
            }

            try (ChannelExec ch = session.createExecChannel(command)) {
                this.current = ch;

                // For logread commands, use PTY to ensure proper cleanup on disconnect
                if (isLogreadCommand) {
                    ch.setupSensibleDefaultPty();
                }

                ch.open().verify();
                logger.debug("Syslog follower connected: {}", command);
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(ch.getInvertedOut(), java.nio.charset.StandardCharsets.UTF_8))) {
                    for (String line; running && (line = br.readLine()) != null;) {
                        dispatchLine(line);
                        backoffMs = 1000; // reset on successful read
                    }

                    // Stream ended unexpectedly - log exit info and wait with backoff
                    if (running) {
                        Integer exitStatus = ch.getExitStatus();

                        if (exitStatus != null && exitStatus != 0) {
                            String stderrOutput = readStderr(ch);
                            logger.warn("Syslog command failed with exit code {} ({}) retrying in {}ms: {}", exitStatus,
                                    command, backoffMs, stderrOutput);
                        } else {
                            logger.warn(
                                    "Syslog stream ended unexpectedly ({}), command may have crashed or syslogd restarted, retrying in {}ms",
                                    command, backoffMs);
                        }
                    }
                }
            } catch (Exception e) {
                if (running) {
                    logger.debug("Log follower disconnected ({}), retrying in {}ms: {}", command, backoffMs,
                            e.getMessage());
                }
            } finally {
                this.current = null;
            }

            // Always apply backoff to prevent tight loops
            if (running) {
                waitForBackoff();
            }
        }
    }

    /**
     * Read stderr content from the channel.
     */
    private String readStderr(ChannelExec ch) {
        try {
            java.io.InputStream errStream = ch.getInvertedErr();
            if (errStream != null && errStream.available() > 0) {
                BufferedReader errReader = new BufferedReader(
                        new InputStreamReader(errStream, java.nio.charset.StandardCharsets.UTF_8));
                StringBuilder stderr = new StringBuilder();
                String line;
                while (errReader.ready() && (line = errReader.readLine()) != null) {
                    if (stderr.length() > 0) {
                        stderr.append("; ");
                    }
                    stderr.append(line);
                }
                return stderr.toString();
            }
        } catch (Exception e) {
            // Stderr read errors - ignore and return empty string
        }
        return "";
    }

    /**
     * Wait with exponential backoff to prevent tight loops.
     */
    private void waitForBackoff() {
        try {
            Thread.sleep(backoffMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        backoffMs = Math.min(backoffMs * 2, 30_000);
    }

    /**
     * Signal the follower that a session is available. Called by the device after session recovery.
     */
    public void wakeUp() {
        synchronized (sessionLock) {
            sessionLock.notifyAll();
        }
    }

    @Override
    public void close() {
        running = false;
        wakeUp();
        ChannelExec ch = this.current;
        if (ch != null) {
            try {
                ch.close(false);
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * Parse a raw log line and dispatch to the appropriate listener callback.
     */
    private void dispatchLine(String line) {
        SyslogListener l = listener;
        if (l == null) {
            return;
        }

        try {
            SyslogEvent event = parser.parseLine(line, Year.now(java.time.ZoneId.systemDefault()).getValue(),
                    devicePattern);
            if (event == null) {
                return;
            }

            String proc = event.process.toLowerCase(Locale.ROOT);
            String msg = event.message;

            // Classify by process name and message content
            if (DHCP_PROCESS.matcher(proc).find() && DHCP_MESSAGE.matcher(msg).find()) {
                l.onDhcpEvent(event);
            } else if (WIRELESS_PROCESS.matcher(proc).find() || WIRELESS_MESSAGE.matcher(msg).find()) {
                l.onWirelessEvent(event);
            } else if (event.firewall != null || msg.contains("IPTABLES")) {
                l.onWarningEvent(event);
            } else if (isErrorSeverity(event.severity) || ERROR_MESSAGE.matcher(msg).find()) {
                l.onErrorEvent(event);
            } else if (isWarningSeverity(event.severity)) {
                l.onWarningEvent(event);
            } else if ("kernel".equals(proc) && msg.contains("error")) {
                l.onErrorEvent(event);
            } else {
                logger.trace("Ignored syslog: {}, {}, {}, {}, {}, {}", event.hostname, event.facility, event.severity,
                        event.process, event.pid, event.message);
            }

        } catch (Exception e) {
            logger.trace("Error processing log line '{}': {}", line, e.getMessage());
        }
    }

    private static boolean isErrorSeverity(@Nullable String severity) {
        return severity != null && ("err".equals(severity) || "crit".equals(severity) || "alert".equals(severity)
                || "emerg".equals(severity));
    }

    private static boolean isWarningSeverity(@Nullable String severity) {
        return "warning".equals(severity) || "warn".equals(severity);
    }

    /**
     * Check if the follower is currently running.
     */
    public boolean isRunning() {
        return running;
    }
}
