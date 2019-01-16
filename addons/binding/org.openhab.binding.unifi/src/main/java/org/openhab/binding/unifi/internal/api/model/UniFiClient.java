/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.unifi.internal.api.model;

import java.util.Calendar;

import org.apache.commons.lang.BooleanUtils;
import org.openhab.binding.unifi.internal.api.util.UniFiTidyLowerCaseStringDeserializer;
import org.openhab.binding.unifi.internal.api.util.UniFiTimestampDeserializer;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link UniFiClient} is the base data model for any (wired or wireless) connected to a UniFi network.
 *
 * @author Matthew Bowman - Initial contribution
 */
public abstract class UniFiClient {

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

    protected UniFiDevice device;

    public String getId() {
        return id;
    }

    public String getSiteId() {
        return siteId;
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

    public UniFiDevice getDevice() {
        return device;
    }

    public void setDevice(UniFiDevice device) {
        this.device = device;
    }

    public abstract Boolean isWired();

    public final Boolean isWireless() {
        return BooleanUtils.negate(isWired());
    }

    public abstract String getDeviceMac();

    @Override
    public String toString() {
        return String.format("UniFiClient{mac: '%s', ip: '%s', hostname: '%s', alias: '%s', wired: %b, device: %s}",
                mac, ip, hostname, alias, isWired(), device);
    }

}
