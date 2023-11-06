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
package org.openhab.binding.wlanthermo.internal.api.nano.dto.settings;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This DTO is used to parse the JSON
 * Class is auto-generated from JSON using http://www.jsonschema2pojo.org/
 *
 * @author Christian Schlipp - Initial contribution
 */
public class Pid {

    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("aktor")
    @Expose
    public Integer aktor;
    @SerializedName("Kp")
    @Expose
    public Double kp;
    @SerializedName("Ki")
    @Expose
    public Double ki;
    @SerializedName("Kd")
    @Expose
    public Double kd;
    @SerializedName("DCmmin")
    @Expose
    public Double dCmmin;
    @SerializedName("DCmmax")
    @Expose
    public Double dCmmax;
    @SerializedName("opl")
    @Expose
    public Integer opl;
    @SerializedName("tune")
    @Expose
    public Integer tune;
    @SerializedName("jp")
    @Expose
    public Integer jp;
}
