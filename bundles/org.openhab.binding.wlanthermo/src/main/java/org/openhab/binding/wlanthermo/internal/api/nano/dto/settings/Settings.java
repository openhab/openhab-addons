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

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This DTO is used to parse the JSON
 * Class is auto-generated from JSON using http://www.jsonschema2pojo.org/
 *
 * @author Christian Schlipp - Initial contribution
 */
public class Settings {

    @SerializedName("device")
    @Expose
    public Device device;
    @SerializedName("system")
    @Expose
    public System system;
    @SerializedName("hardware")
    @Expose
    public List<String> hardware = new ArrayList<>();
    @SerializedName("api")
    @Expose
    public Api api;
    @SerializedName("sensors")
    @Expose
    public List<String> sensors = new ArrayList<>();
    @SerializedName("pid")
    @Expose
    public List<Pid> pid = new ArrayList<>();
    @SerializedName("aktor")
    @Expose
    public List<String> aktor = new ArrayList<>();
    @SerializedName("iot")
    @Expose
    public Iot iot;
    @SerializedName("notes")
    @Expose
    public Notes notes;
}
