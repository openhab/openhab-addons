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
package org.openhab.binding.miio.internal.cloud;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This DTO class wraps the login step 1 json structure
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class CloudLogin1DTO {
    @SerializedName("serviceParam")
    @Expose
    private String serviceParam;
    @SerializedName("qs")
    @Expose
    private String qs;
    @SerializedName("code")
    @Expose
    private Integer code;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("securityStatus")
    @Expose
    private Integer securityStatus;
    @SerializedName("_sign")
    @Expose
    private String sign;
    @SerializedName("sid")
    @Expose
    private String sid;
    @SerializedName("result")
    @Expose
    private String result;
    @SerializedName("captchaUrl")
    @Expose
    private String captchaUrl;
    @SerializedName("callback")
    @Expose
    private String callback;
    @SerializedName("location")
    @Expose
    private String location;
    @SerializedName("pwd")
    @Expose
    private Integer pwd;
    @SerializedName("child")
    @Expose
    private Integer child;
    @SerializedName("desc")
    @Expose
    private String desc;

    public String getServiceParam() {
        return serviceParam;
    }

    public String getQs() {
        return qs;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public Integer getSecurityStatus() {
        return securityStatus;
    }

    public String getSign() {
        return sign;
    }

    public String getSid() {
        return sid;
    }

    public String getResult() {
        return result;
    }

    public String getCaptchaUrl() {
        return captchaUrl;
    }

    public String getCallback() {
        return callback;
    }

    public String getLocation() {
        return location;
    }

    public Integer getPwd() {
        return pwd;
    }

    public Integer getChild() {
        return child;
    }

    public String getDesc() {
        return desc;
    }
}
