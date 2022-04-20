/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.unifi.internal.api.dto;

import java.time.Instant;

import org.openhab.binding.unifi.internal.api.cache.UniFiControllerCache;
import org.openhab.binding.unifi.internal.api.util.UniFiTidyLowerCaseStringDeserializer;
import org.openhab.binding.unifi.internal.api.util.UniFiTimestampDeserializer;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link UniFiClient} is the base data model for any (wired or wireless) connected to a UniFi network.
 *
 * @author Matthew Bowman - Initial contribution
 * @author Patrik Wimnell - Blocking / Unblocking client support
 */
public abstract class UniFiClient implements HasId {

    private final transient UniFiControllerCache cache;

    @SerializedName("_id")
    private String id;

    private String siteId;

    @JsonAdapter(UniFiTidyLowerCaseStringDeserializer.class)
    private String mac;

    private String ip;

    @JsonAdapter(UniFiTidyLowerCaseStringDeserializer.class)
    private String hostname;

    @SerializedName("name")
    @JsonAdapter(UniFiTidyLowerCaseStringDeserializer.class)
    private String alias;

    private Integer uptime;

    @JsonAdapter(UniFiTimestampDeserializer.class)
    private Instant lastSeen;

    private boolean blocked;

    @SerializedName("is_guest")
    private boolean guest;

    @SerializedName("fixed_ip")
    private String fixedIp;

    @SerializedName("satisfaction")
    private Integer experience;

    protected UniFiClient(final UniFiControllerCache cache) {
        this.cache = cache;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getMac() {
        return mac;
    }

    public String getIp() {
        return this.ip == null || this.ip.isBlank() ? this.fixedIp : this.ip;
    }

    public String getHostname() {
        return hostname;
    }

    public String getAlias() {
        return alias;
    }

    public Integer getUptime() {
        return uptime;
    }

    public Instant getLastSeen() {
        return lastSeen;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public abstract Boolean isWired();

    public final Boolean isWireless() {
        return isWired() == null ? null : Boolean.FALSE.equals(isWired());
    }

    protected abstract String getDeviceMac();

    public UniFiSite getSite() {
        return cache.getSite(siteId);
    }

    public UniFiDevice getDevice() {
        return cache.getDevice(getDeviceMac());
    }

    public boolean isGuest() {
        return guest;
    }

    public Integer getExperience() {
        return experience;
    }

    @Override
    public String toString() {
        return String.format(
                "UniFiClient{id: '%s', mac: '%s', ip: '%s', hostname: '%s', alias: '%s', wired: %b, guest: %b, blocked: %b, experience: %d, device: %s}",
                id, mac, getIp(), hostname, alias, isWired(), guest, blocked, experience, getDevice());
    }
}
