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

package org.openhab.binding.speedtest.internal.dto;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link ResultsContainerServerList} class defines a container for the Speedtest server list.
 *
 * @author Brian Homeyer - Initial contribution
 */

public class ResultsContainerServerList {
    @SerializedName("type")
    @Expose
    public String type;
    @SerializedName("timestamp")
    @Expose
    public String timestamp;
    @SerializedName("servers")
    @Expose
    public List<Server> servers = null;

    public class Server {

        @SerializedName("id")
        @Expose
        public Integer id;
        @SerializedName("name")
        @Expose
        public String name;
        @SerializedName("location")
        @Expose
        public String location;
        @SerializedName("country")
        @Expose
        public String country;
        @SerializedName("host")
        @Expose
        public String host;
        @SerializedName("port")
        @Expose
        public Integer port;
    }
}
