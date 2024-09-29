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
package org.openhab.binding.pushbullet.internal.model;

import java.time.Instant;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents the push response received from the API.
 *
 * @author Hakan Tandogan - Initial contribution
 * @author Hakan Tandogan - Migrated from openHAB 1 action with the same name
 * @author Jeremy Setton - Add link and file push type support
 */
@NonNullByDefault
public class PushResponse {

    @SerializedName("iden")
    private @Nullable String iden;

    @SerializedName("active")
    private @Nullable Boolean active;

    @SerializedName("created")
    private @Nullable Instant created;

    @SerializedName("modified")
    private @Nullable Instant modified;

    @SerializedName("type")
    private @Nullable PushType type;

    @SerializedName("dismissed")
    private @Nullable Boolean dismissed;

    @SerializedName("guid")
    private @Nullable String guid;

    @SerializedName("direction")
    private @Nullable String direction;

    @SerializedName("sender_iden")
    private @Nullable String senderIdentifier;

    @SerializedName("sender_email")
    private @Nullable String senderEmail;

    @SerializedName("sender_email_normalized")
    private @Nullable String senderEmailNormalized;

    @SerializedName("sender_name")
    private @Nullable String senderName;

    @SerializedName("receiver_iden")
    private @Nullable String receiverIdentifier;

    @SerializedName("receiver_email")
    private @Nullable String receiverEmail;

    @SerializedName("receiver_email_normalized")
    private @Nullable String receiverEmailNormalized;

    @SerializedName("target_device_iden")
    private @Nullable String targetDeviceIden;

    @SerializedName("source_device_iden")
    private @Nullable String sourceDeviceIden;

    @SerializedName("client_iden")
    private @Nullable String clientIden;

    @SerializedName("channel_iden")
    private @Nullable String channelIden;

    @SerializedName("awake_app_guids")
    private @Nullable List<String> awakeAppGuids;

    @SerializedName("title")
    private @Nullable String title;

    @SerializedName("body")
    private @Nullable String body;

    @SerializedName("url")
    private @Nullable String url;

    @SerializedName("file_name")
    private @Nullable String fileName;

    @SerializedName("file_type")
    private @Nullable String fileType;

    @SerializedName("file_url")
    private @Nullable String fileUrl;

    @SerializedName("image_url")
    private @Nullable String imageUrl;

    @SerializedName("image_width")
    private @Nullable Integer imageWidth;

    @SerializedName("image_height")
    private @Nullable Integer imageHeight;

    public @Nullable String getIden() {
        return iden;
    }

    public @Nullable Boolean getActive() {
        return active;
    }

    public @Nullable Instant getCreated() {
        return created;
    }

    public @Nullable Instant getModified() {
        return modified;
    }

    public @Nullable PushType getType() {
        return type;
    }

    public @Nullable Boolean getDismissed() {
        return dismissed;
    }

    public @Nullable String getGuid() {
        return guid;
    }

    public @Nullable String getDirection() {
        return direction;
    }

    public @Nullable String getSenderIdentifier() {
        return senderIdentifier;
    }

    public @Nullable String getSenderEmail() {
        return senderEmail;
    }

    public @Nullable String getSenderEmailNormalized() {
        return senderEmailNormalized;
    }

    public @Nullable String getSenderName() {
        return senderName;
    }

    public @Nullable String getReceiverIdentifier() {
        return receiverIdentifier;
    }

    public @Nullable String getReceiverEmail() {
        return receiverEmail;
    }

    public @Nullable String getReceiverEmailNormalized() {
        return receiverEmailNormalized;
    }

    public @Nullable String getTargetDeviceIden() {
        return targetDeviceIden;
    }

    public @Nullable String getSourceDeviceIden() {
        return sourceDeviceIden;
    }

    public @Nullable String getClientIden() {
        return clientIden;
    }

    public @Nullable String getChannelIden() {
        return channelIden;
    }

    public @Nullable List<String> getAwakeAppGuids() {
        return awakeAppGuids;
    }

    public @Nullable String getTitle() {
        return title;
    }

    public @Nullable String getBody() {
        return body;
    }

    public @Nullable String getUrl() {
        return url;
    }

    public @Nullable String getFileName() {
        return fileName;
    }

    public @Nullable String getFileType() {
        return fileType;
    }

    public @Nullable String getFileUrl() {
        return fileUrl;
    }

    public @Nullable String getImageUrl() {
        return imageUrl;
    }

    public @Nullable Integer getImageWidth() {
        return imageWidth;
    }

    public @Nullable Integer getImageHeight() {
        return imageHeight;
    }

    @Override
    public String toString() {
        return "PushResponse {iden='" + iden + ", active='" + active + "', created='" + created + "', modified='"
                + modified + "', type='" + type + "', dismissed='" + dismissed + "', guid='" + guid + "', direction='"
                + direction + "', senderIdentifier='" + senderIdentifier + "', senderEmail='" + senderEmail
                + "', senderEmailNormalized='" + senderEmailNormalized + "', senderName='" + senderName
                + "', receiverIdentifier='" + receiverIdentifier + "', receiverEmail='" + receiverEmail
                + "', receiverEmailNormalized='" + receiverEmailNormalized + "', targetDeviceIden='" + targetDeviceIden
                + "', sourceDeviceIden='" + sourceDeviceIden + "', clientIden='" + clientIden + "', channelIden='"
                + channelIden + "', awakeAppGuids='" + awakeAppGuids + "', title='" + title + "', body='" + body
                + "', url='" + url + "', fileName='" + fileName + "', fileType='" + fileType + "', fileUrl='" + fileUrl
                + "', imageUrl='" + imageUrl + "', imageWidth='" + imageWidth + "', imageHeight='" + imageHeight + "'}";
    }
}
