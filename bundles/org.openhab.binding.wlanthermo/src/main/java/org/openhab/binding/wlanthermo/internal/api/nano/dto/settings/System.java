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
package org.openhab.binding.wlanthermo.internal.api.nano.dto.settings;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This DTO is used to parse the JSON
 * Class is auto-generated from JSON using http://www.jsonschema2pojo.org/
 *
 * @author Christian Schlipp - Initial contribution
 */
public class System {

    @SerializedName("time")
    @Expose
    public String time;
    @SerializedName("unit")
    @Expose
    public String unit;
    @SerializedName("ap")
    @Expose
    public String ap;
    @SerializedName("host")
    @Expose
    public String host;
    @SerializedName("language")
    @Expose
    public String language;
    @SerializedName("version")
    @Expose
    public String version;
    @SerializedName("getupdate")
    @Expose
    public String getupdate;
    @SerializedName("autoupd")
    @Expose
    public Boolean autoupd;
    @SerializedName("hwversion")
    @Expose
    public String hwversion;
    @SerializedName("god")
    @Expose
    public Integer god;
}
