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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared parser for {@code iwinfo <iface> assoclist} output used by
 * OpenWrt and Marvell DD-WRT devices.
 *
 * Sample output:
 * 
 * <pre>
 * 60:32:B1:49:21:64  -61 dBm / -89 dBm (SNR 28)  310 ms ago
 *         RX: 72.2 MBit/s, MCS 7, 20MHz                1183866 Pkts.
 *         TX: 1.0 MBit/s                                801556 Pkts.
 *         expected throughput: unknown
 * </pre>
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public final class IwinfoParser {

    private static final Logger logger = Objects.requireNonNull(LoggerFactory.getLogger(IwinfoParser.class));

    // MAC line: "60:32:B1:49:21:64 -61 dBm / -89 dBm (SNR 28) 310 ms ago"
    private static final Pattern MAC_LINE = Objects.requireNonNull(
            Pattern.compile("^([0-9A-Fa-f:]{17})\\s+(-?\\d+)\\s+dBm\\s*/\\s*(-?\\d+)\\s+dBm\\s+\\(SNR\\s+(\\d+)\\)"));

    // RX line: "RX: 72.2 MBit/s, MCS 7, 20MHz 1183866 Pkts."
    private static final Pattern RX_LINE = Objects
            .requireNonNull(Pattern.compile("^\\s*RX:\\s*(.+?)\\s+\\d+\\s+Pkts\\."));

    // TX line: "TX: 1.0 MBit/s 801556 Pkts."
    private static final Pattern TX_LINE = Objects
            .requireNonNull(Pattern.compile("^\\s*TX:\\s*(.+?)\\s+\\d+\\s+Pkts\\."));

    private IwinfoParser() {
    }

    /**
     * Parse {@code iwinfo <iface> assoclist} output into wireless client objects.
     */
    public static List<DDWRTWirelessClient> parseAssoclist(SshRunner runner, String iface, String apMac) {
        String output = runner.execStdout("iwinfo " + iface + " assoclist");
        if (output.isEmpty()) {
            return Objects.requireNonNull(Collections.emptyList());
        }

        List<DDWRTWirelessClient> clients = new ArrayList<>();
        DDWRTWirelessClient current = null;

        for (String line : output.split("\n")) {
            Matcher macMatcher = MAC_LINE.matcher(line.trim());
            if (macMatcher.find()) {
                if (current != null) {
                    clients.add(current);
                }
                current = new DDWRTWirelessClient(Objects.requireNonNull(macMatcher.group(1)));
                current.setApMac(apMac);
                current.setIface(iface);
                current.setOnline(true);
                try {
                    current.setSignalDbm(Integer.parseInt(Objects.requireNonNull(macMatcher.group(2))));
                    current.setNoiseDbm(Integer.parseInt(Objects.requireNonNull(macMatcher.group(3))));
                    current.setSnr(Integer.parseInt(Objects.requireNonNull(macMatcher.group(4))));
                } catch (NumberFormatException e) {
                    logger.debug("Failed to parse signal values: {}", line);
                }
                continue;
            }

            if (current != null) {
                Matcher rxMatcher = RX_LINE.matcher(line);
                if (rxMatcher.find()) {
                    current.setRxRate(Objects.requireNonNull(rxMatcher.group(1)).trim());
                    continue;
                }
                Matcher txMatcher = TX_LINE.matcher(line);
                if (txMatcher.find()) {
                    current.setTxRate(Objects.requireNonNull(txMatcher.group(1)).trim());
                }
            }
        }
        if (current != null) {
            clients.add(current);
        }

        logger.debug("Parsed {} clients from iwinfo {} assoclist", clients.size(), iface);
        return clients;
    }

    /**
     * Enumerate radios using {@code iwinfo} (lists all wireless interfaces).
     */
    public static List<DDWRTRadio> enumerateRadios(SshRunner runner, String deviceMac) {
        // iwinfo without args lists interfaces with their ESSID and channel
        String output = runner.execStdout("iwinfo");
        if (output.isEmpty()) {
            return Objects.requireNonNull(Collections.emptyList());
        }

        List<DDWRTRadio> radios = new ArrayList<>();
        String currentIface = "";

        for (String line : output.split("\n")) {
            String trimmed = line.trim();
            // Interface lines look like: "phy0-ap0 ESSID: "MyNetwork""
            if (!trimmed.startsWith("  ") && !trimmed.isEmpty() && !line.startsWith("\t")) {
                String[] parts = trimmed.split("\\s+", 2);
                if (parts.length >= 1) {
                    currentIface = Objects.requireNonNull(parts[0]);
                    DDWRTRadio radio = new DDWRTRadio(deviceMac, currentIface);

                    // Extract ESSID if present on same line
                    if (trimmed.contains("ESSID:")) {
                        String essid = trimmed.replaceAll(".*ESSID:\\s*\"([^\"]*)\".*", "$1");
                        if (!essid.equals(trimmed)) {
                            radio.setSsid(essid);
                        }
                    }

                    radio.setEnabled(true);

                    // Get channel info
                    String chStr = runner.execStdout(
                            "iwinfo " + currentIface + " info | grep -i channel | head -1 | grep -oE '[0-9]+'");
                    if (!chStr.isEmpty()) {
                        try {
                            radio.setChannel(Integer.parseInt(chStr.trim().split("\n")[0]));
                        } catch (NumberFormatException e) {
                            // ignore
                        }
                    }

                    radios.add(radio);
                    logger.debug("Found iwinfo radio: {}", radio);
                }
            }
        }
        return radios;
    }
}
