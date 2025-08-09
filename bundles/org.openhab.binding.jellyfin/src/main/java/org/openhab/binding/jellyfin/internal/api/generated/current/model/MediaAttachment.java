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

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class MediaAttachment.
 */
@JsonPropertyOrder({ MediaAttachment.JSON_PROPERTY_CODEC, MediaAttachment.JSON_PROPERTY_CODEC_TAG,
        MediaAttachment.JSON_PROPERTY_COMMENT, MediaAttachment.JSON_PROPERTY_INDEX,
        MediaAttachment.JSON_PROPERTY_FILE_NAME, MediaAttachment.JSON_PROPERTY_MIME_TYPE,
        MediaAttachment.JSON_PROPERTY_DELIVERY_URL })

public class MediaAttachment {
    public static final String JSON_PROPERTY_CODEC = "Codec";
    @org.eclipse.jdt.annotation.NonNull
    private String codec;

    public static final String JSON_PROPERTY_CODEC_TAG = "CodecTag";
    @org.eclipse.jdt.annotation.NonNull
    private String codecTag;

    public static final String JSON_PROPERTY_COMMENT = "Comment";
    @org.eclipse.jdt.annotation.NonNull
    private String comment;

    public static final String JSON_PROPERTY_INDEX = "Index";
    @org.eclipse.jdt.annotation.NonNull
    private Integer index;

    public static final String JSON_PROPERTY_FILE_NAME = "FileName";
    @org.eclipse.jdt.annotation.NonNull
    private String fileName;

    public static final String JSON_PROPERTY_MIME_TYPE = "MimeType";
    @org.eclipse.jdt.annotation.NonNull
    private String mimeType;

    public static final String JSON_PROPERTY_DELIVERY_URL = "DeliveryUrl";
    @org.eclipse.jdt.annotation.NonNull
    private String deliveryUrl;

    public MediaAttachment() {
    }

    public MediaAttachment codec(@org.eclipse.jdt.annotation.NonNull String codec) {
        this.codec = codec;
        return this;
    }

    /**
     * Gets or sets the codec.
     * 
     * @return codec
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CODEC)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getCodec() {
        return codec;
    }

    @JsonProperty(JSON_PROPERTY_CODEC)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCodec(@org.eclipse.jdt.annotation.NonNull String codec) {
        this.codec = codec;
    }

    public MediaAttachment codecTag(@org.eclipse.jdt.annotation.NonNull String codecTag) {
        this.codecTag = codecTag;
        return this;
    }

    /**
     * Gets or sets the codec tag.
     * 
     * @return codecTag
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CODEC_TAG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getCodecTag() {
        return codecTag;
    }

    @JsonProperty(JSON_PROPERTY_CODEC_TAG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCodecTag(@org.eclipse.jdt.annotation.NonNull String codecTag) {
        this.codecTag = codecTag;
    }

    public MediaAttachment comment(@org.eclipse.jdt.annotation.NonNull String comment) {
        this.comment = comment;
        return this;
    }

    /**
     * Gets or sets the comment.
     * 
     * @return comment
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_COMMENT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getComment() {
        return comment;
    }

    @JsonProperty(JSON_PROPERTY_COMMENT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setComment(@org.eclipse.jdt.annotation.NonNull String comment) {
        this.comment = comment;
    }

    public MediaAttachment index(@org.eclipse.jdt.annotation.NonNull Integer index) {
        this.index = index;
        return this;
    }

    /**
     * Gets or sets the index.
     * 
     * @return index
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_INDEX)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getIndex() {
        return index;
    }

    @JsonProperty(JSON_PROPERTY_INDEX)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIndex(@org.eclipse.jdt.annotation.NonNull Integer index) {
        this.index = index;
    }

    public MediaAttachment fileName(@org.eclipse.jdt.annotation.NonNull String fileName) {
        this.fileName = fileName;
        return this;
    }

    /**
     * Gets or sets the filename.
     * 
     * @return fileName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_FILE_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getFileName() {
        return fileName;
    }

    @JsonProperty(JSON_PROPERTY_FILE_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFileName(@org.eclipse.jdt.annotation.NonNull String fileName) {
        this.fileName = fileName;
    }

    public MediaAttachment mimeType(@org.eclipse.jdt.annotation.NonNull String mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    /**
     * Gets or sets the MIME type.
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

    public MediaAttachment deliveryUrl(@org.eclipse.jdt.annotation.NonNull String deliveryUrl) {
        this.deliveryUrl = deliveryUrl;
        return this;
    }

    /**
     * Gets or sets the delivery URL.
     * 
     * @return deliveryUrl
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DELIVERY_URL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getDeliveryUrl() {
        return deliveryUrl;
    }

    @JsonProperty(JSON_PROPERTY_DELIVERY_URL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDeliveryUrl(@org.eclipse.jdt.annotation.NonNull String deliveryUrl) {
        this.deliveryUrl = deliveryUrl;
    }

    /**
     * Return true if this MediaAttachment object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MediaAttachment mediaAttachment = (MediaAttachment) o;
        return Objects.equals(this.codec, mediaAttachment.codec)
                && Objects.equals(this.codecTag, mediaAttachment.codecTag)
                && Objects.equals(this.comment, mediaAttachment.comment)
                && Objects.equals(this.index, mediaAttachment.index)
                && Objects.equals(this.fileName, mediaAttachment.fileName)
                && Objects.equals(this.mimeType, mediaAttachment.mimeType)
                && Objects.equals(this.deliveryUrl, mediaAttachment.deliveryUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codec, codecTag, comment, index, fileName, mimeType, deliveryUrl);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MediaAttachment {\n");
        sb.append("    codec: ").append(toIndentedString(codec)).append("\n");
        sb.append("    codecTag: ").append(toIndentedString(codecTag)).append("\n");
        sb.append("    comment: ").append(toIndentedString(comment)).append("\n");
        sb.append("    index: ").append(toIndentedString(index)).append("\n");
        sb.append("    fileName: ").append(toIndentedString(fileName)).append("\n");
        sb.append("    mimeType: ").append(toIndentedString(mimeType)).append("\n");
        sb.append("    deliveryUrl: ").append(toIndentedString(deliveryUrl)).append("\n");
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
}
