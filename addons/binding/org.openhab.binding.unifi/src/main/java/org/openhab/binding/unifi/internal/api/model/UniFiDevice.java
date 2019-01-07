/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.unifi.internal.api.model;

import org.apache.commons.lang.StringUtils;
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

    @SerializedName("_id")
    private String id;

    @JsonAdapter(UniFiTidyLowerCaseStringDeserializer.class)
    private String mac;

    private String model;

    private String name;

    private String siteId;

    private UniFiSite site;

    public String getId() {
        return id;
    }

    public String getModel() {
        return model;
    }

    public String getName() {
        return StringUtils.defaultIfBlank(name, mac);
    }

    public String getMac() {
        return mac;
    }

    public String getSiteId() {
        return siteId;
    }

    public UniFiSite getSite() {
        return site;
    }

    public void setSite(UniFiSite site) {
        this.site = site;
    }

    @Override
    public String toString() {
        return String.format("UniFiDevice{mac: '%s', name: '%s', model: '%s', site: %s}", mac, name, model, site);
    }
}
