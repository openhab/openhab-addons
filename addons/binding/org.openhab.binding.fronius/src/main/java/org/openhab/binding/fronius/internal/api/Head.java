/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
        return requestArguments;
    }

    public void setRequestArguments(HeadRequestArguments requestArguments) {
        this.requestArguments = requestArguments;
    }

    public HeadStatus getStatus() {
        return status;
    }

    public void setStatus(HeadStatus status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

}
