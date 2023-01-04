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
package org.openhab.binding.boschindego.internal.dto.request;

import com.google.gson.annotations.SerializedName;

/**
 * Request for authenticating with server
 * 
 * @author Jacob Laursen - Initial contribution
 */
public class AuthenticationRequest {

    @SerializedName("accept_tc_id")
    public String acceptTcId;

    public String device;

    @SerializedName("os_type")
    public String osType;

    @SerializedName("os_version")
    public String osVersion;

    @SerializedName("dvc_manuf")
    public String deviceManufacturer;

    @SerializedName("dvc_type")
    public String deviceType;

    public AuthenticationRequest() {
        acceptTcId = "202012";
    }
}
