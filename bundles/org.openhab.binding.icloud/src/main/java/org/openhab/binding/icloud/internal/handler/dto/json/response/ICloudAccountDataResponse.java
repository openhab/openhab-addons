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
package org.openhab.binding.icloud.internal.handler.dto.json.response;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * Serializable class to parse the device information json response
 * received from the Apple server.
 *
 * @author Patrik Gfeller - Initial Contribution
 */
public class ICloudAccountDataResponse {

    @SerializedName("content")
    private List<ICloudDeviceInformation> iCloudDeviceInformationList;

    @SerializedName("serverContext")
    private ICloudServerContext iCloudServerContext;

    @SerializedName("statusCode")
    private String iCloudAccountStatusCode;

    @SerializedName("userInfo")
    private ICloudAccountUserInfo iCloudAccountUserInfo;

    public List<ICloudDeviceInformation> getICloudDeviceInformationList() {
        return iCloudDeviceInformationList;
    }

    public String getICloudAccountStatusCode() {
        return iCloudAccountStatusCode;
    }

    public ICloudAccountUserInfo getICloudAccountUserInfo() {
        return iCloudAccountUserInfo;
    }
}
