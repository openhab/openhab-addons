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
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * WLAN module versions reported by version-1 Robonect firmware.
 */
public class VersionWlan {

    @SerializedName("at-version")
    private @Nullable String atVersion;

    @SerializedName("sdk-version")
    private @Nullable String sdkVersion;

    public @Nullable String getAtVersion() {
        return atVersion;
    }

    public @Nullable String getSdkVersion() {
        return sdkVersion;
    }
}
