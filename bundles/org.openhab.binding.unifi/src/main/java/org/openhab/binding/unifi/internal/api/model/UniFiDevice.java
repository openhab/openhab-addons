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

import org.openhab.binding.unifi.internal.api.util.UniFiTidyLowerCaseStringDeserializer;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link UniFiDevice} represents the data model of a UniFi Wireless Device
 * (better known as an Access Point).
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiDevice {

    protected final transient UniFiController controller;

    @SerializedName("_id")
    private String id;

    @JsonAdapter(UniFiTidyLowerCaseStringDeserializer.class)
    private String mac;

    private String model;

    private String name;

    private String siteId;

    public UniFiDevice(UniFiController controller) {
        this.controller = controller;
    }

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
        return controller.getSite(siteId);
    }

    @Override
    public String toString() {
        return String.format("UniFiDevice{mac: '%s', name: '%s', model: '%s', site: %s}", mac, name, model, getSite());
    }
}
