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
package org.openhab.binding.vizio.internal.dto.input;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ItemInput} class contains data from the Vizio TV JSON response
 *
 * @author Michael Lobstein - Initial contribution
 */
public class ItemInput {
    @SerializedName("HASHVAL")
    private Long hashval = 0L;
    @SerializedName("NAME")
    private String name;
    @SerializedName("ENABLED")
    private String enabled;
    @SerializedName("VALUE")
    private String value = "";
    @SerializedName("CNAME")
    private String cname;
    @SerializedName("HIDDEN")
    private String hidden;
    @SerializedName("TYPE")
    private String type;

    public Long getHashval() {
        return hashval;
    }

    public void setHashval(Long hashval) {
        this.hashval = hashval;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public String getHidden() {
        return hidden;
    }

    public void setHidden(String hidden) {
        this.hidden = hidden;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
