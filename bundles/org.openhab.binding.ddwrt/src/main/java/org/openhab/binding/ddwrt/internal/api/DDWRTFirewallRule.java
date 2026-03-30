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

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents a firewall rule on DD-WRT/OpenWrt routers.
 * 
 * For DD-WRT: Represents nvram-based GUI filter rules.
 * Format: {@code $STAT:$START-$END:from:to:proto:port:description}
 * 
 * For OpenWrt: Represents nftables/iptables rules with full details.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class DDWRTFirewallRule {

    public enum RuleType {
        ACCEPT,
        DROP,
        REJECT,
        LOG,
        FILTER
    }

    public enum Direction {
        INPUT,
        OUTPUT,
        FORWARD,
        ANY
    }

    public enum Protocol {
        TCP,
        UDP,
        ICMP,
        ALL,
        TCP_UDP
    }

    // Common fields
    private final String ruleId;
    private String description = "";
    private boolean enabled = false;
    private final String parentDeviceMac;

    // DD-WRT nvram specific fields
    private @Nullable String nvramKey;
    private String rawValue = "";

    // Full firewall rule fields (for OpenWrt/advanced usage)
    private RuleType type = RuleType.FILTER;
    private Direction direction = Direction.ANY;
    private Protocol protocol = Protocol.ALL;
    private @Nullable String sourceIp;
    private @Nullable String destIp;
    private @Nullable Integer sourcePort;
    private @Nullable Integer destPort;
    private @Nullable String interfaceName;
    private long hitCount = 0;
    private long lastHit = 0;

    // Constructor for DD-WRT nvram rules (backward compatibility)
    public DDWRTFirewallRule(String ruleId, String nvramKey, String parentDeviceMac) {
        this.ruleId = Objects.requireNonNull(ruleId);
        this.nvramKey = nvramKey;
        this.parentDeviceMac = Objects.requireNonNull(parentDeviceMac);
    }

    // Constructor for full firewall rules
    public DDWRTFirewallRule(String ruleId, RuleType type, Direction direction, Protocol protocol,
            @Nullable String sourceIp, @Nullable String destIp, @Nullable Integer sourcePort,
            @Nullable Integer destPort, @Nullable String interfaceName, boolean enabled, String description,
            String parentDeviceMac, long hitCount, long lastHit) {
        this.ruleId = Objects.requireNonNull(ruleId);
        this.type = Objects.requireNonNull(type);
        this.direction = Objects.requireNonNull(direction);
        this.protocol = Objects.requireNonNull(protocol);
        this.sourceIp = sourceIp;
        this.destIp = destIp;
        this.sourcePort = sourcePort;
        this.destPort = destPort;
        this.interfaceName = interfaceName;
        this.enabled = enabled;
        this.description = description;
        this.parentDeviceMac = Objects.requireNonNull(parentDeviceMac);
        this.hitCount = hitCount;
        this.lastHit = lastHit;
    }

    // ---- Common getters ----

    public String getRuleId() {
        return ruleId;
    }

    public String getDescription() {
        return description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getParentDeviceMac() {
        return parentDeviceMac;
    }

    // ---- DD-WRT nvram specific getters/setters ----

    public @Nullable String getNvramKey() {
        return nvramKey;
    }

    public void setNvramKey(@Nullable String nvramKey) {
        this.nvramKey = nvramKey;
    }

    public String getRawValue() {
        return rawValue;
    }

    public void setRawValue(String rawValue) {
        this.rawValue = rawValue;
    }

    // ---- Full firewall rule getters ----

    public RuleType getType() {
        return type;
    }

    public void setType(RuleType type) {
        this.type = Objects.requireNonNull(type);
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = Objects.requireNonNull(direction);
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = Objects.requireNonNull(protocol);
    }

    public @Nullable String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(@Nullable String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public @Nullable String getDestIp() {
        return destIp;
    }

    public void setDestIp(@Nullable String destIp) {
        this.destIp = destIp;
    }

    public @Nullable Integer getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(@Nullable Integer sourcePort) {
        this.sourcePort = sourcePort;
    }

    public @Nullable Integer getDestPort() {
        return destPort;
    }

    public void setDestPort(@Nullable Integer destPort) {
        this.destPort = destPort;
    }

    public @Nullable String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(@Nullable String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public long getHitCount() {
        return hitCount;
    }

    public void setHitCount(long hitCount) {
        this.hitCount = hitCount;
    }

    public long getLastHit() {
        return lastHit;
    }

    public void setLastHit(long lastHit) {
        this.lastHit = lastHit;
    }

    // ---- Common setters ----

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    // ---- Utility methods ----

    /**
     * Check if this is a DD-WRT nvram-based rule.
     */
    public boolean isNvramRule() {
        return nvramKey != null;
    }

    /**
     * Create a simple port blocking rule.
     */
    public static DDWRTFirewallRule createPortRule(String ruleId, RuleType type, Direction direction, Protocol protocol,
            @Nullable Integer port, String description, String parentDeviceMac) {
        return new DDWRTFirewallRule(ruleId, type, direction, protocol, null, null, null, port, null, true, description,
                parentDeviceMac, 0, 0);
    }

    /**
     * Create an IP-based rule.
     */
    public static DDWRTFirewallRule createIpRule(String ruleId, RuleType type, Direction direction, Protocol protocol,
            @Nullable String ip, boolean isSource, String description, String parentDeviceMac) {
        if (isSource) {
            return new DDWRTFirewallRule(ruleId, type, direction, protocol, ip, null, null, null, null, true,
                    description, parentDeviceMac, 0, 0);
        } else {
            return new DDWRTFirewallRule(ruleId, type, direction, protocol, null, ip, null, null, null, true,
                    description, parentDeviceMac, 0, 0);
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DDWRTFirewallRule that = (DDWRTFirewallRule) obj;
        return ruleId.equals(that.ruleId) && parentDeviceMac.equals(that.parentDeviceMac);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ruleId, parentDeviceMac);
    }

    @Override
    public String toString() {
        if (isNvramRule()) {
            return "DDWRTFirewallRule{id='" + ruleId + "', desc='" + description + "', enabled=" + enabled + ", nvram='"
                    + nvramKey + "'}";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("DDWRTFirewallRule{id='").append(ruleId).append('\'');
            sb.append(", type=").append(type);
            sb.append(", direction=").append(direction);
            sb.append(", protocol=").append(protocol);
            if (sourceIp != null) {
                sb.append(", src=").append(sourceIp);
            }
            if (sourcePort != null) {
                sb.append(":").append(sourcePort);
            }
            if (destIp != null) {
                sb.append(", dst=").append(destIp);
            }
            if (destPort != null) {
                sb.append(":").append(destPort);
            }
            if (interfaceName != null) {
                sb.append(", iface=").append(interfaceName);
            }
            sb.append(", enabled=").append(enabled);
            if (hitCount > 0) {
                sb.append(", hits=").append(hitCount);
            }
            sb.append('}');
            return sb.toString();
        }
    }
}
