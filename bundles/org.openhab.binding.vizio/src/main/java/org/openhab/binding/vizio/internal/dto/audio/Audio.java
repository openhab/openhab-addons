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
package org.openhab.binding.vizio.internal.dto.audio;

import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.vizio.internal.dto.Parameters;
import org.openhab.binding.vizio.internal.dto.Status;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Audio} class maps the JSON data response from the Vizio TV endpoint:
 * '/menu_native/dynamic/tv_settings/audio'
 *
 * @author Michael Lobstein - Initial contribution
 */
public class Audio {
    @SerializedName("STATUS")
    private Status status;
    @SerializedName("HASHLIST")
    private List<Long> hashlist = new ArrayList<>();
    @SerializedName("GROUP")
    private String group;
    @SerializedName("NAME")
    private String name;
    @SerializedName("PARAMETERS")
    private Parameters parameters;
    @SerializedName("ITEMS")
    private List<ItemAudio> items = new ArrayList<>();
    @SerializedName("URI")
    private String uri;
    @SerializedName("CNAME")
    private String cname;
    @SerializedName("TYPE")
    private String type;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<Long> getHashlist() {
        return hashlist;
    }

    public void setHashlist(List<Long> hashlist) {
        this.hashlist = hashlist;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    public List<ItemAudio> getItems() {
        return items;
    }

    public void setItems(List<ItemAudio> items) {
        this.items = items;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
