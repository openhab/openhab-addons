/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.ecovacs.internal.api.impl.dto.response.main;

import com.google.gson.annotations.SerializedName;

/**
 * @author Johannes Ptaszyk - Initial contribution
 */
public class AccessData {

    @SerializedName("uid")
    private final String uid;

    @SerializedName("accessToken")
    private final String accessToken;

    @SerializedName("userName")
    private final String userName;

    @SerializedName("email")
    private final String email;

    @SerializedName("mobile")
    private final String mobile;

    @SerializedName("isNew")
    private final boolean isNew;

    @SerializedName("loginName")
    private final String loginName;

    @SerializedName("ucUid")
    private final String ucUid;

    public AccessData(String uid, String accessToken, String userName, String email, String mobile, boolean isNew,
            String loginName, String ucUid) {
        this.uid = uid;
        this.accessToken = accessToken;
        this.userName = userName;
        this.email = email;
        this.mobile = mobile;
        this.isNew = isNew;
        this.loginName = loginName;
        this.ucUid = ucUid;
    }

    public String getUid() {
        return uid;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    public String getMobile() {
        return mobile;
    }

    public boolean isNew() {
        return isNew;
    }

    public String getLoginName() {
        return loginName;
    }

    public String getUcUid() {
        return ucUid;
    }
}
