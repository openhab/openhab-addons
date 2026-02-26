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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses DD-WRT/OpenWrt syslog lines into structured events.
 * Uses device-specific patterns provided by DDWRTBaseDevice subclasses,
 * with kernel messages as a fallback pattern.
 * 
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class SyslogParser {

    private static final Logger logger = LoggerFactory.getLogger(SyslogParser.class);

    private static final Pattern ANSI_ESCAPE = Pattern.compile("\u001B\\[[;?0-9]*[ -/]*[@-~]");
    private static final Pattern OSC_SEQUENCE = Pattern.compile("\u001B\\].*?(\u0007|\u001B\\\\)");

    // General syslog format: timestamp hostname [optional facility.severity] process[pid]: message
    // Examples:
    // Feb 21 16:21:46 hostname daemon.info dnsmasq-dhcp[25563]: DHCPACK...
    // Feb 21 16:08:56 hostname authpriv.notice dropbear[2663]: Password auth succeeded
    // Groups: 1=timestamp, 2=hostname, 3=facility(opt), 4=severity(opt), 5=process, 6=pid, 7=message
    private static final Pattern STANDARD_SYSLOG = Pattern.compile(
            "^([A-Za-z]{3}\\s+\\d{1,2}\\s+\\d{2}:\\d{2}:\\d{2})\\s+(\\S+)\\s+(?:(\\w+)\\.(\\w+)\\s+)?([^:\\[\\s]+(?:\\.[^:\\[\\s]+)*)(?:\\[(\\d+)\\])?:\\s*(.*)$");

    // Kernel messages: timestamp kernel: message (fallback pattern)
    private static final Pattern KERNEL_SYSLOG = Pattern
            .compile("^([A-Za-z]{3}\\s+\\d{1,2}\\s+\\d{2}:\\d{2}:\\d{2})\\s+(\\S+)\\s+kernel:\\s*(.*)$");

    // DD-WRT firewall log format: timestamp kernel: IPTABLES ... SRC=... DST=... PROTO=...
    private static final Pattern FIREWALL_LOG = Pattern
            .compile(".*IPTABLES.*\\s+SRC=([0-9.]+)\\s+DST=([0-9.]+)\\s+PROTO=(\\w+)\\s+SPT=(\\d+)\\s+DPT=(\\d+).*");

    // DD-WRT wireless association/deassociation
    private static final Pattern WIRELESS_ASSOC = Pattern
            .compile(".*wireless.*(?:associated|disassociated|authenticated|deauthenticated).*");

    private final DateTimeFormatter isoDateFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    /**
     * Parsed syslog event.
     */
    public static class SyslogEvent {
        public final Instant timestamp;
        public final String hostname;
        public final String process;
        public final @Nullable Integer pid;
        public final String message;
        public final @Nullable String facility;
        public final @Nullable String severity;
        public final @Nullable FirewallInfo firewall;
        public final boolean isWirelessEvent;

        public SyslogEvent(Instant timestamp, String hostname, String process, @Nullable Integer pid, String message,
                @Nullable String facility, @Nullable String severity, @Nullable FirewallInfo firewall,
                boolean isWirelessEvent) {
            this.timestamp = timestamp;
            this.hostname = hostname;
            this.process = process;
            this.pid = pid;
            this.message = message;
            this.facility = facility;
            this.severity = severity;
            this.firewall = firewall;
            this.isWirelessEvent = isWirelessEvent;
        }
    }

    /**
     * Parsed firewall information from a log line.
     */
    public static class FirewallInfo {
        public final String sourceIp;
        public final String destIp;
        public final String protocol;
        public final int sourcePort;
        public final int destPort;

        public FirewallInfo(String sourceIp, String destIp, String protocol, int sourcePort, int destPort) {
            this.sourceIp = sourceIp;
            this.destIp = destIp;
            this.protocol = protocol;
            this.sourcePort = sourcePort;
            this.destPort = destPort;
        }
    }

    /**
     * Parse a syslog line into a structured event.
     * 
     * @param line raw syslog line
     * @param currentYear current year for timestamp parsing (syslog doesn't include year)
     * @param devicePattern device-specific pattern provided by DDWRTBaseDevice subclass
     * @return parsed event, or null if line couldn't be parsed
     */
    public @Nullable SyslogEvent parseLine(String line, int currentYear, @Nullable Pattern devicePattern) {
        String sanitized = stripControlCodes(line);
        if (sanitized.isEmpty()) {
            return null;
        }

        // Try device-specific pattern first
        if (devicePattern != null) {
            Matcher deviceMatcher = devicePattern.matcher(sanitized);
            if (deviceMatcher.matches()) {
                // 7-group patterns (e.g. OpenWrt) carry year + facility.severity
                if (deviceMatcher.groupCount() >= 7) {
                    SyslogEvent event = parseOpenWrt(deviceMatcher);
                    if (event != null) {
                        return event;
                    }
                }
                return parseFromMatcher(deviceMatcher, sanitized, currentYear, false);
            }
        }

        // Try general syslog format (with optional facility.severity)
        Matcher stdMatcher = STANDARD_SYSLOG.matcher(sanitized);
        if (stdMatcher.matches()) {
            return parseStandard(stdMatcher, currentYear);
        }

        // Try kernel format as fallback
        Matcher kernelMatcher = KERNEL_SYSLOG.matcher(sanitized);
        if (kernelMatcher.matches()) {
            String timestampStr = kernelMatcher.group(1);
            String hostname = kernelMatcher.group(2);
            String message = kernelMatcher.group(3);

            Instant timestamp = parseTimestamp(timestampStr, currentYear, false);
            if (timestamp != null) {
                @Nullable
                FirewallInfo firewall = parseFirewallInfo(message);
                boolean isWireless = WIRELESS_ASSOC.matcher(message).find();
                return new SyslogEvent(timestamp, hostname, "kernel", null, message, null, null, firewall, isWireless);
            }
        }

        // If no pattern matches, treat as unparseable
        logger.trace("Unparseable syslog line: {}", sanitized);
        return null;
    }

    private static String stripControlCodes(String value) {
        if (value.isEmpty()) {
            return value;
        }
        String withoutOsc = OSC_SEQUENCE.matcher(value).replaceAll("");
        return ANSI_ESCAPE.matcher(withoutOsc).replaceAll("").trim();
    }

    private @Nullable SyslogEvent parseFromMatcher(Matcher matcher, String line, int currentYear, boolean isIso) {
        String timestampStr = matcher.group(1);
        String hostname = matcher.group(2);
        String process = matcher.group(3);
        String pidStr = matcher.groupCount() > 3 ? matcher.group(4) : null;
        String message = matcher.group(matcher.groupCount());

        Instant timestamp = parseTimestamp(timestampStr, currentYear, isIso);
        if (timestamp == null) {
            return null;
        }

        Integer pid = null;
        if (pidStr != null && !pidStr.isEmpty()) {
            try {
                pid = Integer.parseInt(pidStr);
            } catch (NumberFormatException e) {
                // Ignore invalid PID
            }
        }

        @Nullable
        FirewallInfo firewall = parseFirewallInfo(message);
        boolean isWireless = WIRELESS_ASSOC.matcher(message).find();

        return new SyslogEvent(timestamp, hostname, process, pid, message, null, null, firewall, isWireless);
    }

    private @Nullable SyslogEvent parseOpenWrt(Matcher m) {
        String timestampStr = m.group(1);
        int year;
        try {
            year = Integer.parseInt(Objects.requireNonNull(m.group(2)));
        } catch (NumberFormatException e) {
            return null;
        }
        String facility = m.group(3);
        String severity = m.group(4);
        String process = Objects.requireNonNull(m.group(5));
        String pidStr = m.group(6);
        String message = Objects.requireNonNull(m.group(7));

        Instant timestamp = parseTimestamp(timestampStr, year, false);
        if (timestamp == null) {
            return null;
        }

        Integer pid = null;
        if (pidStr != null && !pidStr.isEmpty()) {
            try {
                pid = Integer.parseInt(pidStr);
            } catch (NumberFormatException e) {
                // Ignore invalid PID
            }
        }

        @Nullable
        FirewallInfo firewall = parseFirewallInfo(message);
        boolean isWireless = WIRELESS_ASSOC.matcher(message).find();

        return new SyslogEvent(timestamp, "", process, pid, message, facility, severity, firewall, isWireless);
    }

    private @Nullable SyslogEvent parseStandard(Matcher m, int currentYear) {
        String timestampStr = m.group(1);
        String hostname = Objects.requireNonNull(m.group(2));
        String facility = m.group(3);
        String severity = m.group(4);
        String process = Objects.requireNonNull(m.group(5));
        String pidStr = m.group(6);
        String message = Objects.requireNonNull(m.group(7));

        Instant timestamp = parseTimestamp(timestampStr, currentYear, false);
        if (timestamp == null) {
            return null;
        }

        Integer pid = null;
        if (pidStr != null && !pidStr.isEmpty()) {
            try {
                pid = Integer.parseInt(pidStr);
            } catch (NumberFormatException e) {
                // Ignore invalid PID
            }
        }

        @Nullable
        FirewallInfo firewall = parseFirewallInfo(message);
        boolean isWireless = WIRELESS_ASSOC.matcher(message).find();

        return new SyslogEvent(timestamp, hostname, process, pid, message, facility, severity, firewall, isWireless);
    }

    private @Nullable Instant parseTimestamp(String timestampStr, int currentYear, boolean isIso) {
        try {
            if (isIso) {
                LocalDateTime dt = LocalDateTime.parse(timestampStr, isoDateFormatter);
                return dt.atZone(ZoneId.systemDefault()).toInstant();
            } else {
                // Standard format doesn't include year, so we add it
                LocalDateTime dt = LocalDateTime.parse(currentYear + " " + timestampStr,
                        DateTimeFormatter.ofPattern("yyyy MMM d HH:mm:ss"));
                return dt.atZone(ZoneId.systemDefault()).toInstant();
            }
        } catch (Exception e) {
            logger.trace("Failed to parse timestamp '{}': {}", timestampStr, e.getMessage());
            return null;
        }
    }

    private @Nullable FirewallInfo parseFirewallInfo(String message) {
        Matcher firewallMatcher = FIREWALL_LOG.matcher(message);
        if (firewallMatcher.find()) {
            try {
                String sourceIp = Objects.requireNonNull(firewallMatcher.group(1));
                String destIp = Objects.requireNonNull(firewallMatcher.group(2));
                String protocol = Objects.requireNonNull(firewallMatcher.group(3));
                int sourcePort = Integer.parseInt(Objects.requireNonNull(firewallMatcher.group(4)));
                int destPort = Integer.parseInt(Objects.requireNonNull(firewallMatcher.group(5)));
                return new FirewallInfo(sourceIp, destIp, protocol, sourcePort, destPort);
            } catch (NumberFormatException e) {
                logger.trace("Failed to parse firewall ports in: {}", message);
            }
        }
        return null;
    }
}
