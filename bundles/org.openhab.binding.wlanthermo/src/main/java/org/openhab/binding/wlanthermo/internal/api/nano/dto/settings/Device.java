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
package org.openhab.binding.wlanthermo.internal.api.nano.dto.settings;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This DTO is used to parse the JSON
 * Class is auto-generated from JSON using http://www.jsonschema2pojo.org/
 *
 * @author Christian Schlipp - Initial contribution
 */
public class Device {

    @SerializedName("device")
    @Expose
    public String device;
    @SerializedName("serial")
    @Expose
    public String serial;
    @SerializedName("item")
    @Expose
    public String item;
    @SerializedName("hw_version")
    @Expose
    public String hwVersion;
    @SerializedName("sw_version")
    @Expose
    public String swVersion;
    @SerializedName("api_version")
    @Expose
    public String apiVersion;
    @SerializedName("language")
    @Expose
    public String language;
}
