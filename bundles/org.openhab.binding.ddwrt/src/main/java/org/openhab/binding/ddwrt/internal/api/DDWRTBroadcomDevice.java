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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ddwrt.internal.DDWRTDeviceConfiguration;
import org.slf4j.Logger;

/**
 * DD-WRT device with Broadcom chipset. Uses {@code wl} commands.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class DDWRTBroadcomDevice extends DDWRTBaseDevice {

    public DDWRTBroadcomDevice(DDWRTDeviceConfiguration cfg, Logger logger) {
        super(cfg, logger);
    }

    @Override
    protected List<String> getAssoclistMacs(SshRunner runner, String iface) {
        String output = runner.execStdout("wl -i " + iface + " assoclist");
        if (output.isEmpty()) {
            return Objects.requireNonNull(Collections.emptyList());
        }

        List<String> macs = new ArrayList<>();
        for (String line : output.split("\n")) {
            String trimmed = line.trim();
            // Format: "assoclist XX:XX:XX:XX:XX:XX"
            if (trimmed.startsWith("assoclist ") && trimmed.length() >= 27) {
                String mac = Objects.requireNonNull(trimmed.substring(10).trim().toLowerCase());
                macs.add(mac);
            }
        }
        logger.debug("Broadcom {}: assoclist returned {} MACs", iface, macs.size());
        return macs;
    }

    @Override
    protected List<DDWRTWirelessClient> getAssociatedClients(SshRunner runner, String iface) {
        String output = runner.execStdout("wl -i " + iface + " assoclist");
        if (output.isEmpty()) {
            return Objects.requireNonNull(Collections.emptyList());
        }

        List<DDWRTWirelessClient> clients = new ArrayList<>();
        for (String line : output.split("\n")) {
            String trimmed = line.trim();
            // Format: "assoclist XX:XX:XX:XX:XX:XX"
            if (trimmed.startsWith("assoclist ") && trimmed.length() >= 27) {
                String clientMac = Objects.requireNonNull(trimmed.substring(10).trim().toLowerCase());
                DDWRTWirelessClient client = new DDWRTWirelessClient(clientMac);
                client.setApMac(Objects.requireNonNull(mac));
                client.setIface(iface);
                client.setOnline(true);

                // Query RSSI for this client
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
        // Try common Broadcom interface names
        for (String iface : new String[] { "eth1", "eth2", "wl0", "wl1" }) {
            String ssid = safeTrim(runner.execStdout("wl -i " + iface + " ssid | awk -F'\"' '{print $2}'"));
            if (!ssid.isEmpty()) {
                DDWRTRadio radio = new DDWRTRadio(Objects.requireNonNull(mac), iface);
                radio.setSsid(ssid);

                String chStr = safeTrim(
                        runner.execStdout("wl -i " + iface + " channel | grep 'current' | awk '{print $NF}'"));
                if (!chStr.isEmpty()) {
                    try {
                        radio.setChannel(Integer.parseInt(chStr));
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }

                radio.setEnabled(true);
                radios.add(radio);
                logger.debug("Found Broadcom radio: {}", radio);
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
        if (cpuModel.isEmpty()) {
            // First try to get chipset from system type line (FreshTomato style)
            String systemType = safeTrim(runner.execStdout("grep 'system type' /proc/cpuinfo | cut -d':' -f2 | xargs"));
            if (!systemType.isEmpty() && systemType.contains("BCM")) {
                // Extract "Broadcom BCM5357 chip rev 2 pkg 10" -> "Broadcom BCM5357"
                cpuModel = systemType.replaceAll("(Broadcom BCM\\d+).*", "$1");
            } else {
                // Fallback to Hardware line for DD-WRT style
                cpuModel = safeTrim(runner.execStdout("grep 'Hardware.*:' /proc/cpuinfo | cut -d':' -f2 | xargs"));

                // If we got generic "Northstar" hardware, try to get specific chipset
                if (cpuModel.contains("Northstar")) {
                    cpuModel = getBroadcomChipsetModel(runner);
                }
            }
        }
    }

    /**
     * Map DD-WRT board type and core revision to specific Broadcom chipset model
     */
    private String getBroadcomChipsetModel(SshRunner runner) {
        String boardType = safeTrim(runner.execStdout("nvram get boardtype"));
        String coreRev = safeTrim(runner.execStdout("nvram get wl0_corerev"));

        // Map known board types to chipset models
        if ("0x0665".equals(boardType) && "42".equals(coreRev)) {
            return "Broadcom BCM4709";
        } else if ("0x0665".equals(boardType)) {
            return "Broadcom BCM4708/4709";
        } else if ("0x0646".equals(boardType)) {
            return "Broadcom BCM4706";
        } else if ("0x052b".equals(boardType)) {
            return "Broadcom BCM5357";
        } else if ("0x04cf".equals(boardType)) {
            return "Broadcom BCM4716";
        } else if ("0x0411".equals(boardType)) {
            return "Broadcom BCM4718";
        }

        // Fallback to generic Northstar if we can't map it
        return "Broadcom Northstar";
    }

    @Override
    protected double refreshCpuTemp(SshRunner runner) {
        // Get CPU temperature using parent method
        double cpuTemp = super.refreshCpuTemp(runner);

        // Get wireless temperatures using wl commands
        String wl0TempStr = safeTrim(runner.execStdout("wl -i wl0 phy_tempsense | cut -d' ' -f3"));
        if (!wl0TempStr.isEmpty()) {
            try {
                wl0Temp = Double.parseDouble(wl0TempStr);
            } catch (NumberFormatException e) {
                // Keep default 0.0
            }
        }

        String wl1TempStr = safeTrim(runner.execStdout("wl -i wl1 phy_tempsense | cut -d' ' -f3"));
        if (!wl1TempStr.isEmpty()) {
            try {
                wl1Temp = Double.parseDouble(wl1TempStr);
            } catch (NumberFormatException e) {
                // Keep default 0.0
            }
        }

        return cpuTemp;
    }

    @Override
    protected void setRadioEnabled(SshRunner runner, String iface, boolean enabled) {
        if (enabled) {
            runner.execStdout("wl -i " + iface + " radio on");
        } else {
            runner.execStdout("wl -i " + iface + " radio off");
        }
    }

    @Override
    protected String getDeviceMac(SshRunner runner) {
        // Try DD-WRT nvram first (most reliable for Broadcom DD-WRT)
        String mac = safeTrim(runner.execStdout("nvram get lan_hwaddr"));
        if (!mac.isEmpty()) {
            return mac;
        }

        // Fallback to interface MAC detection
        mac = getMacFromIpLink(runner, "en|eth|wl|br");
        if (!mac.isEmpty()) {
            return mac;
        }

        // Additional fallback for Broadcom-specific interfaces
        mac = safeTrim(runner.execStdout("ifconfig eth1 | grep -oE '([0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}' | head -n1"));
        if (!mac.isEmpty()) {
            return mac;
        }

        return "";
    }
}
