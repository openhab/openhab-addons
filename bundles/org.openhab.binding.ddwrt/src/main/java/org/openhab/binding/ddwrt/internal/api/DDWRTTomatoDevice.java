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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ddwrt.internal.DDWRTDeviceConfiguration;
import org.slf4j.Logger;

/**
 * Tomato USB device. Uses {@code wl} commands and different nvram structure.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class DDWRTTomatoDevice extends DDWRTBaseDevice {

    private static final List<String> DEFAULT_RADIO_IFACES = List.of("eth1", "eth2", "eth3", "wl0", "wl1", "wl2");
    private static final String GET_CONFIGURED_IFACES_COMMAND = "nvram get wl_ifnames; nvram get wl0_ifname; "
            + "nvram get wl1_ifname; nvram get wl2_ifname";
    private static final Pattern IFACE_PATTERN = Objects.requireNonNull(Pattern.compile("[\\w.-]+"));
    private static final Pattern QUOTED_SSID_PATTERN = Objects.requireNonNull(Pattern.compile("\\\"([^\\\"]*)\\\""));

    public DDWRTTomatoDevice(DDWRTDeviceConfiguration cfg, Logger logger) {
        super(cfg, logger);
    }

    @Override
    protected List<DDWRTClient> getAssociatedClients(SshRunner runner, String iface) {
        // Tomato uses similar wl commands to Broadcom but with different output format
        String output = runner.execStdout("wl -i " + iface + " assoclist");
        if (output.isEmpty()) {
            return Objects.requireNonNull(Collections.emptyList());
        }

        List<DDWRTClient> clients = new ArrayList<>();
        for (String line : output.split("\n")) {
            String trimmed = line.trim();
            // Format: "assoclist XX:XX:XX:XX:XX:XX"
            if (trimmed.startsWith("assoclist ") && trimmed.length() >= 27) {
                String clientMac = Objects.requireNonNull(trimmed.substring(10).trim().toLowerCase(Locale.ROOT));
                DDWRTClient client = new DDWRTClient(clientMac);
                client.setApMac(Objects.requireNonNull(mac));
                client.setIface(iface);
                client.setOnline(true);

                // Query RSSI for this client (Tomato may support this)
                String rssiStr = runner.execStdout("wl -i " + iface + " rssi " + clientMac);
                if (!rssiStr.isEmpty()) {
                    try {
                        client.setSnr(Integer.parseInt(rssiStr.trim()));
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }

                clients.add(client);
            }
        }
        return clients;
    }

    @Override
    protected List<DDWRTRadio> enumerateRadios(SshRunner runner) {
        List<DDWRTRadio> radios = new ArrayList<>();
        Set<String> candidates = new LinkedHashSet<>(DEFAULT_RADIO_IFACES);
        String configuredIfaces = safeTrim(runner.execStdout(GET_CONFIGURED_IFACES_COMMAND));
        for (String iface : configuredIfaces.split("\\s+")) {
            if (IFACE_PATTERN.matcher(iface).matches()) {
                candidates.add(iface);
            }
        }

        for (String iface : candidates) {
            String ssid = parseSsid(runner.execStdout("wl -i " + iface + " ssid"));
            if (!ssid.isEmpty()) {
                DDWRTRadio radio = new DDWRTRadio(Objects.requireNonNull(mac), iface);
                radio.setSsid(ssid);

                String chStr = safeTrim(
                        runner.execStdout("wl -i " + iface + " status | awk '/Control channel:/ {print $3; exit}'"));
                if (chStr.isEmpty()) {
                    chStr = safeTrim(
                            runner.execStdout("wl -i " + iface + " channel | grep 'current' | awk '{print $NF}'"));
                }
                if (!chStr.isEmpty()) {
                    try {
                        radio.setChannel(Integer.parseInt(chStr));
                    } catch (NumberFormatException e) {
                        logger.debug("Failed to parse channel '{}' for {}", chStr, iface);
                    }
                }

                String mode = safeTrim(runner.execStdout("wl -i " + iface + " mode"));
                if (!mode.isEmpty()) {
                    radio.setMode(mode);
                }

                String radioStatus = safeTrim(runner.execStdout("wl -i " + iface + " radio"));
                try {
                    radio.setEnabled(Long.decode(radioStatus) == 0);
                } catch (NumberFormatException e) {
                    logger.debug("Failed to parse radio status '{}' for {}", radioStatus, iface);
                }
                radios.add(radio);
                logger.debug("Found Tomato radio: {}", radio);
            }
        }
        return radios;
    }

    private String parseSsid(String output) {
        String trimmed = safeTrim(output);
        Matcher matcher = QUOTED_SSID_PATTERN.matcher(trimmed);
        return matcher.find() ? Objects.requireNonNull(matcher.group(1)) : trimmed;
    }

    @Override
    protected void refreshIdentity(SshRunner runner) {
        // Tomato stores version info differently
        if (model.isEmpty()) {
            model = safeTrim(runner.execStdout("cat /proc/cpuinfo | grep 'system type' | cut -d: -f2"));
        }
        if (firmware.isEmpty()) {
            firmware = safeTrim(runner.execStdout("nvram get os_version || echo 'Tomato USB'"));
        }
    }

    @Override
    protected void setRadioEnabled(SshRunner runner, String iface, boolean enabled) throws IOException {
        if (enabled) {
            runner.exec("wl -i " + iface + " radio on");
        } else {
            runner.exec("wl -i " + iface + " radio off");
        }
    }

    @Override
    protected String getDeviceMac(SshRunner runner) {
        // Tomato uses different nvram keys
        String mac = safeTrim(runner.execStdout("nvram get lan_hwaddr"));
        if (!mac.isEmpty()) {
            return mac;
        }

        // Fallback to interface MAC detection
        mac = getMacFromIpLink(runner, "br|vlan|eth");
        if (!mac.isEmpty()) {
            return mac;
        }

        // Additional fallback for Tomato-specific interfaces
        mac = safeTrim(runner.execStdout("ifconfig br0 | grep -oE '([0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}' | head -n1"));
        if (!mac.isEmpty()) {
            return mac;
        }

        return "";
    }
}
