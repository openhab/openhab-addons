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
package org.openhab.binding.fronius.internal.api;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Head} is responsible for storing
 * the "head" node of the JSON response from the Fronius Solar APIs (V1)
 *
 * The contents of the response object will vary depending on the preceding request but it always contains a common
 * response header and a request body.
 *
 * @author Thomas Rokohl - Initial contribution
 */
public class Head {
    @SerializedName("RequestArguments")
    private HeadRequestArguments requestArguments;
    @SerializedName("Status")
    private HeadStatus status;
    @SerializedName("Timestamp")
    private String timestamp;

    public HeadRequestArguments getRequestArguments() {
        if (requestArguments == null) {
            requestArguments = new HeadRequestArguments();
        }
        return requestArguments;
    }

    public void setRequestArguments(HeadRequestArguments requestArguments) {
        this.requestArguments = requestArguments;
    }

    public HeadStatus getStatus() {
        if (status == null) {
            status = new HeadStatus();
            status.setCode(255);
            status.setReason("undefined runtime error");
        }
        return status;
    }

    public void setStatus(HeadStatus status) {
        this.status = status;
    }

    public String getTimestamp() {
        if (timestamp == null) {
            timestamp = "";
        }
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
