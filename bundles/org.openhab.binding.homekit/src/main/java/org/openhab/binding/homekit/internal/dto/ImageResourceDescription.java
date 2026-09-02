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
package org.openhab.binding.homekit.internal.dto;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * HomeKit resource description DTO used for making POST /resource requests. This description
 * class is specifically for the 'image' resource type. It is used to POST a request for an IP
 * camera snapshot image according to the Apple specification 'Chapter 11.5 Image Snapshots'.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ImageResourceDescription {
    protected final Long aid;
    protected final @SerializedName("resource-type") String type = "image";
    protected final @SerializedName("image-width") Long width;
    protected final @SerializedName("image-height") Long height;

    /**
     * Creates an {@link ImageResourceDescription} with the given accessory ID and size.
     *
     * @param accessoryId the accessory ID
     * @param size a Map.Entry specifying the image width (K) and height (V)
     */
    public ImageResourceDescription(Long accessoryId, Map.Entry<Long, Long> size) {
        aid = accessoryId;
        width = size.getKey();
        height = size.getValue();
    }
}
