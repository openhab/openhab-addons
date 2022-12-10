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
package org.openhab.binding.vizio.internal.dto.power;

import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.vizio.internal.dto.Status;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link PowerMode} class maps the JSON data response from the Vizio TV endpoint:
 * '/state/device/power_mode'
 *
 * @author Michael Lobstein - Initial contribution
 */
public class PowerMode {
    @SerializedName("STATUS")
    private Status status;
    @SerializedName("ITEMS")
    private List<ItemPower> items = new ArrayList<ItemPower>();
    @SerializedName("URI")
    private String uri;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<ItemPower> getItems() {
        return items;
    }

    public void setItems(List<ItemPower> items) {
        this.items = items;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
