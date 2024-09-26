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
 * This class represents the upload response received from the API.
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class UploadResponse {

    @SerializedName("file_name")
    private @Nullable String fileName;

    @SerializedName("file_type")
    private @Nullable String fileType;

    @SerializedName("file_url")
    private @Nullable String fileUrl;

    @SerializedName("upload_url")
    private @Nullable String uploadUrl;

    public @Nullable String getFileName() {
        return fileName;
    }

    public @Nullable String getFileType() {
        return fileType;
    }

    public @Nullable String getFileUrl() {
        return fileUrl;
    }

    public @Nullable String getUploadUrl() {
        return uploadUrl;
    }

    @Override
    public String toString() {
        return "UploadResponse {fileName='" + fileName + "', fileType='" + fileType + "', fileUrl='" + fileUrl
                + "', uploadUrl='" + uploadUrl + "'}";
    }
}
