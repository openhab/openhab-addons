/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.lghorizon.internal.api.dto;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * A single entry of {@code GET /linearService/v2/channels}.
 *
 * @author Mark - Initial contribution
 */
public class ChannelDto {

    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("logicalChannelNumber")
    public String logicalChannelNumber;

    @SerializedName("isRadio")
    public boolean isRadio;

    @SerializedName("linearProducts")
    public List<String> linearProducts;

    @SerializedName("imageStream")
    public ImageStreamDto imageStream;

    public List<String> getLinearProducts() {
        List<String> products = linearProducts;
        return products == null ? List.of() : products;
    }

    /** The channel's own logo/preview image, preferring the full-size variant if both are present. */
    public String getStreamImage() {
        ImageStreamDto stream = imageStream;
        if (stream == null) {
            return null;
        }
        return stream.full != null ? stream.full : stream.small;
    }

    public static class ImageStreamDto {
        @SerializedName("full")
        public String full;

        @SerializedName("small")
        public String small;
    }

    @Override
    public String toString() {
        return "ChannelDto [id=" + id + ", name=" + name + ", logicalChannelNumber=" + logicalChannelNumber
                + ", isRadio=" + isRadio + ", linearProducts=" + linearProducts + "]";
    }
}
