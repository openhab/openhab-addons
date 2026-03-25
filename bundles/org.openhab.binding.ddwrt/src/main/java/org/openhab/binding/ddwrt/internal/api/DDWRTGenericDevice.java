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
import org.openhab.binding.ddwrt.internal.DDWRTDeviceConfiguration;
import org.slf4j.Logger;

/**
 * Generic Linux device (Tomato USB, Raspberry Pi, etc.). Uses {@code iw} / {@code iwconfig} commands.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class DDWRTGenericDevice extends DDWRTBaseDevice {

    private static final Pattern STATION_MAC_PATTERN = Objects
            .requireNonNull(Pattern.compile("^Station\\s+([0-9a-fA-F:]{17})"));
    private static final Pattern SIGNAL_PATTERN = Objects.requireNonNull(Pattern.compile("signal:\\s*(-?\\d+)"));
    private static final Pattern RX_BITRATE_PATTERN = Objects
            .requireNonNull(Pattern.compile("rx bitrate:\\s*([\\d.]+\\s*\\S+)"));
    private static final Pattern TX_BITRATE_PATTERN = Objects
            .requireNonNull(Pattern.compile("tx bitrate:\\s*([\\d.]+\\s*\\S+)"));

    public DDWRTGenericDevice(DDWRTDeviceConfiguration cfg, Logger logger) {
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
            Matcher stationMatcher = STATION_MAC_PATTERN.matcher(line.trim());
            if (stationMatcher.find()) {
                macs.add(Objects.requireNonNull(stationMatcher.group(1)).toLowerCase());
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
                logger.debug("Found generic radio: {}", radio);
            }
        }
        return radios;
    }

    @Override
    protected void setRadioEnabled(SshRunner runner, String iface, boolean enabled) {
        if (enabled) {
            runner.execStdout("ip link set " + iface + " up");
        } else {
            runner.execStdout("ip link set " + iface + " down");
        }
    }

    @Override
    protected String getDeviceMac(SshRunner runner) {
        // Generic Linux: use standard interface MAC detection
        String mac = getMacFromIpLink(runner, "en|eth|wl|br");
        if (!mac.isEmpty()) {
            return mac;
        }

        // Fallback to all interfaces
        mac = getAnyMacFromIpLink(runner);
        if (!mac.isEmpty()) {
            return mac;
        }

        // Additional fallback using ifconfig
        mac = safeTrim(runner.execStdout("ifconfig | grep -oE '([0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}' | head -n1"));
        if (!mac.isEmpty()) {
            return mac;
        }

        return "";
    }
}
