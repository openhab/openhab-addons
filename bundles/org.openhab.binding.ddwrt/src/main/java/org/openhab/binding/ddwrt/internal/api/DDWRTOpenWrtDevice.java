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
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ddwrt.internal.DDWRTDeviceConfiguration;
import org.slf4j.Logger;

/**
 * OpenWrt device. Uses {@code iwinfo} and {@code uci} commands.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class DDWRTOpenWrtDevice extends DDWRTBaseDevice {

    private static final Pattern SAFE_COMMAND_IDENTIFIER = Objects.requireNonNull(Pattern.compile("[a-zA-Z0-9._-]+"));

    public DDWRTOpenWrtDevice(DDWRTDeviceConfiguration cfg, Logger logger) {
        super(cfg, logger);
    }

    @Override
    protected List<String> getAssoclistMacs(SshRunner runner, String iface) {
        return IwinfoParser.parseAssoclistMacs(logger, runner, iface);
    }

    @Override
    protected List<DDWRTClient> getAssociatedClients(SshRunner runner, String iface) {
        return IwinfoParser.parseAssoclist(logger, runner, iface, mac);
    }

    @Override
    protected List<DDWRTRadio> enumerateRadios(SshRunner runner) {
        return IwinfoParser.enumerateRadios(logger, runner, mac);
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
    protected void setRadioEnabled(SshRunner runner, String iface, boolean enabled) throws IOException {
        if (!SAFE_COMMAND_IDENTIFIER.matcher(iface).matches()) {
            throw new IOException("Invalid wireless interface name");
        }

        String wirelessDevice = runner.execStdout("ubus call network.wireless status | jsonfilter -e "
                + "'@.*.interfaces[@.ifname=\"" + iface + "\"].config.device[0]'");
        if (!SAFE_COMMAND_IDENTIFIER.matcher(wirelessDevice).matches()) {
            throw new IOException("Could not determine OpenWrt wireless device for " + iface);
        }

        runner.exec("/sbin/wifi " + (enabled ? "up " : "down ") + wirelessDevice);
    }

    @Override
    protected String getLanInterface(SshRunner runner) {
        return "br-lan";
    }

    @Override
    protected String getNeighborCommand() {
        // Use `ip neigh` (iproute2) for richer state information than legacy `arp`.
        // Filter out FAILED/INCOMPLETE entries at the source — only return entries
        // that have a valid MAC address and a usable NUD state.
        return "ip -4 neigh show nud reachable nud stale nud delay nud probe nud permanent nud noarp";
    }

    @Override
    protected java.util.List<DDWRTNetworkCache.ArpEntry> parseNeighborOutput(String output, String source,
            java.time.Instant seenAt) {
        java.util.List<DDWRTNetworkCache.ArpEntry> entries = new java.util.ArrayList<>();
        // `ip neigh` output format:
        // 192.168.0.74 dev br-lan lladdr d8:49:2f:d9:a4:8a REACHABLE
        // 192.168.0.99 dev br-lan lladdr aa:bb:cc:dd:ee:ff STALE
        // 192.168.0.50 dev br-lan FAILED (no lladdr — already filtered by command)
        for (String line : output.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String[] parts = trimmed.split("\\s+");
            if (parts.length < 5) {
                continue;
            }
            String ip = parts[0];
            String mac = "";
            for (int i = 1; i < parts.length - 1; i++) {
                if ("lladdr".equals(parts[i])) {
                    mac = parts[i + 1].toLowerCase(java.util.Locale.ROOT);
                    break;
                }
            }
            if (!mac.isEmpty() && isValidUnicastMac(mac)) {
                String nudState = parts[parts.length - 1].toUpperCase(java.util.Locale.ROOT);
                DDWRTNetworkCache.ArpState state = switch (nudState) {
                    case "REACHABLE" -> DDWRTNetworkCache.ArpState.ACTIVE;
                    case "STALE", "DELAY", "PROBE" -> DDWRTNetworkCache.ArpState.STALE;
                    case "PERMANENT", "NOARP" -> DDWRTNetworkCache.ArpState.ACTIVE;
                    default -> DDWRTNetworkCache.ArpState.ACTIVE;
                };
                entries.add(new DDWRTNetworkCache.ArpEntry(mac, ip, seenAt, state, source));
            }
        }
        return entries;
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

    @Override
    protected List<DDWRTFirewallRule> enumerateFirewallRules(SshRunner runner) {
        // OpenWrt's fw3/fw4 rulesets are generated runtime state rather than stable, user-configured rules. A future
        // implementation should model the UCI firewall configuration instead of exposing iptables/nftables internals.
        return List.of();
    }

    @Override
    protected void refreshDhcpPool(SshRunner runner) {
        if (!isGateway()) {
            return;
        }
        // OpenWrt: uci dhcp.lan.limit is the number of addresses in the pool
        String limit = safeTrim(runner.execStdout("uci get dhcp.lan.limit 2>/dev/null"));
        if (!limit.isEmpty()) {
            try {
                dhcpPoolSize = Integer.parseInt(limit);
                return;
            } catch (NumberFormatException e) {
                // fall through
            }
        }
        // Fallback to dnsmasq config parsing
        super.refreshDhcpPool(runner);
    }

    @Override
    protected double refreshCpuTemp(SshRunner runner) {
        // OpenWrt: only sysfs thermal zones (no /proc/dmu), skip probing
        cpuTempSource = CpuTempSource.THERMAL_ZONE;
        return readThermalZoneTemp(runner);
    }

    @Override
    protected @Nullable Pattern getSyslogPattern() {
        // OpenWrt logread format: dow month day time year facility.severity process[pid]: message
        // No hostname field. Example: Tue Feb 24 16:07:56 2026 authpriv.info dropbear[3761]: Exit (root)...
        // Groups: 1=timestamp(no year), 2=year, 3=facility, 4=severity, 5=process, 6=pid, 7=message
        return Pattern.compile(
                "^\\w{3}\\s+(\\w{3}\\s+\\d{1,2}\\s+\\d{2}:\\d{2}:\\d{2})\\s+(\\d{4})\\s+(\\w+)\\.(\\w+)\\s+([^:\\[\\s]+)(?:\\[(\\d+)\\])?:\\s*(.*)$");
    }
}
