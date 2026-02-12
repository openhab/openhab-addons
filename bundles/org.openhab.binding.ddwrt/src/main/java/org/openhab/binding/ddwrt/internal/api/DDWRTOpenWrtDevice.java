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

/**
 * OpenWrt device. Uses {@code iwinfo} and {@code uci} commands.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class DDWRTOpenWrtDevice extends DDWRTBaseDevice {

    public DDWRTOpenWrtDevice(DDWRTDeviceConfiguration cfg) {
        super(cfg);
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
        model = safeTrim(runner.execStdout("cat /tmp/sysinfo/model 2>/dev/null"));
        firmware = safeTrim(
                runner.execStdout("cat /etc/openwrt_release 2>/dev/null | grep DISTRIB_DESCRIPTION | cut -d\\' -f2"));
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
    protected void refreshCommon(SshRunner runner) {
        super.refreshCommon(runner);

        // OpenWrt may not have nvram for WAN IP; try uci
        if (wanIp.isEmpty()) {
            wanIp = safeTrim(runner.execStdout(
                    "ip -4 -o addr show dev $(uci get network.wan.device 2>/dev/null || echo eth0) 2>/dev/null | awk '{print $4}' | cut -d/ -f1"));
        }
    }
}
