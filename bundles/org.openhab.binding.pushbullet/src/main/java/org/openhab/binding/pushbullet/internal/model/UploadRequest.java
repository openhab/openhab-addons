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
 * This class represents the upload request sent to the API.
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class UploadRequest {

    @SerializedName("file_name")
    private @Nullable String fileName;

    @SerializedName("file_type")
    private @Nullable String fileType;

    public void setFileName(@Nullable String fileName) {
        this.fileName = fileName;
    }

    public void setFileType(@Nullable String fileType) {
        this.fileType = fileType;
    }

    @Override
    public String toString() {
        return "UploadRequest {fileName='" + fileName + "', fileType='" + fileType + "'}";
    }
}
