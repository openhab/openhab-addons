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

import com.google.gson.annotations.SerializedName;

/**
 * The {@link UniFiSite} represents the data model of a UniFi site.
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiSite {

    private final transient UniFiController controller;

    public UniFiSite(UniFiController controller) {
        this.controller = controller;
    }

    @SerializedName("_id")
    private String id;

    private String name;

    private String desc;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return desc;
    }

    public boolean matchesName(String siteName) {
        return siteName.equalsIgnoreCase(desc) || siteName.equalsIgnoreCase(name) || siteName.equalsIgnoreCase(id);
    }

    @Override
    public String toString() {
        return String.format("UniFiSite{name: '%s', desc: '%s'}", name, desc);
    }
}
