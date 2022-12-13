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
package org.openhab.binding.vizio.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Item} class contains data from the Vizio TV JSON response
 *
 * @author Michael Lobstein - Initial contribution
 */
public class Item {
    @SerializedName("HASHVAL")
    private Long hashval;
    @SerializedName("CNAME")
    private String cname;
    @SerializedName("NAME")
    private String name = "";
    @SerializedName("TYPE")
    private String type;
    @SerializedName("ENABLED")
    private String enabled;
    @SerializedName("READONLY")
    private String readonly;
    @SerializedName("VALUE")
    private Value value = new Value();

    public Long getHashval() {
        return hashval;
    }

    public void setHashval(Long hashval) {
        this.hashval = hashval;
    }

    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public String getReadonly() {
        return readonly;
    }

    public void setReadonly(String readonly) {
        this.readonly = readonly;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }
}
