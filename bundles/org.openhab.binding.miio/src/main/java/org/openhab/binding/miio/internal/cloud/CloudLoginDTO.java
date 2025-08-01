/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.miio.internal.cloud;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This DTO class wraps the login step 2 json structure
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class CloudLoginDTO {

    @SerializedName("qs")
    @Expose
    private String qs;
    @SerializedName("psecurity")
    @Expose
    private String psecurity;
    @SerializedName("nonce")
    @Expose
    private Integer nonce;
    @SerializedName("ssecurity")
    @Expose
    private String ssecurity;
    @SerializedName("passToken")
    @Expose
    private String passToken;
    @SerializedName("userId")
    @Expose
    private String userId;
    @SerializedName("cUserId")
    @Expose
    private String cUserId;
    @SerializedName("securityStatus")
    @Expose
    private Integer securityStatus;
    @SerializedName("pwd")
    @Expose
    private Integer pwd;
    @SerializedName("code")
    @Expose
    private String code;
    @SerializedName("desc")
    @Expose
    private String desc;
    @SerializedName("location")
    @Expose
    private String location;
    @SerializedName("capchaUrl")
    @Expose
    private String capchaUrl;
    @SerializedName("callback")
    @Expose
    private String callback;
    @SerializedName("notificationUrl")
    @Expose
    private String notificationUrl;

    public String getSsecurity() {
        return ssecurity != null ? ssecurity : "";
    }

    public String getUserId() {
        return userId != null ? userId : "";
    }

    public String getcUserId() {
        return cUserId != null ? cUserId : "";
    }

    public String getPassToken() {
        return passToken != null ? passToken : "";
    }

    public String getLocation() {
        return location != null ? location : "";
    }

    public String getCode() {
        return code;
    }

    public String getQs() {
        return qs;
    }

    public String getPsecurity() {
        return psecurity;
    }

    public Integer getNonce() {
        return nonce;
    }

    public String getCUserId() {
        return cUserId;
    }

    public Integer getSecurityStatus() {
        return securityStatus;
    }

    public Integer getPwd() {
        return pwd;
    }

    public String getDesc() {
        return desc;
    }

    public String getCapchaUrl() {
        return capchaUrl;
    }

    public void setCapchaUrl(String capchaUrl) {
        this.capchaUrl = capchaUrl;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public String getNotificationUrl() {
        return notificationUrl;
    }

    public void setNotificationUrl(String notificationUrl) {
        this.notificationUrl = notificationUrl;
    }
}
