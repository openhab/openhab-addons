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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Data object representing a DD-WRT GUI-configured firewall filter rule from nvram.
 *
 * DD-WRT stores filter rules in nvram as {@code filter_rule1}, {@code filter_rule2}, etc.
 * Format: {@code $STAT:$START-$END:from:to:proto:port:description}
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class DDWRTFirewallRule {

    private String ruleId;
    private String nvramKey;
    private String description = "";
    private boolean enabled = false;
    private String rawValue = "";
    private String parentDeviceMac = "";

    public DDWRTFirewallRule(String ruleId, String nvramKey) {
        this.ruleId = ruleId;
        this.nvramKey = nvramKey;
    }

    // ---- Getters ----

    public String getRuleId() {
        return ruleId;
    }

    public String getNvramKey() {
        return nvramKey;
    }

    public String getDescription() {
        return description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getRawValue() {
        return rawValue;
    }

    public String getParentDeviceMac() {
        return parentDeviceMac;
    }

    // ---- Setters ----

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setRawValue(String rawValue) {
        this.rawValue = rawValue;
    }

    public void setParentDeviceMac(String parentDeviceMac) {
        this.parentDeviceMac = parentDeviceMac;
    }

    @Override
    public String toString() {
        return "DDWRTFirewallRule{id='" + ruleId + "', desc='" + description + "', enabled=" + enabled + "}";
    }
}
