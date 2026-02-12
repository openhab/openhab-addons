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
import org.slf4j.LoggerFactory;

/**
 * DD-WRT device with Broadcom chipset. Uses {@code wl} commands.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class DDWRTBroadcomDevice extends DDWRTBaseDevice {

    private static final Logger logger = Objects.requireNonNull(LoggerFactory.getLogger(DDWRTBroadcomDevice.class));

    public DDWRTBroadcomDevice(DDWRTDeviceConfiguration cfg) {
        super(cfg);
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
            String ssid = safeTrim(runner.execStdout("wl -i " + iface + " ssid 2>/dev/null | awk -F'\"' '{print $2}'"));
            if (!ssid.isEmpty()) {
                DDWRTRadio radio = new DDWRTRadio(Objects.requireNonNull(mac), iface);
                radio.setSsid(ssid);

                String chStr = safeTrim(runner
                        .execStdout("wl -i " + iface + " channel 2>/dev/null | grep 'current' | awk '{print $NF}'"));
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
        model = safeTrim(runner.execStdout("grep -i 'Board:' /tmp/loginprompt 2>/dev/null | cut -d' ' -f 2-"));
        firmware = safeTrim(runner.execStdout("grep -i DD-WRT /tmp/loginprompt 2>/dev/null | cut -d' ' -f-2"));
    }

    @Override
    protected void setRadioEnabled(SshRunner runner, String iface, boolean enabled) {
        if (enabled) {
            runner.execStdout("wl -i " + iface + " radio on");
        } else {
            runner.execStdout("wl -i " + iface + " radio off");
        }
    }
}
