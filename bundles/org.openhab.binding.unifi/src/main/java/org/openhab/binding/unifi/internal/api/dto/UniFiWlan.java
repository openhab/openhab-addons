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

import com.google.gson.annotations.SerializedName;

/**
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class UniFiWlan implements HasId {

    protected final transient UniFiControllerCache cache;

    @SerializedName("_id")
    private String id;

    private String name;

    private boolean enabled;

    private String security; // ": "wpapsk",
    private String wlanBand; // ": "both",
    private String wpaEnc; // ": "ccmp",
    private String wpaMode;// ": "wpa2",
    private String xPassphrase; // : "1234",
    private Boolean hideSsid;
    private String siteId;

    public UniFiWlan(final UniFiControllerCache cache) {
        this.cache = cache;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public UniFiSite getSite() {
        return cache.getSite(siteId);
    }

    public String getSecurity() {
        return security;
    }

    public String getWlanBand() {
        return wlanBand;
    }

    public String getWpaEnc() {
        return wpaEnc;
    }

    public String getWpaMode() {
        return wpaMode;
    }

    public String getXPassphrase() {
        return xPassphrase;
    }

    public boolean isHideSsid() {
        return Boolean.TRUE.equals(hideSsid);
    }

    @Override
    public String toString() {
        final String xPassphraseString = xPassphrase == null ? ""
                : (xPassphrase.substring(0, Math.min(5, xPassphrase.length())) + "*".repeat(10));

        return String.format(
                "UniFiWlan{id: '%s', name: '%s', enable: '%b', security: '%s', wlanBand: '%s', wpaEnc: '%s', wpaMode: '%s', xPassphrase: '%s', hideSsid: '%b', site: '%s'}",
                id, name, enabled, security, wlanBand, wpaEnc, wpaMode, xPassphraseString, hideSsid, getSite());
    }
}
