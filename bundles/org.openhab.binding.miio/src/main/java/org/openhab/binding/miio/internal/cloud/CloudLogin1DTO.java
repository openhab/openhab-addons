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
 * This DTO class wraps the login step 1 json structure
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class CloudLogin1DTO {
    @SerializedName("serviceParam")
    @Expose
    private @Nullable String serviceParam;
    @SerializedName("qs")
    @Expose
    private @Nullable String qs;
    @SerializedName("code")
    @Expose
    private @Nullable Integer code;
    @SerializedName("description")
    @Expose
    private @Nullable String description;
    @SerializedName("securityStatus")
    @Expose
    private @Nullable Integer securityStatus;
    @SerializedName("_sign")
    @Expose
    private @Nullable String sign;
    @SerializedName("sid")
    @Expose
    private @Nullable String sid;
    @SerializedName("result")
    @Expose
    private @Nullable String result;
    @SerializedName("captchaUrl")
    @Expose
    private @Nullable String captchaUrl;
    @SerializedName("callback")
    @Expose
    private @Nullable String callback;
    @SerializedName("location")
    @Expose
    private @Nullable String location;
    @SerializedName("pwd")
    @Expose
    private @Nullable Integer pwd;
    @SerializedName("child")
    @Expose
    private @Nullable Integer child;
    @SerializedName("desc")
    @Expose
    private @Nullable String desc;

    public String getServiceParam() {
        return Objects.requireNonNullElse(serviceParam, "");
    }

    public String getQs() {
        return Objects.requireNonNullElse(qs, "");
    }

    public Integer getCode() {
        return Objects.requireNonNullElse(code, 0);
    }

    public String getDescription() {
        return Objects.requireNonNullElse(description, "");
    }

    public Integer getSecurityStatus() {
        return Objects.requireNonNullElse(securityStatus, -1);
    }

    public String getSign() {
        return Objects.requireNonNullElse(sign, "");
    }

    public String getSid() {
        return Objects.requireNonNullElse(sid, "");
    }

    public String getResult() {
        return Objects.requireNonNullElse(result, "");
    }

    public String getCaptchaUrl() {
        return Objects.requireNonNullElse(captchaUrl, "");
    }

    public String getCallback() {
        return Objects.requireNonNullElse(callback, "");
    }

    public String getLocation() {
        return Objects.requireNonNullElse(location, "");
    }

    public Integer getPwd() {
        return Objects.requireNonNullElse(pwd, 0);
    }

    public Integer getChild() {
        return Objects.requireNonNullElse(child, 0);
    }

    public String getDesc() {
        return Objects.requireNonNullElse(desc, "");
    }
}
