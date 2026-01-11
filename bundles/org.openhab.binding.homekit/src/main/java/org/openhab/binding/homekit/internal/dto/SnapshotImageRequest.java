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

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * HomeKit resource DTO used for making a POST /resource request to fetch an IP camera snapshot image
 * with a pre-filled resource type plus standard definition 16:9 image width and height parameters.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class SnapshotImageRequest {
    protected final @SerializedName("resource-type") String type = "image";
    protected final @SerializedName("image-width") Long width = 1280L;
    protected final @SerializedName("image-height") Long height = 720L;
}
