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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ddwrt.internal.DDWRTDeviceConfiguration;
import org.slf4j.Logger;

/**
 * DD-WRT device with Marvell chipset. Uses {@code iw} commands for wireless
 * and DD-WRT {@code nvram} for identity/configuration.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class DDWRTMarvellDevice extends DDWRTBaseDevice {

    private static final Pattern STATION_MAC_PATTERN = Objects
            .requireNonNull(Pattern.compile("^Station\\s+([0-9a-fA-F:]{17})"));
    private static final Pattern SIGNAL_PATTERN = Objects.requireNonNull(Pattern.compile("signal:\\s*(-?\\d+)"));
    private static final Pattern RX_BITRATE_PATTERN = Objects
            .requireNonNull(Pattern.compile("rx bitrate:\\s*([\\d.]+\\s*\\S+)"));
    private static final Pattern TX_BITRATE_PATTERN = Objects
            .requireNonNull(Pattern.compile("tx bitrate:\\s*([\\d.]+\\s*\\S+)"));

    public DDWRTMarvellDevice(DDWRTDeviceConfiguration cfg, Logger logger) {
        super(cfg, logger);
    }

    @Override
    protected List<String> getAssoclistMacs(SshRunner runner, String iface) {
        String output = runner.execStdout("iw dev " + iface + " station dump");
        if (output.isEmpty()) {
            return Objects.requireNonNull(Collections.emptyList());
        }

        List<String> macs = new ArrayList<>();
        for (String line : output.split("\n")) {
            Matcher m = STATION_MAC_PATTERN.matcher(line.trim());
            if (m.find()) {
                macs.add(Objects.requireNonNull(m.group(1)).toLowerCase(Locale.ROOT));
            }
        }
        return macs;
    }

    @Override
    protected List<DDWRTWirelessClient> getAssociatedClients(SshRunner runner, String iface) {
        String output = runner.execStdout("iw dev " + iface + " station dump");
        if (output.isEmpty()) {
            return Objects.requireNonNull(Collections.emptyList());
        }

        List<DDWRTWirelessClient> clients = new ArrayList<>();
        DDWRTWirelessClient current = null;

        for (String line : output.split("\n")) {
            Matcher stationMatcher = STATION_MAC_PATTERN.matcher(line.trim());
            if (stationMatcher.find()) {
                if (current != null) {
                    clients.add(current);
                }
                current = new DDWRTWirelessClient(Objects.requireNonNull(stationMatcher.group(1)));
                current.setApMac(Objects.requireNonNull(mac));
                current.setIface(iface);
                current.setOnline(true);
                continue;
            }

            if (current != null) {
                String trimmed = line.trim();
                Matcher signalMatcher = SIGNAL_PATTERN.matcher(trimmed);
                if (signalMatcher.find()) {
                    try {
                        current.setSignalDbm(Integer.parseInt(Objects.requireNonNull(signalMatcher.group(1))));
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }
                Matcher rxMatcher = RX_BITRATE_PATTERN.matcher(trimmed);
                if (rxMatcher.find()) {
                    current.setRxRate(Objects.requireNonNull(rxMatcher.group(1)));
                }
                Matcher txMatcher = TX_BITRATE_PATTERN.matcher(trimmed);
                if (txMatcher.find()) {
                    current.setTxRate(Objects.requireNonNull(txMatcher.group(1)));
                }
            }
        }
        if (current != null) {
            clients.add(current);
        }

        return clients;
    }

    @Override
    protected List<DDWRTRadio> enumerateRadios(SshRunner runner) {
        String output = runner.execStdout("iw dev");
        if (output.isEmpty()) {
            return Objects.requireNonNull(Collections.emptyList());
        }

        List<DDWRTRadio> radios = new ArrayList<>();
        String currentIface = "";

        for (String line : output.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("Interface ")) {
                currentIface = Objects.requireNonNull(trimmed.substring(10).trim());
            } else if (trimmed.startsWith("ssid ") && !currentIface.isEmpty()) {
                DDWRTRadio radio = new DDWRTRadio(Objects.requireNonNull(mac), currentIface);
                radio.setSsid(Objects.requireNonNull(trimmed.substring(5).trim()));

                String chStr = safeTrim(
                        runner.execStdout("iw dev " + currentIface + " info | grep channel | awk '{print $2}'"));
                if (!chStr.isEmpty()) {
                    try {
                        radio.setChannel(Integer.parseInt(chStr));
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }

                radio.setEnabled(true);
                radios.add(radio);
                logger.debug("Found Marvell radio: {}", radio);
            }
        }
        return radios;
    }

    @Override
    protected void refreshIdentity(SshRunner runner) {
        if (model.isEmpty()) {
            model = safeTrim(runner.execStdout("grep -i 'Board:' /tmp/loginprompt | cut -d' ' -f 2-"));
        }
        if (firmware.isEmpty()) {
            firmware = safeTrim(runner.execStdout("grep -i DD-WRT /tmp/loginprompt | cut -d' ' -f-2"));
        }
    }

    @Override
    protected void setRadioEnabled(SshRunner runner, String iface, boolean enabled) {
        if (enabled) {
            runner.execStdout("ifconfig " + iface + " up");
        } else {
            runner.execStdout("ifconfig " + iface + " down");
        }
    }

    @Override
    protected String getDeviceMac(SshRunner runner) {
        // Try DD-WRT nvram first (most reliable for Marvell DD-WRT)
        String mac = safeTrim(runner.execStdout("nvram get lan_hwaddr"));
        if (!mac.isEmpty()) {
            return mac;
        }

        // Fallback to interface MAC detection
        mac = getMacFromIpLink(runner, "en|eth|wl|br");
        if (!mac.isEmpty()) {
            return mac;
        }

        // Additional fallback for Marvell-specific interfaces
        mac = safeTrim(runner.execStdout("ifconfig eth0 | grep -oE '([0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}' | head -n1"));
        if (!mac.isEmpty()) {
            return mac;
        }

        return "";
    }
}
