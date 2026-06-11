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
package org.openhab.binding.unifiprotect.internal.api.priv.dto.types;

import com.google.gson.annotations.SerializedName;

/**
 * Recording mode types
 *
 * @author Dan Cunningham - Initial contribution
 */
public enum RecordingMode {
    @SerializedName("never")
    NEVER,

    @SerializedName("always")
    ALWAYS,

    @SerializedName("motion")
    MOTION,

    @SerializedName("smartDetect")
    SMART_DETECT,

    @SerializedName("detections")
    DETECTIONS
}
