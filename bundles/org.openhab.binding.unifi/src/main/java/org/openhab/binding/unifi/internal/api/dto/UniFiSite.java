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
 * The {@link UniFiSite} represents the data model of a UniFi site.
 *
 * @author Matthew Bowman - Initial contribution
 * @author Mark Herwege - Added guest vouchers
 */
public class UniFiSite implements HasId {

    private final transient UniFiControllerCache cache;

    public UniFiSite(final UniFiControllerCache cache) {
        this.cache = cache;
    }

    @SerializedName("_id")
    private String id;

    private String name;

    private String desc;

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return desc;
    }

    public UniFiControllerCache getCache() {
        return cache;
    }

    public String getVoucher() {
        UniFiVoucher voucher = cache.getVoucher(this);
        if (voucher == null) {
            return null;
        }
        return voucher.getCode();
    }

    public boolean isSite(final UniFiSite site) {
        return site != null && id.equals(site.getId());
    }

    public boolean matchesName(final String siteName) {
        return siteName.equalsIgnoreCase(desc) || siteName.equalsIgnoreCase(name) || siteName.equalsIgnoreCase(id);
    }

    @Override
    public String toString() {
        return String.format("UniFiSite{id: '%s', name: '%s', desc: '%s'}", id, name, desc);
    }
}
