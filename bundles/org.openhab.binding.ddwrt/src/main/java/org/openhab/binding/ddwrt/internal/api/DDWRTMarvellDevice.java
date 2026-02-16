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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ddwrt.internal.DDWRTDeviceConfiguration;
import org.slf4j.Logger;

/**
 * DD-WRT device with Marvell chipset. Uses {@code iwinfo} commands.
 * Shares the iwinfo assoclist parser with {@link DDWRTOpenWrtDevice}.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class DDWRTMarvellDevice extends DDWRTBaseDevice {

    public DDWRTMarvellDevice(DDWRTDeviceConfiguration cfg, Logger logger) {
        super(cfg, logger);
    }

    @Override
    protected List<DDWRTWirelessClient> getAssociatedClients(SshRunner runner, String iface) {
        return IwinfoParser.parseAssoclist(runner, iface, mac);
    }

    @Override
    protected List<DDWRTRadio> enumerateRadios(SshRunner runner) {
        return IwinfoParser.enumerateRadios(runner, mac);
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
