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

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This DTO class wraps the login step 2 json structure
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class CloudLoginDTO {

    @SerializedName("qs")
    @Expose
    private @Nullable String qs;
    @SerializedName("psecurity")
    @Expose
    private @Nullable String psecurity;
    @SerializedName("nonce")
    @Expose
    private @Nullable Integer nonce;
    @SerializedName("ssecurity")
    @Expose
    private @Nullable String ssecurity;
    @SerializedName("passToken")
    @Expose
    private @Nullable String passToken;
    @SerializedName("userId")
    @Expose
    private @Nullable String userId;
    @SerializedName("cUserId")
    @Expose
    private @Nullable String cUserId;
    @SerializedName("securityStatus")
    @Expose
    private @Nullable Integer securityStatus;
    @SerializedName("pwd")
    @Expose
    private @Nullable Integer pwd;
    @SerializedName("code")
    @Expose
    private @Nullable String code;
    @SerializedName("desc")
    @Expose
    private @Nullable String desc;
    @SerializedName("location")
    @Expose
    private @Nullable String location;
    @SerializedName("captchaUrl")
    @Expose
    private @Nullable String captchaUrl;
    @SerializedName("callback")
    @Expose
    private @Nullable String callback;
    @SerializedName("notificationUrl")
    @Expose
    private @Nullable String notificationUrl;

    public String getQs() {
        return Objects.requireNonNullElse(qs, "");
    }

    public String getPsecurity() {
        return Objects.requireNonNullElse(psecurity, "");
    }

    public Integer getNonce() {
        return Objects.requireNonNullElse(nonce, 0);
    }

    public String getSsecurity() {
        return Objects.requireNonNullElse(ssecurity, "");
    }

    public String getPassToken() {
        return Objects.requireNonNullElse(passToken, "");
    }

    public String getUserId() {
        return Objects.requireNonNullElse(userId, "");
    }

    public String getcUserId() {
        return Objects.requireNonNullElse(cUserId, "");
    }

    public Integer getSecurityStatus() {
        return Objects.requireNonNullElse(securityStatus, -1);
    }

    public Integer getPwd() {
        return Objects.requireNonNullElse(pwd, 0);
    }

    public String getCode() {
        return Objects.requireNonNullElse(code, "");
    }

    public String getDesc() {
        return Objects.requireNonNullElse(desc, "");
    }

    public String getLocation() {
        return Objects.requireNonNullElse(location, "");
    }

    public String getCaptchaUrl() {
        return Objects.requireNonNullElse(captchaUrl, "");
    }

    public String getCallback() {
        return Objects.requireNonNullElse(callback, "");
    }

    public String getNotificationUrl() {
        return Objects.requireNonNullElse(notificationUrl, "");
    }
}
