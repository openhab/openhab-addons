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
package org.openhab.binding.vizio.internal.dto.app;

import org.openhab.binding.vizio.internal.dto.Status;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link CurrentApp} class maps the JSON data response from the Vizio TV endpoint:
 * '/app/current'
 *
 * @author Michael Lobstein - Initial contribution
 */
public class CurrentApp {
    @SerializedName("STATUS")
    private Status status;
    @SerializedName("ITEM")
    private ItemApp item = new ItemApp();
    @SerializedName("URI")
    private String uri;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public ItemApp getItem() {
        return item;
    }

    public void setItem(ItemApp item) {
        this.item = item;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
