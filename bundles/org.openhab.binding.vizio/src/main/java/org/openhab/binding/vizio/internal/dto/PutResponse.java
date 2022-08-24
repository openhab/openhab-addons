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
 * The {@link PutResponse} class maps the JSON data response from several Vizio TV endpoints
 *
 * @author Michael Lobstein - Initial contribution
 */
public class PutResponse {
    @SerializedName("STATUS")
    private Status status;
    @SerializedName("URI")
    private String uri;
    @SerializedName("TIME")
    private String time;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
