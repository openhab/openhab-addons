/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.chamberlainmyq.internal.json;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
/**
 * The {@link MyqJson} is responsible for storing
 * the MyQ Device JSON response data.
 *
 * @author Scott Hanson - Initial contribution
 */
public class MyqJson {

    @SerializedName("Devices")
    @Expose
    private List<Device> devices = null;
    @SerializedName("ReturnCode")
    @Expose
    private String returnCode;
    @SerializedName("ErrorMessage")
    @Expose
    private String errorMessage;
    @SerializedName("CorrelationId")
    @Expose
    private String correlationId;

    public List<Device> getDevices() {
        return devices;
    }

    public String getReturnCode() {
        return returnCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getCorrelationId() {
        return correlationId;
    }

}