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
package org.openhab.binding.heos.internal.json.payload;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Data class for response payloads from browse commands
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class BrowseResult {
    public @Nullable YesNoEnum container;
    @SerializedName("mid")
    public @Nullable String mediaId;
    public @Nullable YesNoEnum playable;
    public @Nullable BrowseResultType type;
    @SerializedName("cid")
    public @Nullable String containerId;
    public @Nullable String name;
    @SerializedName("image_url")
    public @Nullable String imageUrl;

    @Override
    public String toString() {
        return "BrowseResult{" + "container=" + container + ", mediaId='" + mediaId + '\'' + ", playable=" + playable
                + ", type=" + type + ", containerId='" + containerId + '\'' + ", name='" + name + '\'' + ", imageUrl='"
                + imageUrl + '\'' + '}';
    }
}
