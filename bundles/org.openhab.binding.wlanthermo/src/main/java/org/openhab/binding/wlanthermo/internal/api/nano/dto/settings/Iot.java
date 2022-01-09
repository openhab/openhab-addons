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
public class Iot {

    @SerializedName("PMQhost")
    @Expose
    public String pMQhost;
    @SerializedName("PMQport")
    @Expose
    public Integer pMQport;
    @SerializedName("PMQuser")
    @Expose
    public String pMQuser;
    @SerializedName("PMQpass")
    @Expose
    public String pMQpass;
    @SerializedName("PMQqos")
    @Expose
    public Integer pMQqos;
    @SerializedName("PMQon")
    @Expose
    public Boolean pMQon;
    @SerializedName("PMQint")
    @Expose
    public Integer pMQint;
    @SerializedName("CLon")
    @Expose
    public Boolean cLon;
    @SerializedName("CLtoken")
    @Expose
    public String cLtoken;
    @SerializedName("CLint")
    @Expose
    public Integer cLint;
    @SerializedName("CLurl")
    @Expose
    public String cLurl;
}
