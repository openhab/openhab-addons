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
 * OpenWrt device. Uses {@code iwinfo} and {@code uci} commands.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class DDWRTOpenWrtDevice extends DDWRTBaseDevice {

    public DDWRTOpenWrtDevice(DDWRTDeviceConfiguration cfg, Logger logger) {
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
            model = safeTrim(runner.execStdout("cat /tmp/sysinfo/model"));
        }
        if (firmware.isEmpty()) {
            firmware = safeTrim(
                    runner.execStdout("cat /etc/openwrt_release | grep DISTRIB_DESCRIPTION | cut -d\\' -f2"));
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
    protected String getLanInterface() {
        return "br-lan";
    }

    @Override
    protected String refreshWanIp(SshRunner runner) {
        // OpenWrt: no nvram; use ip command to get WAN address
        String rawWanIp = safeTrim(runner.execStdout(
                "ip -4 -o addr show dev $(uci get network.wan.device || echo eth0) | awk '{print $4}' | cut -d/ -f1"));
        return (rawWanIp.isEmpty() || "0.0.0.0".equals(rawWanIp)) ? "" : rawWanIp;
    }

    @Override
    protected String getWanInterface(SshRunner runner) {
        // OpenWrt: no nvram; use uci to get WAN device
        return safeTrim(runner.execStdout("uci get network.wan.device || echo eth0"));
    }

    @Override
    protected String getDeviceMac(SshRunner runner) {
        // OpenWrt: get LAN device MAC via ubus + jsonfilter + sysfs
        String mac = safeTrim(runner.execStdout(
                "cat \"/sys/class/net/$(ubus call network.interface.lan status | jsonfilter -e '@[\"device\"]')/address\""));
        if (!mac.isEmpty()) {
            return mac;
        }

        // Fallback to interface MAC detection (br-lan for OpenWrt)
        mac = getMacFromIpLink(runner, "br|lan|eth");
        if (!mac.isEmpty()) {
            return mac;
        }

        // Additional fallback using ifconfig
        mac = safeTrim(runner.execStdout("ifconfig br-lan | grep -oE '([0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}' | head -n1"));
        if (!mac.isEmpty()) {
            return mac;
        }

        return "";
    }
}
