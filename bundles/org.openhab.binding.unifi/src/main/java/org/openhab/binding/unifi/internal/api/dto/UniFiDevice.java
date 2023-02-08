/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.openhab.binding.unifi.internal.api.cache.UniFiControllerCache;
import org.openhab.binding.unifi.internal.api.util.UniFiTidyLowerCaseStringDeserializer;

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

    private String model;

    private String name;

    private String siteId;

    private UniFiPortTable[] portTable;

    private JsonObject[] portOverrides;

    public UniFiDevice(final UniFiControllerCache cache) {
        this.cache = cache;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getModel() {
        return model;
    }

    public String getName() {
        return name == null || name.isBlank() ? mac : name;
    }

    public String getMac() {
        return mac;
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

    @Override
    public String toString() {
        return String.format("UniFiDevice{mac: '%s', name: '%s', model: '%s', site: %s}", mac, name, model, getSite());
    }
}
