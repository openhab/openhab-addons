/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import com.google.gson.JsonObject;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link UniFiDevice} represents the data model of a UniFi Wireless Device
 * (better known as an Access Point).
 *
 * @author Matthew Bowman - Initial contribution
 * @author Hilbrand Bouwkamp - Added PoEPort support
 */
public class UniFiDevice implements HasId {

    protected final transient UniFiControllerCache cache;

    @SerializedName("_id")
    private String id;

    @JsonAdapter(UniFiTidyLowerCaseStringDeserializer.class)
    private String mac;

    private String ip;

    private String model;

    private String version;

    private String serial;

    private String type;

    private String name;

    private Integer state;

    private Integer uptime;

    @JsonAdapter(UniFiTimestampDeserializer.class)
    private Instant lastSeen;

    private String siteId;

    @SerializedName("satisfaction")
    private Integer experience;

    private UniFiPortTable[] portTable;

    private JsonObject[] portOverrides;

    private Boolean disabled;

    private String ledOverride;

    public UniFiDevice(final UniFiControllerCache cache) {
        this.cache = cache;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getModel() {
        return model;
    }

    public String getVersion() {
        return version;
    }

    public String getSerial() {
        return serial;
    }

    public Integer getExperience() {
        return experience;
    }

    public String getName() {
        return name == null || name.isBlank() ? mac : name;
    }

    public Integer getState() {
        return state;
    }

    public Integer getUptime() {
        return uptime;
    }

    public Instant getLastSeen() {
        return lastSeen;
    }

    public String getMac() {
        return mac;
    }

    public String getIp() {
        return ip;
    }

    public UniFiSite getSite() {
        return cache.getSite(siteId);
    }

    public UniFiPortTable[] getPortTable() {
        return portTable;
    }

    public JsonObject[] getPortOverrides() {
        return portOverrides;
    }

    public Boolean isDisabled() {
        return disabled;
    }

    public String getLedOverride() {
        return ledOverride;
    }

    @Override
    public String toString() {
        return String.format(
                "UniFiDevice{mac: '%s', name: '%s', type: '%s', model: '%s', version: '%s', experience: %d, disabled: %b, led: %s, uptime: %d, site: %s}",
                mac, name, type, model, version, experience, disabled, ledOverride, uptime, getSite());
    }
}
