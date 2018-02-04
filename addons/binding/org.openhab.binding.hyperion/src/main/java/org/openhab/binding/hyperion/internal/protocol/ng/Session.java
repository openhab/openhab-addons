/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hyperion.internal.protocol.ng;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Session} is a POJO for session information in the Hyperion.ng server.
 *
 * @author Daniel Walters - Initial contribution
 */
public class Session {

    @SerializedName("address")
    private String address;

    @SerializedName("domain")
    private String domain;

    @SerializedName("host")
    private String host;

    @SerializedName("name")
    private String name;

    @SerializedName("port")
    private Integer port;

    @SerializedName("type")
    private String type;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
