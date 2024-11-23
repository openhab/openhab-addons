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
package org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal;

import com.google.gson.annotations.SerializedName;

/**
 * @author Johannes Ptaszyk - Initial contribution
 */
public class Device {
    @SerializedName("did")
    private final String did;

    @SerializedName("name")
    private final String name;

    @SerializedName("class")
    private final String deviceClass;

    @SerializedName("resource")
    private final String resource;

    @SerializedName("nick")
    private final String nick;

    @SerializedName("company")
    private final String company;

    @SerializedName("bindTs")
    private final long bindTs;

    @SerializedName("service")
    private final Service service;

    public Device(String did, String name, String deviceClass, String resource, String nick, String company,
            long bindTs, Service service) {
        this.did = did;
        this.name = name;
        this.deviceClass = deviceClass;
        this.resource = resource;
        this.nick = nick;
        this.company = company;
        this.bindTs = bindTs;
        this.service = service;
    }

    public String getDid() {
        return did;
    }

    public String getName() {
        return name;
    }

    public String getDeviceClass() {
        return deviceClass;
    }

    public String getResource() {
        return resource;
    }

    public String getNick() {
        return nick;
    }

    public String getCompany() {
        return company;
    }

    public long getBindTs() {
        return bindTs;
    }

    public Service getService() {
        return service;
    }
}
