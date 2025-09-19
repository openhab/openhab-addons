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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * ResponseProfile
 */
@JsonPropertyOrder({ ResponseProfile.JSON_PROPERTY_CONTAINER, ResponseProfile.JSON_PROPERTY_AUDIO_CODEC,
        ResponseProfile.JSON_PROPERTY_VIDEO_CODEC, ResponseProfile.JSON_PROPERTY_TYPE,
        ResponseProfile.JSON_PROPERTY_ORG_PN, ResponseProfile.JSON_PROPERTY_MIME_TYPE,
        ResponseProfile.JSON_PROPERTY_CONDITIONS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ResponseProfile {
    public static final String JSON_PROPERTY_CONTAINER = "Container";
    @org.eclipse.jdt.annotation.NonNull
    private String container;

    public static final String JSON_PROPERTY_AUDIO_CODEC = "AudioCodec";
    @org.eclipse.jdt.annotation.NonNull
    private String audioCodec;

    public static final String JSON_PROPERTY_VIDEO_CODEC = "VideoCodec";
    @org.eclipse.jdt.annotation.NonNull
    private String videoCodec;

    public static final String JSON_PROPERTY_TYPE = "Type";
    @org.eclipse.jdt.annotation.NonNull
    private DlnaProfileType type;

    public static final String JSON_PROPERTY_ORG_PN = "OrgPn";
    @org.eclipse.jdt.annotation.NonNull
    private String orgPn;

    public static final String JSON_PROPERTY_MIME_TYPE = "MimeType";
    @org.eclipse.jdt.annotation.NonNull
    private String mimeType;

    public static final String JSON_PROPERTY_CONDITIONS = "Conditions";
    @org.eclipse.jdt.annotation.NonNull
    private List<ProfileCondition> conditions;

    public ResponseProfile() {
    }

    public ResponseProfile container(@org.eclipse.jdt.annotation.NonNull String container) {
        this.container = container;
        return this;
    }

    /**
     * Get container
     * 
     * @return container
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CONTAINER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getContainer() {
        return container;
    }

    @JsonProperty(JSON_PROPERTY_CONTAINER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setContainer(@org.eclipse.jdt.annotation.NonNull String container) {
        this.container = container;
    }

    public ResponseProfile audioCodec(@org.eclipse.jdt.annotation.NonNull String audioCodec) {
        this.audioCodec = audioCodec;
        return this;
    }

    /**
     * Get audioCodec
     * 
     * @return audioCodec
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_AUDIO_CODEC)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getAudioCodec() {
        return audioCodec;
    }

    @JsonProperty(JSON_PROPERTY_AUDIO_CODEC)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAudioCodec(@org.eclipse.jdt.annotation.NonNull String audioCodec) {
        this.audioCodec = audioCodec;
    }

    public ResponseProfile videoCodec(@org.eclipse.jdt.annotation.NonNull String videoCodec) {
        this.videoCodec = videoCodec;
        return this;
    }

    /**
     * Get videoCodec
     * 
     * @return videoCodec
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_VIDEO_CODEC)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getVideoCodec() {
        return videoCodec;
    }

    @JsonProperty(JSON_PROPERTY_VIDEO_CODEC)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setVideoCodec(@org.eclipse.jdt.annotation.NonNull String videoCodec) {
        this.videoCodec = videoCodec;
    }

    public ResponseProfile type(@org.eclipse.jdt.annotation.NonNull DlnaProfileType type) {
        this.type = type;
        return this;
    }

    /**
     * Get type
     * 
     * @return type
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public DlnaProfileType getType() {
        return type;
    }

    @JsonProperty(JSON_PROPERTY_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setType(@org.eclipse.jdt.annotation.NonNull DlnaProfileType type) {
        this.type = type;
    }

    public ResponseProfile orgPn(@org.eclipse.jdt.annotation.NonNull String orgPn) {
        this.orgPn = orgPn;
        return this;
    }

    /**
     * Get orgPn
     * 
     * @return orgPn
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ORG_PN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getOrgPn() {
        return orgPn;
    }

    @JsonProperty(JSON_PROPERTY_ORG_PN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOrgPn(@org.eclipse.jdt.annotation.NonNull String orgPn) {
        this.orgPn = orgPn;
    }

    public ResponseProfile mimeType(@org.eclipse.jdt.annotation.NonNull String mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    /**
     * Get mimeType
     * 
     * @return mimeType
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MIME_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getMimeType() {
        return mimeType;
    }

    @JsonProperty(JSON_PROPERTY_MIME_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMimeType(@org.eclipse.jdt.annotation.NonNull String mimeType) {
        this.mimeType = mimeType;
    }

    public ResponseProfile conditions(@org.eclipse.jdt.annotation.NonNull List<ProfileCondition> conditions) {
        this.conditions = conditions;
        return this;
    }

    public ResponseProfile addConditionsItem(ProfileCondition conditionsItem) {
        if (this.conditions == null) {
            this.conditions = new ArrayList<>();
        }
        this.conditions.add(conditionsItem);
        return this;
    }

    /**
     * Get conditions
     * 
     * @return conditions
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CONDITIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<ProfileCondition> getConditions() {
        return conditions;
    }

    @JsonProperty(JSON_PROPERTY_CONDITIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setConditions(@org.eclipse.jdt.annotation.NonNull List<ProfileCondition> conditions) {
        this.conditions = conditions;
    }

    /**
     * Return true if this ResponseProfile object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ResponseProfile responseProfile = (ResponseProfile) o;
        return Objects.equals(this.container, responseProfile.container)
                && Objects.equals(this.audioCodec, responseProfile.audioCodec)
                && Objects.equals(this.videoCodec, responseProfile.videoCodec)
                && Objects.equals(this.type, responseProfile.type) && Objects.equals(this.orgPn, responseProfile.orgPn)
                && Objects.equals(this.mimeType, responseProfile.mimeType)
                && Objects.equals(this.conditions, responseProfile.conditions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(container, audioCodec, videoCodec, type, orgPn, mimeType, conditions);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ResponseProfile {\n");
        sb.append("    container: ").append(toIndentedString(container)).append("\n");
        sb.append("    audioCodec: ").append(toIndentedString(audioCodec)).append("\n");
        sb.append("    videoCodec: ").append(toIndentedString(videoCodec)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    orgPn: ").append(toIndentedString(orgPn)).append("\n");
        sb.append("    mimeType: ").append(toIndentedString(mimeType)).append("\n");
        sb.append("    conditions: ").append(toIndentedString(conditions)).append("\n");
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

        // add `Container` to the URL query string
        if (getContainer() != null) {
            joiner.add(String.format("%sContainer%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getContainer()))));
        }

        // add `AudioCodec` to the URL query string
        if (getAudioCodec() != null) {
            joiner.add(String.format("%sAudioCodec%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAudioCodec()))));
        }

        // add `VideoCodec` to the URL query string
        if (getVideoCodec() != null) {
            joiner.add(String.format("%sVideoCodec%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getVideoCodec()))));
        }

        // add `Type` to the URL query string
        if (getType() != null) {
            joiner.add(String.format("%sType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getType()))));
        }

        // add `OrgPn` to the URL query string
        if (getOrgPn() != null) {
            joiner.add(String.format("%sOrgPn%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getOrgPn()))));
        }

        // add `MimeType` to the URL query string
        if (getMimeType() != null) {
            joiner.add(String.format("%sMimeType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMimeType()))));
        }

        // add `Conditions` to the URL query string
        if (getConditions() != null) {
            for (int i = 0; i < getConditions().size(); i++) {
                if (getConditions().get(i) != null) {
                    joiner.add(getConditions().get(i).toUrlQueryString(String.format("%sConditions%s%s", prefix, suffix,
                            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        return joiner.toString();
    }

    public static class Builder {

        private ResponseProfile instance;

        public Builder() {
            this(new ResponseProfile());
        }

        protected Builder(ResponseProfile instance) {
            this.instance = instance;
        }

        public ResponseProfile.Builder container(String container) {
            this.instance.container = container;
            return this;
        }

        public ResponseProfile.Builder audioCodec(String audioCodec) {
            this.instance.audioCodec = audioCodec;
            return this;
        }

        public ResponseProfile.Builder videoCodec(String videoCodec) {
            this.instance.videoCodec = videoCodec;
            return this;
        }

        public ResponseProfile.Builder type(DlnaProfileType type) {
            this.instance.type = type;
            return this;
        }

        public ResponseProfile.Builder orgPn(String orgPn) {
            this.instance.orgPn = orgPn;
            return this;
        }

        public ResponseProfile.Builder mimeType(String mimeType) {
            this.instance.mimeType = mimeType;
            return this;
        }

        public ResponseProfile.Builder conditions(List<ProfileCondition> conditions) {
            this.instance.conditions = conditions;
            return this;
        }

        /**
         * returns a built ResponseProfile instance.
         *
         * The builder is not reusable.
         */
        public ResponseProfile build() {
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
    public static ResponseProfile.Builder builder() {
        return new ResponseProfile.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public ResponseProfile.Builder toBuilder() {
        return new ResponseProfile.Builder().container(getContainer()).audioCodec(getAudioCodec())
                .videoCodec(getVideoCodec()).type(getType()).orgPn(getOrgPn()).mimeType(getMimeType())
                .conditions(getConditions());
    }
}
