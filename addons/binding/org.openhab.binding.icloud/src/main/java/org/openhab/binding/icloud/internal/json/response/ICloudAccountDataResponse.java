/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.internal.json.response;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;

/**
 * Serializable class to parse the device information json response
 * received from the Apple server.
 *
 * @author Patrik Gfeller - Initial Contribution
 *
 */
public class ICloudAccountDataResponse {

    @SerializedName("content")
    private ArrayList<ICloudDeviceInformation> iCloudDeviceInformationList;

    @SerializedName("serverContext")
    private ICloudServerContext iCloudServerContext;

    @SerializedName("statusCode")
    private String iCloudAccountStatusCode;

    @SerializedName("userInfo")
    private ICloudAccountUserInfo iCloudAccountUserInfo;

    public ArrayList<ICloudDeviceInformation> getICloudDeviceInformationList() {
        return this.iCloudDeviceInformationList;
    }

    public String getICloudAccountStatusCode() {
        return this.iCloudAccountStatusCode;
    }

    public ICloudAccountUserInfo getICloudAccountUserInfo() {
        return this.iCloudAccountUserInfo;
    }
}