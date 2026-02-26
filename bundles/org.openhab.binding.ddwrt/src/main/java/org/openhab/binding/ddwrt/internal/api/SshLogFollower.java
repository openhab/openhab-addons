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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.time.Year;
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
    private static final Pattern WIRELESS_MESSAGE = Pattern
            .compile("associated|disassociated|authenticated|deauthenticated|IEEE 802\\.11", Pattern.CASE_INSENSITIVE);

    // Error-level process/message indicators
    private static final Pattern ERROR_MESSAGE = Pattern.compile("panic|segfault|Oops|out of memory|kernel BUG",
            Pattern.CASE_INSENSITIVE);

    private final Logger logger = LoggerFactory.getLogger(SshLogFollower.class);
    private final Supplier<@Nullable ClientSession> sessionSupplier;
    private final SyslogParser parser = new SyslogParser();
    private final String command;
    private final @Nullable Pattern devicePattern;

    private final Object sessionLock = new Object();

    private @Nullable SyslogListener listener;
    private volatile boolean running = true;
    private volatile @Nullable ChannelExec current;

    public SshLogFollower(Supplier<@Nullable ClientSession> sessionSupplier, String command,
            @Nullable Pattern devicePattern) {
        this.sessionSupplier = Objects.requireNonNull(sessionSupplier);
        this.command = Objects.requireNonNull(command);
        this.devicePattern = devicePattern;
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
                // Request PTY so remote process receives SIGHUP when channel closes
                ch.setupSensibleDefaultPty();
                ch.open().verify();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(ch.getInvertedOut()))) {
                    for (String line; running && (line = br.readLine()) != null;) {
                        dispatchLine(line);
                    }
                }
            } catch (Exception e) {
                if (running) {
                    logger.debug("Log follower disconnected ({}), retrying in 1s: {}", command, e.getMessage());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            } finally {
                this.current = null;
            }
        }
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
                // Send Ctrl-C (ETX) via PTY to SIGINT the remote process
                OutputStream in = ch.getInvertedIn();
                if (in != null) {
                    in.write(3); // ETX = Ctrl-C
                    in.flush();
                }
            } catch (Exception ignore) {
            }
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
            SyslogEvent event = parser.parseLine(line, Year.now().getValue(), devicePattern);
            if (event == null) {
                return;
            }

            String proc = event.process.toLowerCase();
            String msg = event.message;

            // Classify by process name first, then by severity, then by message content
            if (DHCP_PROCESS.matcher(proc).find()) {
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
