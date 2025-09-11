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
package org.openhab.binding.unifi.internal.api.dto;

import org.openhab.binding.unifi.internal.api.cache.UniFiControllerCache;

import com.google.gson.annotations.SerializedName;

/**
 * @author Thomas Lauterbach - Initial contribution
 */
public class UniFiNetwork implements HasId {

    protected final transient UniFiControllerCache cache;

    @SerializedName("_id")
    private String id;

    private String name;

    private String siteId;

    private boolean enabled;

    private String purpose;

    public UniFiNetwork(final UniFiControllerCache cache) {
        this.cache = cache;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UniFiSite getSite() {
        return cache.getSite(siteId);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getPurpose() {
        return purpose;
    }

    @Override
    public String toString() {
        return String.format("UniFiNetwork{id: '%s', name: '%s'}", id, name);
    }
}
