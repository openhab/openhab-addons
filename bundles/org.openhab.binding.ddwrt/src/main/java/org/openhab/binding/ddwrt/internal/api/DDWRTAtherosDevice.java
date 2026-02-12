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
 * DD-WRT device with Atheros chipset. Uses {@code wl_atheros} commands.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class DDWRTAtherosDevice extends DDWRTBaseDevice {

    private static final Logger logger = Objects.requireNonNull(LoggerFactory.getLogger(DDWRTAtherosDevice.class));

    public DDWRTAtherosDevice(DDWRTDeviceConfiguration cfg) {
        super(cfg);
    }

    @Override
    protected List<DDWRTWirelessClient> getAssociatedClients(SshRunner runner, String iface) {
        String output = runner.execStdout("wl_atheros -i " + iface + " assoclist");
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
                client.setApMac(mac);
                client.setIface(iface);
                client.setOnline(true);

                // Query SNR for this client
                String rssiStr = runner.execStdout("wl_atheros -i " + iface + " rssi " + clientMac);
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
        // Try common Atheros interface names
        for (String iface : new String[] { "ath0", "ath1", "ath2" }) {
            String status = runner.execStdout("wl_atheros -i " + iface + " status 2>/dev/null");
            if (!status.isEmpty()) {
                DDWRTRadio radio = new DDWRTRadio(mac, iface);

                String ssid = safeTrim(runner.execStdout("nvram get " + iface + "_ssid 2>/dev/null"));
                if (!ssid.isEmpty()) {
                    radio.setSsid(ssid);
                }

                String chStr = safeTrim(runner.execStdout("nvram get " + iface + "_channel 2>/dev/null"));
                if (!chStr.isEmpty()) {
                    try {
                        radio.setChannel(Integer.parseInt(chStr));
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }

                String mode = safeTrim(runner.execStdout("nvram get " + iface + "_net_mode 2>/dev/null"));
                if (!mode.isEmpty()) {
                    radio.setMode(mode);
                }

                radio.setEnabled(true);
                radios.add(radio);
                logger.debug("Found Atheros radio: {}", radio);
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
            runner.execStdout("ifconfig " + iface + " up");
        } else {
            runner.execStdout("ifconfig " + iface + " down");
        }
    }
}
