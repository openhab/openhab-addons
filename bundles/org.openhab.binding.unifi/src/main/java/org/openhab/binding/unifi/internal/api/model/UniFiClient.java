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
package org.openhab.binding.unifi.internal.api.model;

import java.util.Calendar;

import org.openhab.binding.unifi.internal.api.UniFiException;
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
public abstract class UniFiClient {

    protected final transient UniFiController controller;

    @SerializedName("_id")
    protected String id;

    protected String siteId;

    @JsonAdapter(UniFiTidyLowerCaseStringDeserializer.class)
    protected String mac;

    protected String ip;

    @JsonAdapter(UniFiTidyLowerCaseStringDeserializer.class)
    protected String hostname;

    @SerializedName("name")
    @JsonAdapter(UniFiTidyLowerCaseStringDeserializer.class)
    protected String alias;

    protected Integer uptime;

    @JsonAdapter(UniFiTimestampDeserializer.class)
    protected Calendar lastSeen;

    protected boolean blocked;

    protected UniFiClient(UniFiController controller) {
        this.controller = controller;
    }

    public String getId() {
        return id;
    }

    public String getMac() {
        return mac;
    }

    public String getIp() {
        return this.ip;
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

    public Calendar getLastSeen() {
        return lastSeen;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public abstract Boolean isWired();

    public final Boolean isWireless() {
        return isWired() == null ? null : (isWired().booleanValue() ? Boolean.FALSE : Boolean.TRUE);
    }

    protected abstract String getDeviceMac();

    public UniFiSite getSite() {
        return controller.getSite(siteId);
    }

    public UniFiDevice getDevice() {
        return controller.getDevice(getDeviceMac());
    }

    // Functional API

    public void block(boolean blocked) throws UniFiException {
        controller.block(this, blocked);
    }

    public void reconnect() throws UniFiException {
        controller.reconnect(this);
    }

    @Override
    public String toString() {
        return String.format(
                "UniFiClient{id: '%s', mac: '%s', ip: '%s', hostname: '%s', alias: '%s', wired: %b, blocked: %b, device: %s}",
                id, mac, ip, hostname, alias, isWired(), blocked, getDevice());
    }
}
