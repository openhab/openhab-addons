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
package org.openhab.binding.vizio.internal.dto.app;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ItemAppValue} class contains data from the Vizio TV JSON response
 *
 * @author Michael Lobstein - Initial contribution
 */
public class ItemAppValue {
    @SerializedName("MESSAGE")
    private String message;
    @SerializedName("NAME_SPACE")
    private Integer nameSpace = -1;
    @SerializedName("APP_ID")
    private String appId = "";

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getNameSpace() {
        return nameSpace;
    }

    public void setNameSpace(Integer nameSpace) {
        this.nameSpace = nameSpace;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }
}
