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
import java.util.List;
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

    public DDWRTOpenWrtDevice(DDWRTDeviceConfiguration cfg, Logger logger) {
        super(cfg, logger);
    }

    @Override
    protected List<String> getAssoclistMacs(SshRunner runner, String iface) {
        return IwinfoParser.parseAssoclistMacs(logger, runner, iface);
    }

    @Override
    protected List<DDWRTWirelessClient> getAssociatedClients(SshRunner runner, String iface) {
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

    @Override
    protected List<DDWRTFirewallRule> enumerateFirewallRules(SshRunner runner) {
        List<DDWRTFirewallRule> rules = new ArrayList<>();

        try {
            // Modern OpenWrt (22.03+) uses nftables via fw4
            String nftOutput = runner.execStdout("nft list ruleset 2>/dev/null");
            if (!nftOutput.isEmpty()) {
                rules.addAll(parseNftRules(nftOutput));
                return rules;
            }

            // Fallback: older OpenWrt with iptables (fw3)
            String iptablesOutput = runner
                    .execStdout("iptables -L -n --line-numbers 2>/dev/null || iptables -L -n 2>/dev/null");
            if (!iptablesOutput.isEmpty()) {
                rules.addAll(parseIptablesRules(iptablesOutput));
            }
        } catch (Exception e) {
            logger.debug("Failed to enumerate OpenWrt firewall rules: {}", e.getMessage());
        }

        return rules;
    }

    /**
     * Parse {@code nft list ruleset} output into firewall rule objects.
     * nftables output is structured as nested table/chain/rule blocks.
     */
    private List<DDWRTFirewallRule> parseNftRules(String nftOutput) {
        List<DDWRTFirewallRule> rules = new ArrayList<>();
        String[] lines = nftOutput.split("\n");

        String currentChain = "";
        int ruleNumber = 1;

        for (String line : lines) {
            line = line.trim();

            // Track chain context: "chain input {"
            if (line.startsWith("chain ") && line.endsWith("{")) {
                currentChain = line.substring(6, line.length() - 1).trim();
                ruleNumber = 1;
                continue;
            }

            // Skip structural lines
            if (line.isEmpty() || "}".equals(line) || line.startsWith("table ") || line.startsWith("type ")
                    || line.startsWith("policy ") || line.startsWith("comment ") || line.startsWith("set ")
                    || line.startsWith("elements")) {
                continue;
            }

            // Remaining lines inside a chain are rules
            if (!currentChain.isEmpty() && !line.startsWith("chain ") && !line.startsWith("table ")) {
                try {
                    DDWRTFirewallRule.Direction direction = convertChainToDirection(currentChain);
                    DDWRTFirewallRule.RuleType type = DDWRTFirewallRule.RuleType.FILTER;

                    if (line.contains("accept")) {
                        type = DDWRTFirewallRule.RuleType.ACCEPT;
                    } else if (line.contains("drop")) {
                        type = DDWRTFirewallRule.RuleType.DROP;
                    } else if (line.contains("reject")) {
                        type = DDWRTFirewallRule.RuleType.REJECT;
                    } else if (line.contains("log")) {
                        type = DDWRTFirewallRule.RuleType.LOG;
                    }

                    String ruleId = "nft_" + currentChain + "_" + ruleNumber;
                    String description = "nft " + currentChain + ": " + line;

                    rules.add(new DDWRTFirewallRule(ruleId, type, direction, DDWRTFirewallRule.Protocol.ALL, null, null,
                            null, null, null, true, description, mac, 0, 0));
                    ruleNumber++;
                } catch (Exception e) {
                    logger.trace("Failed to parse nft rule '{}': {}", line, e.getMessage());
                }
            }
        }

        return rules;
    }

    /**
     * Parse iptables -L output into firewall rule objects.
     */
    private List<DDWRTFirewallRule> parseIptablesRules(String iptablesOutput) {
        List<DDWRTFirewallRule> rules = new ArrayList<>();
        String[] lines = iptablesOutput.split("\n");

        String currentChain = "";
        int ruleNumber = 1;

        for (String line : lines) {
            line = line.trim();

            // Skip empty lines and chain headers (except to capture chain name)
            if (line.isEmpty() || line.startsWith("Chain") || line.startsWith("target") || line.startsWith("num")) {
                if (line.startsWith("Chain")) {
                    // Extract chain name: "Chain INPUT (policy ACCEPT)"
                    String[] parts = line.split("\\s+");
                    if (parts.length > 1) {
                        currentChain = parts[1];
                    }
                }
                continue;
            }

            try {
                DDWRTFirewallRule rule = parseIptablesLine(line, currentChain, ruleNumber++);
                if (rule != null) {
                    rules.add(rule);
                }
            } catch (Exception e) {
                logger.trace("Failed to parse iptables line '{}': {}", line, e.getMessage());
            }
        }

        return rules;
    }

    /**
     * Parse a single iptables rule line.
     * Format: "num target prot opt source destination [options]"
     */
    private @Nullable DDWRTFirewallRule parseIptablesLine(String line, String chain, int ruleNumber) {
        String[] parts = line.split("\\s+");
        if (parts.length < 5) {
            return null;
        }

        try {
            // Parse basic rule components
            String target = parts[1];
            String protocol = parts[2];
            // parts[3] is the iptables "opt" column (typically "--"), not used
            String source = parts[4];
            String dest = parts.length > 5 ? parts[5] : "any";

            // Convert to our firewall rule format
            DDWRTFirewallRule.RuleType type = convertTargetToRuleType(target);
            DDWRTFirewallRule.Direction direction = convertChainToDirection(chain);
            DDWRTFirewallRule.Protocol proto = convertProtocol(protocol);

            // Extract ports from destination if present (e.g., "192.168.1.1:80")
            String destIp = dest;
            Integer destPort = null;
            if (dest.contains(":") && !dest.startsWith("0.0.0.0/0")) {
                String[] destParts = dest.split(":");
                destIp = destParts[0];
                try {
                    destPort = Integer.parseInt(destParts[1]);
                } catch (NumberFormatException e) {
                    // Not a port, keep as-is
                }
            }

            // Clean up "any" addresses
            if ("0.0.0.0/0".equals(source) || "any".equals(source)) {
                source = null;
            }
            if ("0.0.0.0/0".equals(destIp) || "any".equals(destIp)) {
                destIp = null;
            }

            String ruleId = "iptables_" + chain + "_" + ruleNumber;
            String description = String.format("OpenWrt iptables rule: %s chain %s", chain, target);

            return new DDWRTFirewallRule(ruleId, type, direction, proto, source, destIp, null, destPort, null, true,
                    description, mac, 0, 0);
        } catch (Exception e) {
            logger.trace("Error parsing iptables line: {}", e.getMessage());
            return null;
        }
    }

    private DDWRTFirewallRule.RuleType convertTargetToRuleType(String target) {
        switch (target.toLowerCase()) {
            case "accept":
                return DDWRTFirewallRule.RuleType.ACCEPT;
            case "drop":
                return DDWRTFirewallRule.RuleType.DROP;
            case "reject":
                return DDWRTFirewallRule.RuleType.REJECT;
            case "log":
                return DDWRTFirewallRule.RuleType.LOG;
            default:
                return DDWRTFirewallRule.RuleType.FILTER;
        }
    }

    private DDWRTFirewallRule.Direction convertChainToDirection(String chain) {
        switch (chain.toLowerCase()) {
            case "input":
                return DDWRTFirewallRule.Direction.INPUT;
            case "output":
                return DDWRTFirewallRule.Direction.OUTPUT;
            case "forward":
                return DDWRTFirewallRule.Direction.FORWARD;
            default:
                return DDWRTFirewallRule.Direction.ANY;
        }
    }

    private DDWRTFirewallRule.Protocol convertProtocol(String protocol) {
        switch (protocol.toLowerCase()) {
            case "tcp":
                return DDWRTFirewallRule.Protocol.TCP;
            case "udp":
                return DDWRTFirewallRule.Protocol.UDP;
            case "icmp":
                return DDWRTFirewallRule.Protocol.ICMP;
            case "all":
                return DDWRTFirewallRule.Protocol.ALL;
            default:
                return DDWRTFirewallRule.Protocol.ALL;
        }
    }

    @Override
    protected double refreshCpuTemp(SshRunner runner) {
        // OpenWrt: only check sysfs thermal zones (no /proc/dmu)
        String tempStr = safeTrim(runner.execStdout("cat /sys/class/thermal/thermal_zone0/temp"));
        if (tempStr.isEmpty()) {
            return 0.0;
        }
        try {
            double val = Double.parseDouble(tempStr);
            // thermal_zone reports millidegrees
            return val > 1000.0 ? val / 1000.0 : val;
        } catch (NumberFormatException e) {
            return 0.0;
        }
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
