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

package org.openhab.binding.jellyfin.internal.api.generated.legacy.model;

import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * The DlnaOptions class contains the user definable parameters for the dlna subsystems.
 */
@JsonPropertyOrder({ DlnaOptions.JSON_PROPERTY_ENABLE_PLAY_TO, DlnaOptions.JSON_PROPERTY_ENABLE_SERVER,
        DlnaOptions.JSON_PROPERTY_ENABLE_DEBUG_LOG, DlnaOptions.JSON_PROPERTY_ENABLE_PLAY_TO_TRACING,
        DlnaOptions.JSON_PROPERTY_CLIENT_DISCOVERY_INTERVAL_SECONDS,
        DlnaOptions.JSON_PROPERTY_ALIVE_MESSAGE_INTERVAL_SECONDS,
        DlnaOptions.JSON_PROPERTY_BLAST_ALIVE_MESSAGE_INTERVAL_SECONDS, DlnaOptions.JSON_PROPERTY_DEFAULT_USER_ID,
        DlnaOptions.JSON_PROPERTY_AUTO_CREATE_PLAY_TO_PROFILES, DlnaOptions.JSON_PROPERTY_BLAST_ALIVE_MESSAGES,
        DlnaOptions.JSON_PROPERTY_SEND_ONLY_MATCHED_HOST })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class DlnaOptions {
    public static final String JSON_PROPERTY_ENABLE_PLAY_TO = "EnablePlayTo";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enablePlayTo;

    public static final String JSON_PROPERTY_ENABLE_SERVER = "EnableServer";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableServer;

    public static final String JSON_PROPERTY_ENABLE_DEBUG_LOG = "EnableDebugLog";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableDebugLog;

    public static final String JSON_PROPERTY_ENABLE_PLAY_TO_TRACING = "EnablePlayToTracing";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enablePlayToTracing;

    public static final String JSON_PROPERTY_CLIENT_DISCOVERY_INTERVAL_SECONDS = "ClientDiscoveryIntervalSeconds";
    @org.eclipse.jdt.annotation.NonNull
    private Integer clientDiscoveryIntervalSeconds;

    public static final String JSON_PROPERTY_ALIVE_MESSAGE_INTERVAL_SECONDS = "AliveMessageIntervalSeconds";
    @org.eclipse.jdt.annotation.NonNull
    private Integer aliveMessageIntervalSeconds;

    public static final String JSON_PROPERTY_BLAST_ALIVE_MESSAGE_INTERVAL_SECONDS = "BlastAliveMessageIntervalSeconds";
    @org.eclipse.jdt.annotation.NonNull
    private Integer blastAliveMessageIntervalSeconds;

    public static final String JSON_PROPERTY_DEFAULT_USER_ID = "DefaultUserId";
    @org.eclipse.jdt.annotation.NonNull
    private String defaultUserId;

    public static final String JSON_PROPERTY_AUTO_CREATE_PLAY_TO_PROFILES = "AutoCreatePlayToProfiles";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean autoCreatePlayToProfiles;

    public static final String JSON_PROPERTY_BLAST_ALIVE_MESSAGES = "BlastAliveMessages";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean blastAliveMessages;

    public static final String JSON_PROPERTY_SEND_ONLY_MATCHED_HOST = "SendOnlyMatchedHost";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean sendOnlyMatchedHost;

    public DlnaOptions() {
    }

    public DlnaOptions enablePlayTo(@org.eclipse.jdt.annotation.NonNull Boolean enablePlayTo) {
        this.enablePlayTo = enablePlayTo;
        return this;
    }

    /**
     * Gets or sets a value indicating whether gets or sets a value to indicate the status of the dlna playTo subsystem.
     * 
     * @return enablePlayTo
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_PLAY_TO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnablePlayTo() {
        return enablePlayTo;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_PLAY_TO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnablePlayTo(@org.eclipse.jdt.annotation.NonNull Boolean enablePlayTo) {
        this.enablePlayTo = enablePlayTo;
    }

    public DlnaOptions enableServer(@org.eclipse.jdt.annotation.NonNull Boolean enableServer) {
        this.enableServer = enableServer;
        return this;
    }

    /**
     * Gets or sets a value indicating whether gets or sets a value to indicate the status of the dlna server subsystem.
     * 
     * @return enableServer
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_SERVER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableServer() {
        return enableServer;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_SERVER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableServer(@org.eclipse.jdt.annotation.NonNull Boolean enableServer) {
        this.enableServer = enableServer;
    }

    public DlnaOptions enableDebugLog(@org.eclipse.jdt.annotation.NonNull Boolean enableDebugLog) {
        this.enableDebugLog = enableDebugLog;
        return this;
    }

    /**
     * Gets or sets a value indicating whether detailed dlna server logs are sent to the console/log. If the setting
     * \&quot;Emby.Dlna\&quot;: \&quot;Debug\&quot; msut be set in logging.default.json for this property to work.
     * 
     * @return enableDebugLog
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_DEBUG_LOG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableDebugLog() {
        return enableDebugLog;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_DEBUG_LOG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableDebugLog(@org.eclipse.jdt.annotation.NonNull Boolean enableDebugLog) {
        this.enableDebugLog = enableDebugLog;
    }

    public DlnaOptions enablePlayToTracing(@org.eclipse.jdt.annotation.NonNull Boolean enablePlayToTracing) {
        this.enablePlayToTracing = enablePlayToTracing;
        return this;
    }

    /**
     * Gets or sets a value indicating whether whether detailed playTo debug logs are sent to the console/log. If the
     * setting \&quot;Emby.Dlna.PlayTo\&quot;: \&quot;Debug\&quot; msut be set in logging.default.json for this property
     * to work.
     * 
     * @return enablePlayToTracing
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_PLAY_TO_TRACING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnablePlayToTracing() {
        return enablePlayToTracing;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_PLAY_TO_TRACING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnablePlayToTracing(@org.eclipse.jdt.annotation.NonNull Boolean enablePlayToTracing) {
        this.enablePlayToTracing = enablePlayToTracing;
    }

    public DlnaOptions clientDiscoveryIntervalSeconds(
            @org.eclipse.jdt.annotation.NonNull Integer clientDiscoveryIntervalSeconds) {
        this.clientDiscoveryIntervalSeconds = clientDiscoveryIntervalSeconds;
        return this;
    }

    /**
     * Gets or sets the ssdp client discovery interval time (in seconds). This is the time after which the server will
     * send a ssdp search request.
     * 
     * @return clientDiscoveryIntervalSeconds
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CLIENT_DISCOVERY_INTERVAL_SECONDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getClientDiscoveryIntervalSeconds() {
        return clientDiscoveryIntervalSeconds;
    }

    @JsonProperty(JSON_PROPERTY_CLIENT_DISCOVERY_INTERVAL_SECONDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setClientDiscoveryIntervalSeconds(
            @org.eclipse.jdt.annotation.NonNull Integer clientDiscoveryIntervalSeconds) {
        this.clientDiscoveryIntervalSeconds = clientDiscoveryIntervalSeconds;
    }

    public DlnaOptions aliveMessageIntervalSeconds(
            @org.eclipse.jdt.annotation.NonNull Integer aliveMessageIntervalSeconds) {
        this.aliveMessageIntervalSeconds = aliveMessageIntervalSeconds;
        return this;
    }

    /**
     * Gets or sets the frequency at which ssdp alive notifications are transmitted.
     * 
     * @return aliveMessageIntervalSeconds
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ALIVE_MESSAGE_INTERVAL_SECONDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getAliveMessageIntervalSeconds() {
        return aliveMessageIntervalSeconds;
    }

    @JsonProperty(JSON_PROPERTY_ALIVE_MESSAGE_INTERVAL_SECONDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAliveMessageIntervalSeconds(
            @org.eclipse.jdt.annotation.NonNull Integer aliveMessageIntervalSeconds) {
        this.aliveMessageIntervalSeconds = aliveMessageIntervalSeconds;
    }

    public DlnaOptions blastAliveMessageIntervalSeconds(
            @org.eclipse.jdt.annotation.NonNull Integer blastAliveMessageIntervalSeconds) {
        this.blastAliveMessageIntervalSeconds = blastAliveMessageIntervalSeconds;
        return this;
    }

    /**
     * Gets or sets the frequency at which ssdp alive notifications are transmitted. MIGRATING - TO BE REMOVED ONCE WEB
     * HAS BEEN ALTERED.
     * 
     * @return blastAliveMessageIntervalSeconds
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_BLAST_ALIVE_MESSAGE_INTERVAL_SECONDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getBlastAliveMessageIntervalSeconds() {
        return blastAliveMessageIntervalSeconds;
    }

    @JsonProperty(JSON_PROPERTY_BLAST_ALIVE_MESSAGE_INTERVAL_SECONDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBlastAliveMessageIntervalSeconds(
            @org.eclipse.jdt.annotation.NonNull Integer blastAliveMessageIntervalSeconds) {
        this.blastAliveMessageIntervalSeconds = blastAliveMessageIntervalSeconds;
    }

    public DlnaOptions defaultUserId(@org.eclipse.jdt.annotation.NonNull String defaultUserId) {
        this.defaultUserId = defaultUserId;
        return this;
    }

    /**
     * Gets or sets the default user account that the dlna server uses.
     * 
     * @return defaultUserId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DEFAULT_USER_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getDefaultUserId() {
        return defaultUserId;
    }

    @JsonProperty(JSON_PROPERTY_DEFAULT_USER_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDefaultUserId(@org.eclipse.jdt.annotation.NonNull String defaultUserId) {
        this.defaultUserId = defaultUserId;
    }

    public DlnaOptions autoCreatePlayToProfiles(@org.eclipse.jdt.annotation.NonNull Boolean autoCreatePlayToProfiles) {
        this.autoCreatePlayToProfiles = autoCreatePlayToProfiles;
        return this;
    }

    /**
     * Gets or sets a value indicating whether playTo device profiles should be created.
     * 
     * @return autoCreatePlayToProfiles
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_AUTO_CREATE_PLAY_TO_PROFILES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getAutoCreatePlayToProfiles() {
        return autoCreatePlayToProfiles;
    }

    @JsonProperty(JSON_PROPERTY_AUTO_CREATE_PLAY_TO_PROFILES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAutoCreatePlayToProfiles(@org.eclipse.jdt.annotation.NonNull Boolean autoCreatePlayToProfiles) {
        this.autoCreatePlayToProfiles = autoCreatePlayToProfiles;
    }

    public DlnaOptions blastAliveMessages(@org.eclipse.jdt.annotation.NonNull Boolean blastAliveMessages) {
        this.blastAliveMessages = blastAliveMessages;
        return this;
    }

    /**
     * Gets or sets a value indicating whether to blast alive messages.
     * 
     * @return blastAliveMessages
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_BLAST_ALIVE_MESSAGES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getBlastAliveMessages() {
        return blastAliveMessages;
    }

    @JsonProperty(JSON_PROPERTY_BLAST_ALIVE_MESSAGES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBlastAliveMessages(@org.eclipse.jdt.annotation.NonNull Boolean blastAliveMessages) {
        this.blastAliveMessages = blastAliveMessages;
    }

    public DlnaOptions sendOnlyMatchedHost(@org.eclipse.jdt.annotation.NonNull Boolean sendOnlyMatchedHost) {
        this.sendOnlyMatchedHost = sendOnlyMatchedHost;
        return this;
    }

    /**
     * gets or sets a value indicating whether to send only matched host.
     * 
     * @return sendOnlyMatchedHost
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SEND_ONLY_MATCHED_HOST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSendOnlyMatchedHost() {
        return sendOnlyMatchedHost;
    }

    @JsonProperty(JSON_PROPERTY_SEND_ONLY_MATCHED_HOST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSendOnlyMatchedHost(@org.eclipse.jdt.annotation.NonNull Boolean sendOnlyMatchedHost) {
        this.sendOnlyMatchedHost = sendOnlyMatchedHost;
    }

    /**
     * Return true if this DlnaOptions object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DlnaOptions dlnaOptions = (DlnaOptions) o;
        return Objects.equals(this.enablePlayTo, dlnaOptions.enablePlayTo)
                && Objects.equals(this.enableServer, dlnaOptions.enableServer)
                && Objects.equals(this.enableDebugLog, dlnaOptions.enableDebugLog)
                && Objects.equals(this.enablePlayToTracing, dlnaOptions.enablePlayToTracing)
                && Objects.equals(this.clientDiscoveryIntervalSeconds, dlnaOptions.clientDiscoveryIntervalSeconds)
                && Objects.equals(this.aliveMessageIntervalSeconds, dlnaOptions.aliveMessageIntervalSeconds)
                && Objects.equals(this.blastAliveMessageIntervalSeconds, dlnaOptions.blastAliveMessageIntervalSeconds)
                && Objects.equals(this.defaultUserId, dlnaOptions.defaultUserId)
                && Objects.equals(this.autoCreatePlayToProfiles, dlnaOptions.autoCreatePlayToProfiles)
                && Objects.equals(this.blastAliveMessages, dlnaOptions.blastAliveMessages)
                && Objects.equals(this.sendOnlyMatchedHost, dlnaOptions.sendOnlyMatchedHost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enablePlayTo, enableServer, enableDebugLog, enablePlayToTracing,
                clientDiscoveryIntervalSeconds, aliveMessageIntervalSeconds, blastAliveMessageIntervalSeconds,
                defaultUserId, autoCreatePlayToProfiles, blastAliveMessages, sendOnlyMatchedHost);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DlnaOptions {\n");
        sb.append("    enablePlayTo: ").append(toIndentedString(enablePlayTo)).append("\n");
        sb.append("    enableServer: ").append(toIndentedString(enableServer)).append("\n");
        sb.append("    enableDebugLog: ").append(toIndentedString(enableDebugLog)).append("\n");
        sb.append("    enablePlayToTracing: ").append(toIndentedString(enablePlayToTracing)).append("\n");
        sb.append("    clientDiscoveryIntervalSeconds: ").append(toIndentedString(clientDiscoveryIntervalSeconds))
                .append("\n");
        sb.append("    aliveMessageIntervalSeconds: ").append(toIndentedString(aliveMessageIntervalSeconds))
                .append("\n");
        sb.append("    blastAliveMessageIntervalSeconds: ").append(toIndentedString(blastAliveMessageIntervalSeconds))
                .append("\n");
        sb.append("    defaultUserId: ").append(toIndentedString(defaultUserId)).append("\n");
        sb.append("    autoCreatePlayToProfiles: ").append(toIndentedString(autoCreatePlayToProfiles)).append("\n");
        sb.append("    blastAliveMessages: ").append(toIndentedString(blastAliveMessages)).append("\n");
        sb.append("    sendOnlyMatchedHost: ").append(toIndentedString(sendOnlyMatchedHost)).append("\n");
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

        // add `EnablePlayTo` to the URL query string
        if (getEnablePlayTo() != null) {
            joiner.add(String.format("%sEnablePlayTo%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnablePlayTo()))));
        }

        // add `EnableServer` to the URL query string
        if (getEnableServer() != null) {
            joiner.add(String.format("%sEnableServer%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableServer()))));
        }

        // add `EnableDebugLog` to the URL query string
        if (getEnableDebugLog() != null) {
            joiner.add(String.format("%sEnableDebugLog%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableDebugLog()))));
        }

        // add `EnablePlayToTracing` to the URL query string
        if (getEnablePlayToTracing() != null) {
            joiner.add(String.format("%sEnablePlayToTracing%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnablePlayToTracing()))));
        }

        // add `ClientDiscoveryIntervalSeconds` to the URL query string
        if (getClientDiscoveryIntervalSeconds() != null) {
            joiner.add(String.format("%sClientDiscoveryIntervalSeconds%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getClientDiscoveryIntervalSeconds()))));
        }

        // add `AliveMessageIntervalSeconds` to the URL query string
        if (getAliveMessageIntervalSeconds() != null) {
            joiner.add(String.format("%sAliveMessageIntervalSeconds%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAliveMessageIntervalSeconds()))));
        }

        // add `BlastAliveMessageIntervalSeconds` to the URL query string
        if (getBlastAliveMessageIntervalSeconds() != null) {
            joiner.add(String.format("%sBlastAliveMessageIntervalSeconds%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getBlastAliveMessageIntervalSeconds()))));
        }

        // add `DefaultUserId` to the URL query string
        if (getDefaultUserId() != null) {
            joiner.add(String.format("%sDefaultUserId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDefaultUserId()))));
        }

        // add `AutoCreatePlayToProfiles` to the URL query string
        if (getAutoCreatePlayToProfiles() != null) {
            joiner.add(String.format("%sAutoCreatePlayToProfiles%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAutoCreatePlayToProfiles()))));
        }

        // add `BlastAliveMessages` to the URL query string
        if (getBlastAliveMessages() != null) {
            joiner.add(String.format("%sBlastAliveMessages%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getBlastAliveMessages()))));
        }

        // add `SendOnlyMatchedHost` to the URL query string
        if (getSendOnlyMatchedHost() != null) {
            joiner.add(String.format("%sSendOnlyMatchedHost%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSendOnlyMatchedHost()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private DlnaOptions instance;

        public Builder() {
            this(new DlnaOptions());
        }

        protected Builder(DlnaOptions instance) {
            this.instance = instance;
        }

        public DlnaOptions.Builder enablePlayTo(Boolean enablePlayTo) {
            this.instance.enablePlayTo = enablePlayTo;
            return this;
        }

        public DlnaOptions.Builder enableServer(Boolean enableServer) {
            this.instance.enableServer = enableServer;
            return this;
        }

        public DlnaOptions.Builder enableDebugLog(Boolean enableDebugLog) {
            this.instance.enableDebugLog = enableDebugLog;
            return this;
        }

        public DlnaOptions.Builder enablePlayToTracing(Boolean enablePlayToTracing) {
            this.instance.enablePlayToTracing = enablePlayToTracing;
            return this;
        }

        public DlnaOptions.Builder clientDiscoveryIntervalSeconds(Integer clientDiscoveryIntervalSeconds) {
            this.instance.clientDiscoveryIntervalSeconds = clientDiscoveryIntervalSeconds;
            return this;
        }

        public DlnaOptions.Builder aliveMessageIntervalSeconds(Integer aliveMessageIntervalSeconds) {
            this.instance.aliveMessageIntervalSeconds = aliveMessageIntervalSeconds;
            return this;
        }

        public DlnaOptions.Builder blastAliveMessageIntervalSeconds(Integer blastAliveMessageIntervalSeconds) {
            this.instance.blastAliveMessageIntervalSeconds = blastAliveMessageIntervalSeconds;
            return this;
        }

        public DlnaOptions.Builder defaultUserId(String defaultUserId) {
            this.instance.defaultUserId = defaultUserId;
            return this;
        }

        public DlnaOptions.Builder autoCreatePlayToProfiles(Boolean autoCreatePlayToProfiles) {
            this.instance.autoCreatePlayToProfiles = autoCreatePlayToProfiles;
            return this;
        }

        public DlnaOptions.Builder blastAliveMessages(Boolean blastAliveMessages) {
            this.instance.blastAliveMessages = blastAliveMessages;
            return this;
        }

        public DlnaOptions.Builder sendOnlyMatchedHost(Boolean sendOnlyMatchedHost) {
            this.instance.sendOnlyMatchedHost = sendOnlyMatchedHost;
            return this;
        }

        /**
         * returns a built DlnaOptions instance.
         *
         * The builder is not reusable.
         */
        public DlnaOptions build() {
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
    public static DlnaOptions.Builder builder() {
        return new DlnaOptions.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public DlnaOptions.Builder toBuilder() {
        return new DlnaOptions.Builder().enablePlayTo(getEnablePlayTo()).enableServer(getEnableServer())
                .enableDebugLog(getEnableDebugLog()).enablePlayToTracing(getEnablePlayToTracing())
                .clientDiscoveryIntervalSeconds(getClientDiscoveryIntervalSeconds())
                .aliveMessageIntervalSeconds(getAliveMessageIntervalSeconds())
                .blastAliveMessageIntervalSeconds(getBlastAliveMessageIntervalSeconds())
                .defaultUserId(getDefaultUserId()).autoCreatePlayToProfiles(getAutoCreatePlayToProfiles())
                .blastAliveMessages(getBlastAliveMessages()).sendOnlyMatchedHost(getSendOnlyMatchedHost());
    }
}
