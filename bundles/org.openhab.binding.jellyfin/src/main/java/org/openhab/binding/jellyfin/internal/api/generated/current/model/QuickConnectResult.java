/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

package org.openhab.binding.jellyfin.internal.api.generated.current.model;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Stores the state of an quick connect request.
 */
@JsonPropertyOrder({ QuickConnectResult.JSON_PROPERTY_AUTHENTICATED, QuickConnectResult.JSON_PROPERTY_SECRET,
        QuickConnectResult.JSON_PROPERTY_CODE, QuickConnectResult.JSON_PROPERTY_DEVICE_ID,
        QuickConnectResult.JSON_PROPERTY_DEVICE_NAME, QuickConnectResult.JSON_PROPERTY_APP_NAME,
        QuickConnectResult.JSON_PROPERTY_APP_VERSION, QuickConnectResult.JSON_PROPERTY_DATE_ADDED })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class QuickConnectResult {
    public static final String JSON_PROPERTY_AUTHENTICATED = "Authenticated";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean authenticated;

    public static final String JSON_PROPERTY_SECRET = "Secret";
    @org.eclipse.jdt.annotation.NonNull
    private String secret;

    public static final String JSON_PROPERTY_CODE = "Code";
    @org.eclipse.jdt.annotation.NonNull
    private String code;

    public static final String JSON_PROPERTY_DEVICE_ID = "DeviceId";
    @org.eclipse.jdt.annotation.NonNull
    private String deviceId;

    public static final String JSON_PROPERTY_DEVICE_NAME = "DeviceName";
    @org.eclipse.jdt.annotation.NonNull
    private String deviceName;

    public static final String JSON_PROPERTY_APP_NAME = "AppName";
    @org.eclipse.jdt.annotation.NonNull
    private String appName;

    public static final String JSON_PROPERTY_APP_VERSION = "AppVersion";
    @org.eclipse.jdt.annotation.NonNull
    private String appVersion;

    public static final String JSON_PROPERTY_DATE_ADDED = "DateAdded";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime dateAdded;

    public QuickConnectResult() {
    }

    public QuickConnectResult authenticated(@org.eclipse.jdt.annotation.NonNull Boolean authenticated) {
        this.authenticated = authenticated;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this request is authorized.
     * 
     * @return authenticated
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_AUTHENTICATED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getAuthenticated() {
        return authenticated;
    }

    @JsonProperty(value = JSON_PROPERTY_AUTHENTICATED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAuthenticated(@org.eclipse.jdt.annotation.NonNull Boolean authenticated) {
        this.authenticated = authenticated;
    }

    public QuickConnectResult secret(@org.eclipse.jdt.annotation.NonNull String secret) {
        this.secret = secret;
        return this;
    }

    /**
     * Gets the secret value used to uniquely identify this request. Can be used to retrieve authentication information.
     * 
     * @return secret
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SECRET, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getSecret() {
        return secret;
    }

    @JsonProperty(value = JSON_PROPERTY_SECRET, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSecret(@org.eclipse.jdt.annotation.NonNull String secret) {
        this.secret = secret;
    }

    public QuickConnectResult code(@org.eclipse.jdt.annotation.NonNull String code) {
        this.code = code;
        return this;
    }

    /**
     * Gets the user facing code used so the user can quickly differentiate this request from others.
     * 
     * @return code
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CODE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getCode() {
        return code;
    }

    @JsonProperty(value = JSON_PROPERTY_CODE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCode(@org.eclipse.jdt.annotation.NonNull String code) {
        this.code = code;
    }

    public QuickConnectResult deviceId(@org.eclipse.jdt.annotation.NonNull String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    /**
     * Gets the requesting device id.
     * 
     * @return deviceId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_DEVICE_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getDeviceId() {
        return deviceId;
    }

    @JsonProperty(value = JSON_PROPERTY_DEVICE_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDeviceId(@org.eclipse.jdt.annotation.NonNull String deviceId) {
        this.deviceId = deviceId;
    }

    public QuickConnectResult deviceName(@org.eclipse.jdt.annotation.NonNull String deviceName) {
        this.deviceName = deviceName;
        return this;
    }

    /**
     * Gets the requesting device name.
     * 
     * @return deviceName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_DEVICE_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getDeviceName() {
        return deviceName;
    }

    @JsonProperty(value = JSON_PROPERTY_DEVICE_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDeviceName(@org.eclipse.jdt.annotation.NonNull String deviceName) {
        this.deviceName = deviceName;
    }

    public QuickConnectResult appName(@org.eclipse.jdt.annotation.NonNull String appName) {
        this.appName = appName;
        return this;
    }

    /**
     * Gets the requesting app name.
     * 
     * @return appName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_APP_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getAppName() {
        return appName;
    }

    @JsonProperty(value = JSON_PROPERTY_APP_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAppName(@org.eclipse.jdt.annotation.NonNull String appName) {
        this.appName = appName;
    }

    public QuickConnectResult appVersion(@org.eclipse.jdt.annotation.NonNull String appVersion) {
        this.appVersion = appVersion;
        return this;
    }

    /**
     * Gets the requesting app version.
     * 
     * @return appVersion
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_APP_VERSION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getAppVersion() {
        return appVersion;
    }

    @JsonProperty(value = JSON_PROPERTY_APP_VERSION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAppVersion(@org.eclipse.jdt.annotation.NonNull String appVersion) {
        this.appVersion = appVersion;
    }

    public QuickConnectResult dateAdded(@org.eclipse.jdt.annotation.NonNull OffsetDateTime dateAdded) {
        this.dateAdded = dateAdded;
        return this;
    }

    /**
     * Gets or sets the DateTime that this request was created.
     * 
     * @return dateAdded
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_DATE_ADDED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getDateAdded() {
        return dateAdded;
    }

    @JsonProperty(value = JSON_PROPERTY_DATE_ADDED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDateAdded(@org.eclipse.jdt.annotation.NonNull OffsetDateTime dateAdded) {
        this.dateAdded = dateAdded;
    }

    /**
     * Return true if this QuickConnectResult object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QuickConnectResult quickConnectResult = (QuickConnectResult) o;
        return Objects.equals(this.authenticated, quickConnectResult.authenticated)
                && Objects.equals(this.secret, quickConnectResult.secret)
                && Objects.equals(this.code, quickConnectResult.code)
                && Objects.equals(this.deviceId, quickConnectResult.deviceId)
                && Objects.equals(this.deviceName, quickConnectResult.deviceName)
                && Objects.equals(this.appName, quickConnectResult.appName)
                && Objects.equals(this.appVersion, quickConnectResult.appVersion)
                && Objects.equals(this.dateAdded, quickConnectResult.dateAdded);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authenticated, secret, code, deviceId, deviceName, appName, appVersion, dateAdded);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class QuickConnectResult {\n");
        sb.append("    authenticated: ").append(toIndentedString(authenticated)).append("\n");
        sb.append("    secret: ").append(toIndentedString(secret)).append("\n");
        sb.append("    code: ").append(toIndentedString(code)).append("\n");
        sb.append("    deviceId: ").append(toIndentedString(deviceId)).append("\n");
        sb.append("    deviceName: ").append(toIndentedString(deviceName)).append("\n");
        sb.append("    appName: ").append(toIndentedString(appName)).append("\n");
        sb.append("    appVersion: ").append(toIndentedString(appVersion)).append("\n");
        sb.append("    dateAdded: ").append(toIndentedString(dateAdded)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

    /**
     * Convert the instance into URL query string.
     *
     * @return URL query string
     */
    public String toUrlQueryString() {
        return toUrlQueryString(null);
    }

    /**
     * Convert the instance into URL query string.
     *
     * @param prefix prefix of the query string
     * @return URL query string
     */
    public String toUrlQueryString(String prefix) {
        String suffix = "";
        String containerSuffix = "";
        String containerPrefix = "";
        if (prefix == null) {
            // style=form, explode=true, e.g. /pet?name=cat&type=manx
            prefix = "";
        } else {
            // deepObject style e.g. /pet?id[name]=cat&id[type]=manx
            prefix = prefix + "[";
            suffix = "]";
            containerSuffix = "]";
            containerPrefix = "[";
        }

        StringJoiner joiner = new StringJoiner("&");

        // add `Authenticated` to the URL query string
        if (getAuthenticated() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAuthenticated%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAuthenticated()))));
        }

        // add `Secret` to the URL query string
        if (getSecret() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSecret%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSecret()))));
        }

        // add `Code` to the URL query string
        if (getCode() != null) {
            joiner.add(String.format(Locale.ROOT, "%sCode%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCode()))));
        }

        // add `DeviceId` to the URL query string
        if (getDeviceId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sDeviceId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDeviceId()))));
        }

        // add `DeviceName` to the URL query string
        if (getDeviceName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sDeviceName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDeviceName()))));
        }

        // add `AppName` to the URL query string
        if (getAppName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAppName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAppName()))));
        }

        // add `AppVersion` to the URL query string
        if (getAppVersion() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAppVersion%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAppVersion()))));
        }

        // add `DateAdded` to the URL query string
        if (getDateAdded() != null) {
            joiner.add(String.format(Locale.ROOT, "%sDateAdded%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDateAdded()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private QuickConnectResult instance;

        public Builder() {
            this(new QuickConnectResult());
        }

        protected Builder(QuickConnectResult instance) {
            this.instance = instance;
        }

        public QuickConnectResult.Builder authenticated(Boolean authenticated) {
            this.instance.authenticated = authenticated;
            return this;
        }

        public QuickConnectResult.Builder secret(String secret) {
            this.instance.secret = secret;
            return this;
        }

        public QuickConnectResult.Builder code(String code) {
            this.instance.code = code;
            return this;
        }

        public QuickConnectResult.Builder deviceId(String deviceId) {
            this.instance.deviceId = deviceId;
            return this;
        }

        public QuickConnectResult.Builder deviceName(String deviceName) {
            this.instance.deviceName = deviceName;
            return this;
        }

        public QuickConnectResult.Builder appName(String appName) {
            this.instance.appName = appName;
            return this;
        }

        public QuickConnectResult.Builder appVersion(String appVersion) {
            this.instance.appVersion = appVersion;
            return this;
        }

        public QuickConnectResult.Builder dateAdded(OffsetDateTime dateAdded) {
            this.instance.dateAdded = dateAdded;
            return this;
        }

        /**
         * returns a built QuickConnectResult instance.
         *
         * The builder is not reusable.
         */
        public QuickConnectResult build() {
            try {
                return this.instance;
            } finally {
                // ensure that this.instance is not reused
                this.instance = null;
            }
        }

        @Override
        public String toString() {
            return getClass() + "=(" + instance + ")";
        }
    }

    /**
     * Create a builder with no initialized field.
     */
    public static QuickConnectResult.Builder builder() {
        return new QuickConnectResult.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public QuickConnectResult.Builder toBuilder() {
        return new QuickConnectResult.Builder().authenticated(getAuthenticated()).secret(getSecret()).code(getCode())
                .deviceId(getDeviceId()).deviceName(getDeviceName()).appName(getAppName()).appVersion(getAppVersion())
                .dateAdded(getDateAdded());
    }
}
