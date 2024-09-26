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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents the push request sent to the API.
 *
 * @author Hakan Tandogan - Initial contribution
 * @author Hakan Tandogan - Migrated from openHAB 1 action with the same name
 * @author Jeremy Setton - Add link and file push type support
 */
@NonNullByDefault
public class PushRequest {

    @SerializedName("type")
    private @Nullable PushType type;

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

    @SerializedName("source_device_iden")
    private @Nullable String sourceDeviceIden;

    @SerializedName("device_iden")
    private @Nullable String deviceIden;

    @SerializedName("client_iden")
    private @Nullable String clientIden;

    @SerializedName("channel_tag")
    private @Nullable String channelTag;

    @SerializedName("email")
    private @Nullable String email;

    @SerializedName("guid")
    private @Nullable String guid;

    public void setTitle(@Nullable String title) {
        this.title = title;
    }

    public void setType(@Nullable PushType type) {
        this.type = type;
    }

    public void setBody(@Nullable String body) {
        this.body = body;
    }

    public void setUrl(@Nullable String url) {
        this.url = url;
    }

    public void setFileName(@Nullable String fileName) {
        this.fileName = fileName;
    }

    public void setFileType(@Nullable String fileType) {
        this.fileType = fileType;
    }

    public void setFileUrl(@Nullable String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public void setSourceDeviceIden(@Nullable String sourceDeviceIden) {
        this.sourceDeviceIden = sourceDeviceIden;
    }

    public void setDeviceIden(@Nullable String deviceIden) {
        this.deviceIden = deviceIden;
    }

    public void setClientIden(@Nullable String clientIden) {
        this.clientIden = clientIden;
    }

    public void setChannel(@Nullable String channelTag) {
        this.channelTag = channelTag;
    }

    public void setEmail(@Nullable String email) {
        this.email = email;
    }

    public void setGuid(@Nullable String guid) {
        this.guid = guid;
    }

    @Override
    public String toString() {
        return "Push {type='" + type + "', title='" + title + "', body='" + body + "', url='" + url + "', fileName='"
                + fileName + "', fileType='" + fileType + "', fileUrl='" + fileUrl + "', sourceDeviceIden='"
                + sourceDeviceIden + "', deviceIden='" + deviceIden + "', clientIden='" + clientIden + "', channelTag='"
                + channelTag + "', email='" + email + "', guid='" + guid + "'}";
    }
}
