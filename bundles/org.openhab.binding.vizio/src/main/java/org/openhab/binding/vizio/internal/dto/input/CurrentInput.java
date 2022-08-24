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

import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.vizio.internal.dto.Parameters;
import org.openhab.binding.vizio.internal.dto.Status;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link CurrentInput} class maps the JSON data response from the Vizio TV endpoint:
 * '/menu_native/dynamic/tv_settings/devices/current_input'
 *
 * @author Michael Lobstein - Initial contribution
 */
public class CurrentInput {
    @SerializedName("STATUS")
    private Status status;
    @SerializedName("ITEMS")
    private List<ItemInput> items = new ArrayList<ItemInput>();
    @SerializedName("HASHLIST")
    private List<Long> hashlist = new ArrayList<Long>();
    @SerializedName("URI")
    private String uri;
    @SerializedName("PARAMETERS")
    private Parameters parameters;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<ItemInput> getItems() {
        return items;
    }

    public void setItems(List<ItemInput> items) {
        this.items = items;
    }

    public List<Long> getHashlist() {
        return hashlist;
    }

    public void setHashlist(List<Long> hashlist) {
        this.hashlist = hashlist;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }
}
